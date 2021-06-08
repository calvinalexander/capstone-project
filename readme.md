# capstone-project-nightingales
Project from Bangkit Program with the Choosen Theme Healthcare to Help on Diagnose COVID-19 Suspect condition.

Documentation
Tensorflow Model Creation Steps on Google Colab:
1. Download the kaggle dataset through kaggle api endpoint URL
2. Unzip the dataset
3. Split the dataset into training and validation data
4. Preprocessing the data using image augmentation
5. Batch the data using image generator
6. Train and test the model
7. Visualize the train and test accuracy graph of trained model
8. Test the prediction result with different data
9. Convert trained model into tensorflow lite format using tflite converter
10. Add metadata into the tflite model (ml kit requirement)

Model Deployment on Cloud Steps on Firebase Cloud Platform:
1. Add firebase using firebase console
2. Create firebase project
3. Register the app used for development to firebase project
4. Upload firebase configuration to the app
5. Add firebase dependencies for loading the remote model
6. Deploy the tflite model into firebase cloud platform as custom model

Application Development Step on Android Studio:
1. Create remote model download function
2. Upload tflite model into asset folder as local model
3. Perform inference on input data for both model
4. Run the interpreter
5. Testing both model to predict images