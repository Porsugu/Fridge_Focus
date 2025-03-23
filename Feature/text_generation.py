import base64
import json
import re
import requests
from google import genai
from google.genai import types
from dotenv import load_dotenv
import os

load_dotenv()

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
    
    # Create the JSON object
    recipe_json = {
        "name": name,
        "guide": guide,
        "ingredients": ingredients
    }
    
    return recipe_json

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

def generate(prompt):
    client = genai.Client(
        api_key=os.environ.get("GEMINI_API_KEY"),
    )

    # Prompt goes here
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

if __name__ == "__main__":
    user_input = input("Enter your prompt (e.g., 'Generate a recipe for chocolate cake'): ")
    generate(user_input)