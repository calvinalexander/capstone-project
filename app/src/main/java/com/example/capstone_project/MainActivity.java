package com.example.capstone_project;


//import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.capstone_project.ml.XrayModelMetadata;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
//import com.google.mlkit.common.model.CustomRemoteModel;
//import com.google.mlkit.linkfirebase.FirebaseModelSource;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.label.ImageLabel;
//import com.google.mlkit.vision.label.ImageLabeler;
//import com.google.mlkit.vision.label.ImageLabeling;
//import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
//import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button upload_btn, predict_btn;
    private TextView tv;
    private Bitmap image;
//    private InputImage img;
    private Interpreter interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imgv);
        upload_btn = (Button) findViewById(R.id.upload);
        predict_btn = (Button) findViewById(R.id.predict);
        tv = (TextView) findViewById(R.id.confidence);

//        CustomRemoteModel remoteModel =
//                new CustomRemoteModel
//                        .Builder(new FirebaseModelSource.Builder("xray-model-metadata").build())
//                        .build();
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("xray-model-metadata", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {

                        // Download complete. Depending on your app, you could enable the ML
                        // feature, or switch from the local model to the remote model, etc.
                        // The CustomModel object contains the local path of the model file,
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        File modelFile = model.getFile();
                        if (modelFile != null) {
//                            upload_btn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                                    intent.setType("image/*");
//                                    startActivityForResult(intent, 101);
//                                }
//                            });
//
//                            CustomImageLabelerOptions.Builder optionsBuilder;
//                            optionsBuilder = new CustomImageLabelerOptions.Builder(remoteModel);
//                            CustomImageLabelerOptions options = optionsBuilder
//                                    .setConfidenceThreshold(0.5f)
//                                    .setMaxResultCount(2)
//                                    .build();
//                            ImageLabeler labeler = ImageLabeling.getClient(options);
//
//                            predict_btn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    image = Bitmap.createScaledBitmap(image, 175, 175, true);
//                                    img = InputImage.fromBitmap(image,0);
//                                    labeler.process(img)
//                                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
//                                                @Override
//                                                public void onSuccess(List<ImageLabel> labels) {
//                                                    for (ImageLabel label : labels) {
//                                                        String text = label.getText();
//                                                        float confidence = label.getConfidence();
//                                                        int index = label.getIndex();
//                                                        tv.setText("Category:"+text+"\nconfidence: "+confidence);
//                                                    }
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                }
//                                            });
//                                }});


                            Toast.makeText(getApplicationContext(),"Model Downloading", Toast.LENGTH_LONG);
                            interpreter = new Interpreter(modelFile);

                            upload_btn.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, 101);
                                }
                            });

                            predict_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    image = Bitmap.createScaledBitmap(image, 175, 175, true);
                                    ByteBuffer input = ByteBuffer.allocateDirect(367500).order(ByteOrder.nativeOrder());
                                    for (int y = 0; y < 175; y++) {
                                        for (int x = 0; x < 175; x++) {
                                            int px = image.getPixel(x, y);

                                            // Get channel values from the pixel value.
                                            int r = Color.red(px);
                                            int g = Color.green(px);
                                            int b = Color.blue(px);

                                            // Normalize channel values to [-1.0, 1.0]. This requirement depends
                                            // on the model. For example, some models might require values to be
                                            // normalized to the range [0.0, 1.0] instead.
                                            float rf = (r) / 255.0f;
                                            float gf = (g) / 255.0f;
                                            float bf = (b) / 255.0f;

                                            input.putFloat(rf);
                                            input.putFloat(gf);
                                            input.putFloat(bf);
                                        }
                                    }

                                    int bufferSize = 1000 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
                                    ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
                                    interpreter.run(input, modelOutput);

                                    modelOutput.rewind();
                                    FloatBuffer probabilities = modelOutput.asFloatBuffer();
                                    try {
                                        BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(getAssets().open("label")));
                                        for (int i = 0; i < 2; i++) {
                                            String label = reader.readLine();
                                            float probability = probabilities.get(i);
                                            if(probability > 0.5){
                                                tv.setText("Category: "+label+"\nConfidence: "+probability);
                                            }
                                        }

                                    } catch (IOException e) {
                                        tv.setText("Null Probabilities!");
                                    }

                                }
                            });
                        }
                        else {
                            upload_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, 101);

                                }
                            });
                            predict_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    image = Bitmap.createScaledBitmap(image, 175, 175, true);
                                    try {
                                        XrayModelMetadata model = XrayModelMetadata.newInstance(getApplicationContext());
                                        // Runs model inference and gets result.
                                        XrayModelMetadata.Outputs outputs = model.process(TensorImage.fromBitmap(image));
                                        List<Category> probability = outputs.getProbabilityAsCategoryList();
                                        tv.setText("confidence: " + probability);
                                        // Releases model resources if no longer used.
                                        model.close();
                                    } catch (IOException e) {
                                        // TODO Handle the exception
                                    }
                                }
                            });
                        }
                    }


//                    public void onClick(View v) {
//                        image = Bitmap.createScaledBitmap(image, 175, 175, true);
//                        img = InputImage.fromBitmap(image,0);
//                        labeler.process(img)
//                                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
//                                    @Override
//                                    public void onSuccess(List<ImageLabel> labels) {
//                                        for (ImageLabel label : labels) {
//                                            String text = label.getText();
//                                            float confidence = label.getConfidence();
//                                            int index = label.getIndex();
//                                            categories = new ArrayList<>();
//                                            categories.add(confidence);
//                                            tv.setText("Label: "+text+"\nconfidence: "+confidence+"\n"+index);
//
//                                        }
//
////                                                tv.setText("confidence: "+categories);
//
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });
//                    }
                });
    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            imageView.setImageURI(data.getData());
            Uri uri = data.getData();
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
