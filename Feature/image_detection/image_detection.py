from ultralytics import YOLO
import cv2
import matplotlib.pyplot as plt
from collections import Counter

# Load YOLO model
model = YOLO("Gemini Test/image_detection/best.pt")

# Path to one test image
image_path = "Gemini Test/image_detection/test_data/1685517940871_jpg.rf.341193681cf29c75577dd32a5759b842.jpg"  # <- replace with your actual image

# Run prediction
results = model.predict(
    source=image_path,
    save=True,
    project="Gemini Test/image_detection",
    name="prediction",
    exist_ok=True
)

# Get class name mapping
class_names = model.names

# Get all class IDs from the boxes
class_ids = [int(box.cls[0]) for box in results[0].boxes]

# Count how many times each class appears
counts = Counter(class_ids)

# Display detected items with counts
print("🧾 Detected items in image:")
for cls_id, count in counts.items():
    print(f" - {class_names[cls_id]}: {count}")

