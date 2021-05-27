package com.example.capstone_project;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.capstone_project.ml.XrayModelMetadata;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.CustomRemoteModel;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.linkfirebase.FirebaseModelSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button upload_btn, predict_btn;
    private TextView tv;
    private Bitmap image;
    private InputImage img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imgv);
        upload_btn = (Button) findViewById(R.id.upload);
        predict_btn = (Button) findViewById(R.id.predict);
        tv = (TextView) findViewById(R.id.confidence);

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("xray-model_metadata.tflite")
                        // or .setAbsoluteFilePath(absolute file path to model file)
                        // or .setUri(URI to model file)
                        .build();

        CustomRemoteModel remoteModel =
                new CustomRemoteModel
                        .Builder(new FirebaseModelSource.Builder("xray-model-metadata").build())
                        .build();
        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        RemoteModelManager.getInstance().download(remoteModel, downloadConditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //success
                    }
                });

        RemoteModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {

                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        CustomImageLabelerOptions.Builder optionsBuilder;
                        if (isDownloaded) {
                            optionsBuilder = new CustomImageLabelerOptions.Builder(remoteModel);
                        } else {
                            optionsBuilder = new CustomImageLabelerOptions.Builder(localModel);
                        }
                        CustomImageLabelerOptions options = optionsBuilder
                                .setConfidenceThreshold(0.5f)
                                .setMaxResultCount(1)
                                .build();
                        ImageLabeler labeler = ImageLabeling.getClient(options);

                        upload_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, 200);

                            }
                        });

                        predict_btn.setOnClickListener(new View.OnClickListener() {
                            public List<Float> categories;

//                            @Override
//                            public void onClick(View v) {
//                                image = Bitmap.createScaledBitmap(image, 175, 175, true);
//                                img = InputImage.fromBitmap(image,0);
//                                labeler.process(img)
//                                        .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
//                                            @Override
//                                            public void onSuccess(List<ImageLabel> labels) {
//                                                for (ImageLabel label : labels) {
//                                                    String text = label.getText();
//                                                    float confidence = label.getConfidence();
//                                                    int index = label.getIndex();
//                                                    categories = new ArrayList<>();
//                                                    categories.add(confidence);
//
//                                                }
//
//                                                tv.setText("confidence: "+categories);
//
//
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//
//                                            }
//                                        });
//                            }

//  Local Prediction
                            @Override
                            public void onClick(View v) {
                                image = Bitmap.createScaledBitmap(image, 175, 175, true);
                                try {
                                    XrayModelMetadata model = XrayModelMetadata.newInstance(getApplicationContext());
                                    // Runs model inference and gets result.
                                    XrayModelMetadata.Outputs outputs = model.process(TensorImage.fromBitmap(image));
                                    List<Category> probability = outputs.getProbabilityAsCategoryList();
                                    tv.setText("confidence: "+ probability);
                                    // Releases model resources if no longer used.
                                    model.close();
                                } catch (IOException e) {
                                    // TODO Handle the exception
                                }
                            }

                            });
                    };
                });

    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            imageView.setImageURI(data.getData());
            Uri uri = data.getData();
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                image = toGrayScale(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Bitmap toGrayScale(Bitmap bmpOriginal) {

        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        bmpOriginal.recycle();
        return bmpGrayscale;
    }
}
