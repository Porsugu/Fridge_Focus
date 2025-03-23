import os
from google import genai
from google.genai import types
import requests
import mimetypes
from IPython.display import display, Markdown, Image
import pathlib
from dotenv import load_dotenv
import base64

load_dotenv()

# ==================== Image Generation ====================
client = genai.Client(
    api_key=os.environ.get("GEMINI_API_KEY"),
)

MODEL_ID = "gemini-2.0-flash-exp"

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

# Prompt goes here
contents = 'Generate a omelette'

response = client.models.generate_content(
    model=MODEL_ID,
    contents=contents,
    config=types.GenerateContentConfig(
        response_modalities=['Text', 'Image']
    )
)

display_response(response)
save_image(response, 'omelette.png')



# ==================== Get URL of image ====================
API_KEY = os.environ.get("IMGBB_API_KEY")
IMAGE_PATH = 'omelette.png'
UPLOAD_URL = 'https://api.imgbb.com/1/upload'

def upload_image_to_imgbb(image_path, api_key):
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
    
    # Handle the response
    if response.status_code == 200:
        image_url = response.json()['data']['url']
        print('✅ Image uploaded successfully!')
        print('🌐 Image URL:', image_url)
        return image_url
    else:
        print('❌ Upload failed.')
        print('Status Code:', response.status_code)
        print('Response:', response.text)
        return None

# Run it
if __name__ == '__main__':
    upload_image_to_imgbb(IMAGE_PATH, API_KEY)