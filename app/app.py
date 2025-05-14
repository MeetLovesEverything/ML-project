from fastapi import FastAPI, UploadFile, File
from fastapi.responses import JSONResponse
import numpy as np
import librosa
import io
import tensorflow as tf

app = FastAPI()

# Labels used during model training
LANGUAGE_LABELS = [
    "*Bengali", "*Gujrati", "*Hindi", "*Kannada", "*Malyalam",
    "*Marathi", "*Tamil", "*Telgu", "*Urdu", "Gujrati"
]

# Load model (adjust path if needed)
model = tf.keras.models.load_model("app/language_model_new.h5")

def extract_features(audio_bytes: bytes) -> np.ndarray:
    """Extract MFCC features from audio bytes"""
    y, sr = librosa.load(io.BytesIO(audio_bytes), sr=22050)
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=40)
    mfcc_mean = np.mean(mfcc.T, axis=0)
    return mfcc_mean

@app.post("/predict-language")
async def predict_language(audio_file: UploadFile = File(...)):
    try:
        audio_bytes = await audio_file.read()
        features = extract_features(audio_bytes)

        # Reshape to (1, 40) for model input
        input_tensor = np.expand_dims(features, axis=0)

        predictions = model.predict(input_tensor)
        predicted_index = np.argmax(predictions, axis=1)[0]
        predicted_language = LANGUAGE_LABELS[predicted_index]

        return JSONResponse({"predicted_language": predicted_language})

    except Exception as e:
        return JSONResponse(content={"error": str(e)}, status_code=500)
