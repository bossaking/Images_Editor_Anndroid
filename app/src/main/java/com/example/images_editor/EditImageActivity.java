package com.example.images_editor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.uvstudio.him.photofilterlibrary.PhotoFilter;

import java.io.IOException;
import java.io.InputStream;

public class EditImageActivity extends AppCompatActivity implements FiltersAdapter.FilterClickListener {

    ImageView imageToEditView;
    Bitmap editedImage;
    PhotoFilter photoFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("Image");

        imageToEditView = findViewById(R.id.imageToEditView);
        imageToEditView.setImageURI(uri);
        try {
            editedImage = getBitmapFromUri(getContentResolver(), uri);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        photoFilter = new PhotoFilter();

//        Button test = findViewById(R.id.testButton);
//        test.setOnClickListener(v -> {
//            try {
//                Bitmap bitmap = getBitmapFromUri(this.getContentResolver(), uri);
//                imageToEditView.setImageBitmap(photoFilter.two(getApplicationContext(), bitmap));
//            } catch (IOException e) {
//                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

//        ImageView cat = findViewById(R.id.cat);
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat);
        RecyclerView recyclerView = findViewById(R.id.filtersRecyclerView);
        Bitmap bmp = scaleImage(editedImage);
        FiltersAdapter adapter = new FiltersAdapter(bmp, photoFilter, getApplicationContext(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
//        cat.setImageBitmap(bmp);
    }

    private Bitmap getBitmapFromUri(ContentResolver cr, Uri uri)
            throws IOException {
        InputStream input = cr.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return bitmap;
    }

    @Override
    public void onItemClick(int position) {
        switch (position){
            case 0:
                imageToEditView.setImageBitmap(this.editedImage);
                break;
            case 1:
                imageToEditView.setImageBitmap(photoFilter.one(this, this.editedImage));
                break;
            case 2:
                imageToEditView.setImageBitmap(photoFilter.two(this, this.editedImage));
                break;
            case 3:
                imageToEditView.setImageBitmap(photoFilter.three(this, this.editedImage));
                break;
            case 4:
                imageToEditView.setImageBitmap(photoFilter.four(this, this.editedImage));
                break;
            case 5:
                imageToEditView.setImageBitmap(photoFilter.five(this, this.editedImage));
                break;
        }
    }

    public Bitmap scaleImage(Bitmap bmp){
        float aspectRatio = bmp.getWidth() /
                (float) bmp.getHeight();
        int width = 256;
        int height = Math.round(width / aspectRatio);
        return Bitmap.createScaledBitmap(
                bmp, width, height, false);
    }
}