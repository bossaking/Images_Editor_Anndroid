package com.example.images_editor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class EditImageActivity extends AppCompatActivity {

    ImageView imageToEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("Image");

        imageToEditView = findViewById(R.id.imageToEditView);
        imageToEditView.setImageURI(uri);
    }
}