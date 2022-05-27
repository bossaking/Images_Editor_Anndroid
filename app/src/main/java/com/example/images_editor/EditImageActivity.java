package com.example.images_editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.uvstudio.him.photofilterlibrary.PhotoFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditImageActivity extends AppCompatActivity implements FiltersAdapter.FilterClickListener {

    ImageView imageToEditView;
    Bitmap editedImage;
    Bitmap actualImage;
    PhotoFilter photoFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("Image");

        imageToEditView = findViewById(R.id.imageToEditView);

        try {
            editedImage = getBitmapFromUri(getContentResolver(), uri);
            if (editedImage.getByteCount() >= 1000000) {
                editedImage = Bitmap.createScaledBitmap(
                        editedImage, editedImage.getWidth() / 2, editedImage.getHeight() / 2, false);
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            String path = getRealPathFromURI(uri);
            ExifInterface ei = new ExifInterface(path);
            int ex = ExifInterface.ORIENTATION_ROTATE_90;
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    editedImage = rotateBitmap(editedImage, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    editedImage = rotateBitmap(editedImage, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    editedImage = rotateBitmap(editedImage, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        actualImage = editedImage;
        imageToEditView.setImageBitmap(editedImage);
        photoFilter = new PhotoFilter();

        RecyclerView recyclerView = findViewById(R.id.filtersRecyclerView);
        Bitmap bmp = scaleImage(editedImage);
        FiltersAdapter adapter = new FiltersAdapter(bmp, photoFilter, getApplicationContext(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            savePhotoToStorage(actualImage);
            Toast.makeText(this, "Zapisano", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
        switch (position) {
            case 0:
                actualImage = this.editedImage;
                imageToEditView.setImageBitmap(actualImage);
                break;
            case 1:
                actualImage = photoFilter.one(this, this.editedImage);
                imageToEditView.setImageBitmap(actualImage);
                break;
            case 2:
                actualImage = photoFilter.two(this, this.editedImage);
                imageToEditView.setImageBitmap(actualImage);
                break;
            case 3:
                actualImage = photoFilter.three(this, this.editedImage);
                imageToEditView.setImageBitmap(actualImage);
                break;
            case 4:
                actualImage = photoFilter.four(this, this.editedImage);
                imageToEditView.setImageBitmap(actualImage);
                break;
            case 5:
                actualImage = photoFilter.five(this, this.editedImage);
                imageToEditView.setImageBitmap(actualImage);
                break;
        }
    }

    public Bitmap scaleImage(Bitmap bmp) {
        float aspectRatio = bmp.getWidth() /
                (float) bmp.getHeight();
        int width = 256;
        int height = Math.round(width / aspectRatio);
        return Bitmap.createScaledBitmap(
                bmp, width, height, false);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(editedImage, 0, 0, editedImage.getWidth(), editedImage.getHeight(), matrix, true);
    }

    public void savePhotoToStorage(Bitmap bitmap) throws Exception {

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";

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
    }
}