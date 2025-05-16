
# Multilingual Speech Recognition

This project aims to develop a robust multilingual speech recognition system capable of accurately transcribing audio inputs in multiple languages.
Leveraging advanced machine learning techniques, the system is designed to handle diverse linguistic inputs, making it suitable for applications like virtual assistants, transcription services, and language translation tools.

## Features

- **Multilingual Support**: Recognizes and transcribes speech in multiple languages.
- **Machine Learning Integration**: Utilizes state-of-the-art ML models for speech-to-text conversion.
- **Mobile Application**: Includes a mobile app interface for real-time speech recognition.
- **Chatbot Functionality**: Integrates a chatbot for interactive user engagement.
- **Training Module**: Provides scripts and tools for training custom speech recognition models.

## Working Videos of project and its workflow

The video of the app and the model can be found at -https://drive.google.com/drive/folders/1EBOd8fOkCR1XUxLhG7jZwPFo9NDOz5hH?usp=sharing


## Project Structure

```
ML-project/
├── app/                   # Mobile application source code
├── mobile app/ chatbot/   # Chatbot integration for the mobile app
├── videos/                # Demonstration videos and related media
├── model_train.py         # Script for training the speech recognition model
├── requirements.txt       # Python dependencies
```

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/MeetLovesEverything/ML-project.git
   cd ML-project
   ```

2. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

3. **Set Up the Mobile App**:
   Navigate to the `app/` directory and follow the platform-specific instructions to build and run the mobile application.

## Usage

- **Training the Model**:
  ```bash
  python model_train.py
  ```
  This script will initiate the training process using the provided datasets and configurations.

- **Running the Mobile App**:
  Follow the instructions in the `app/` directory to deploy and run the mobile application on your device.

- **Interacting with the Chatbot**:
  The chatbot can be accessed through the mobile application, providing an interactive interface for speech recognition tasks.

## Contributing

Contributions are welcome! If you'd like to enhance the multilingual capabilities, improve the mobile app, or add new features, please follow these steps:

1. **Fork the Repository**: Click on the 'Fork' button at the top right of the repository page.

2. **Create a New Branch**:
   ```bash
   git checkout -b feature/YourFeatureName
   ```

3. **Commit Your Changes**:
   ```bash
   git commit -m "Add your message here"
   ```

4. **Push to the Branch**:
   ```bash
   git push origin feature/YourFeatureName
   ```

5. **Open a Pull Request**: Navigate to your forked repository and click on 'New Pull Request'.

## License

This project is licensed under the [MIT License](LICENSE). Feel free to use, modify, and distribute the code as per the license terms.

## Acknowledgements

We would like to thank the open-source community and contributors who have provided valuable resources and tools that have made this project possible.


## API Access

The backend API for the multilingual speech recognition system is deployed on **Render**.

- **Base URL**: [https://ml-project-kaek.onrender.com](https://ml-project-kaek.onrender.com)

You can use this API for:
- Uploading audio and receiving transcriptions
- Getting supported language lists
- Integration with frontend or mobile applications

## Mobile Application

A mobile application is available for real-time speech recognition and chatbot interaction.

- 📱 **Download APK**: [Google Drive Link](https://drive.google.com/drive/folders/1d9jO2CPptZh6zVmbOpfV2qNT9vlPZ3g4?usp=sharing)

Follow the instructions in the mobile app folder to set up and explore features.

