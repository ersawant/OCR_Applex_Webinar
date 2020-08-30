package com.applex.webinar.ocrapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText  ocrResult;
    private Button select;
    private ImageButton copy, share, search;
    private ConnectivityManager cm;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    String[] cameraPermission;
    String[] storagePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        preview = findViewById(R.id.preview);
        ocrResult = findViewById(R.id.result);
        copy = findViewById(R.id.copy);
        share = findViewById(R.id.share);
        search = findViewById(R.id.search);
        select = findViewById(R.id.select_image);

        cm = (ConnectivityManager) MainActivity.this.getSystemService(CONNECTIVITY_SERVICE);


        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                }
                else {
                    pickGallery();
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ocrResult.getText().toString();
                if(text.length()!=0){
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_TEXT,text);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i,"Share with"));

                }
                else {
                    Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ocrResult.getText().toString();
                if(text.length()!=0){
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Text",text);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(MainActivity.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ocrResult.getText().toString();
                if(text.length()!=0){
                    Search();
                }
                else{
                    Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void Search() {
        if(cm.getActiveNetworkInfo() != null) {
            String text = ocrResult.getText().toString();
            if (text.length() != 0) {
                Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
                i.putExtra(SearchManager.QUERY, text);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Please check your internet connection and try again...", Toast.LENGTH_SHORT).show();
        }

    }


    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"), IMAGE_PICK_GALLERY_CODE);
    }

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(MainActivity.this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
//            case CAMERA_REQUEST_CODE:
//                if(grantResults.length > 0){
//                    boolean cameraAccepted = grantResults[0] ==
//                            PackageManager.PERMISSION_GRANTED;
//                    boolean writeStorageAccepted = grantResults[0] ==
//                            PackageManager.PERMISSION_GRANTED;
//                    if(cameraAccepted && writeStorageAccepted){
//                        pickCamera();
//                    }
//                    else{
//                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickGallery();
                    }
                    else{
                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //////////////////////PREMISSIONS//////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            if(imageUri != null){
                preview.setImageURI(imageUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) preview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();

                if(textRecognizer.isOperational()){
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);

                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i = 0; i < items.size(); i++){
                        TextBlock myItem = items.valueAt(i);
                        stringBuilder.append(myItem.getValue());
                        if (i != items.size() - 1) {
                            stringBuilder.append("\n");
                        }
                    }
                    ocrResult.setText(stringBuilder.toString());
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void textExtractor(Uri uri) {
        preview.setImageURI(uri);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) preview.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!recognizer.isOperational()) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                if (i != items.size() - 1) {
                    sb.append("\n");
                }
            }
            ocrResult.setText(sb.toString());
        }
    }

}