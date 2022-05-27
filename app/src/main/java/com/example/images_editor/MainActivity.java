package com.example.images_editor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePhotoResultLauncher.launch(intent);
            }
        }else{
            Toast.makeText(this, "LOL", Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<Intent> takePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        Uri uri = galleryAddPic();
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


String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Uri galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        return contentUri;
    }
}