package com.example.images_editor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getImageFromStorageButton = findViewById(R.id.getImageFromStorageButton);
        getImageFromStorageButton.setOnClickListener(v -> {
            getImageFromStorage();
        });

        Button openCameraButton = findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(v -> {
            checkCameraPermissions();
            takePhoto();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void getImageFromStorage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        getImageFromStorageResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> getImageFromStorageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = Objects.requireNonNull(result.getData()).getData();
                    try {
                        openEditorActivity(imageUri);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> takePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bitmap image = Objects.requireNonNull(result.getData()).getParcelableExtra("data");
                    try {
                        Uri uri = savePhotoToStorage(image);
                        openEditorActivity(uri);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    private void openEditorActivity(Uri uri) {
        Intent intent = new Intent(this, EditImageActivity.class);
        intent.putExtra("Image", uri);
        startActivity(intent);
    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }
    }

    public Uri savePhotoToStorage(Bitmap bitmap) throws Exception {

        String fileName = getCurrentTimeString() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        } else {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(directory, fileName);
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        OutputStream output = getContentResolver().openOutputStream(uri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

        return uri;
    }

    public String getCurrentTimeString() {
        int yyyy = Calendar.getInstance().get(Calendar.YEAR);
        int MM = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int hh = Calendar.getInstance().get(Calendar.HOUR);
        int mm = Calendar.getInstance().get(Calendar.MINUTE);
        int ss = Calendar.getInstance().get(Calendar.SECOND);

        return yyyy + "-" + MM + "-" + dd + " " + hh + ":" + mm + ":" + ss;
    }
}