import json
import re
import requests
from google import genai
from google.genai import types
from dotenv import load_dotenv
import requests
from IPython.display import display, Markdown, Image
import pathlib
from dotenv import load_dotenv
import base64
import os

load_dotenv()

# ==================== Convert Result to Jason ====================
def parse_recipe_to_json(recipe_text):
    """Parse recipe text into the desired JSON format."""
    # Initialize variables
    lines = recipe_text.strip().split('\n')
    name = lines[0].strip()
    
    ingredients = []
    instructions = []
    current_section = None
    
    # Process each line
    for line in lines[1:]:
        line = line.strip()
        if not line:
            continue
            
        if line == "Ingredients":
            current_section = "ingredients"
        elif line == "Instructions":
            current_section = "instructions"
        elif current_section == "ingredients" and line.startswith("- "):
            # Parse ingredient line (e.g. "- 0.5 kg Beef")
            ingredient_text = line[2:].strip()
            parts = ingredient_text.split(' ', 2)
            
            if len(parts) >= 3:
                quantity = float(parts[0])
                unit = parts[1]
                ingredient_name = parts[2]
            else:
                # Handle case like "- 1 Onion"
                quantity = float(parts[0])
                ingredient_name = parts[1]
                unit = "piece"  # Default unit
                
            ingredients.append({
                "name": ingredient_name,
                "quantity": quantity,
                "unit": unit
            })
        elif current_section == "instructions" and re.match(r"^\d+\.", line):
            instructions.append(line)
    
    # Join instructions
    guide = " ".join(instructions)
    
    # Image Generation
    image_generation(name)
    url = upload_image_to_imgbb()
    print("")


    # Create the JSON object
    recipe_json = {
        "name": name,
        "guide": guide,
        "ingredients": ingredients,
        "url": url
    }
    
    return recipe_json


# ==================== POST to DB ====================
def post_recipe_to_api(recipe_json):
    """Send the recipe JSON to the Flask API."""
    API_URL = "https://fridgefocus-backend.onrender.com/addRecipes"  # Replace with your actual API URL
    
    try:
        # Make the POST request
        response = requests.post(
            API_URL,
            json=recipe_json,
            headers={"Content-Type": "application/json"}
        )
        
        # Check if request was successful
        if response.status_code == 201:
            print(f"Recipe successfully added! Response: {response.json()}")
            return True
        else:
            print(f"Failed to add recipe. Status code: {response.status_code}")
            print(f"Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"Error posting to API: {e}")
        return False


# ==================== Generate Recipe ====================
def generate():
    client = genai.Client(
        api_key=os.environ.get("GEMINI_API_KEY"),
    )
    
    # Prompt goes here
    inventory_list = []
    # TODO: Get list of ingredients
    inventory_list = ['apple', 'banana', 'garlic', 'onion', 'tofu', 'spinach', "orange", "broccoli"]

    prompt = (
        f"Give me a recipe with the following ingredients (don't need to use all): {inventory_list}. "
        "Make the format to be ingredients first with a title of ingredients on the first line, "
        '"-" "1 space" "number" "measurement like kg or ml" "ingredient". '
        'After the list of ingredients, add a title for "Instructions", with the format '
        '"1." "1 space" "step 1". So, the first line is "the food name", the second line is "title Ingredients", '
        'the third line and until the end of the ingredient list, the following line is "the title Instructions", '
        "then the next lines are the instructions."
        "IMPORTANT: No spaces between line!!!!"
        "IMPORTANT: Always number, space, then unit, including 200g, should be 200 g!!!!"
        "IMPORTANT: Always have unit, space, ingredient, don't forget the unit, including 0.5 onion, it should be 0.5 piece onion!!!!"
    )

    model = "gemini-2.0-flash"
    contents = [
        types.Content(
            role="user",
            parts=[
                types.Part.from_text(text=prompt),
            ],
        ),
    ]
    generate_content_config = types.GenerateContentConfig(
        temperature=1,
        top_p=0.95,
        top_k=40,
        max_output_tokens=8192,
        response_mime_type="text/plain",
    )

    # Collect all chunks of text
    full_response = ""
    for chunk in client.models.generate_content_stream(
        model=model,
        contents=contents,
        config=generate_content_config,
    ):
        chunk_text = chunk.text
        full_response += chunk_text
        print(chunk_text, end="")  # Still print as they come in
    
    # Process the full response into JSON
    print("\n\n--- Parsed Recipe ---\n")
    try:
        recipe_json = parse_recipe_to_json(full_response)
        print(json.dumps(recipe_json, indent=2))
        
        # Ask user if they want to save this recipe
        save_choice = input("\nDo you want to save this recipe to the database? (y/n): ")
        if save_choice.lower() == 'y':
            post_recipe_to_api(recipe_json)
        
        return recipe_json
    except Exception as e:
        print(f"Error parsing recipe: {e}")
        return None


def display_response(response):
  for part in response.candidates[0].content.parts:
    if part.text is not None:
      display(Markdown(part.text))
    elif part.inline_data is not None:
      mime = part.inline_data.mime_type
      print(mime)
      data = part.inline_data.data
      display(Image(data=data))
def save_image(response, path):
  for part in response.candidates[0].content.parts:
    if part.text is not None:
      continue
    elif part.inline_data is not None:
      mime = part.inline_data.mime_type
      data = part.inline_data.data
      pathlib.Path(path).write_bytes(data)

# ==================== Genrate Food Image ====================
def image_generation(food_name):
    client = genai.Client(
        api_key=os.environ.get("GEMINI_API_KEY"),
    )

    MODEL_ID = "gemini-2.0-flash-exp"

    # Prompt goes here
    contents = f"Generate an image of a {food_name}"

    response = client.models.generate_content(
        model=MODEL_ID,
        contents=contents,
        config=types.GenerateContentConfig(
            response_modalities=['Text', 'Image']
        )
    )

    display_response(response)
    save_image(response, 'Feature/generated_image.png')


# ==================== Upload for Image URL ====================
def upload_image_to_imgbb():
    api_key = os.environ.get("IMGBB_API_KEY")
    image_path = 'Feature/generated_image.png'
    UPLOAD_URL = 'https://api.imgbb.com/1/upload'

    # Read and encode the image to base64
    with open(image_path, 'rb') as image_file:
        encoded_image = base64.b64encode(image_file.read()).decode('utf-8')
    
    # Prepare the payload
    payload = {
        'key': api_key,
        'image': encoded_image
    }
    
    # Send POST request
    response = requests.post(UPLOAD_URL, data=payload)
    os.remove(image_path)
    
    # Handle the response
    if response.status_code == 200:
        image_url = response.json()['data']['url']
        print('Image uploaded successfully!')
        print('Image URL:', image_url)
        return image_url
    else:
        print('Upload failed.')
        print('Status Code:', response.status_code)
        print('Response:', response.text)
        return None

if __name__ == "__main__":
    # user_input = input("Enter your prompt (e.g., 'Generate a recipe for chocolate cake'): ")
    # generate(user_input)
    generate()
    