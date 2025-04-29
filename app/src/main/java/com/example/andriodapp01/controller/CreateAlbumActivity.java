package com.example.andriodapp01.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.SelectedPhotosAdapter;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.AlbumManager;

import java.util.ArrayList;
import java.util.List;

public class CreateAlbumActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_MEDIA_IMAGES = 2;

    private EditText albumNameInput;
    private RecyclerView selectedPhotosRecyclerView;
    private Button addPhotosButton;
    private Button saveAlbumButton;

    private List<Photo> selectedPhotos = new ArrayList<>();
    private List<Uri> selectedPhotoUris = new ArrayList<>(); // Store URIs temporarily
    private SelectedPhotosAdapter selectedPhotosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        // Initialize views
        albumNameInput = findViewById(R.id.albumNameInput);
        selectedPhotosRecyclerView = findViewById(R.id.selectedPhotosRecyclerView);
        addPhotosButton = findViewById(R.id.addPhotosButton);
        saveAlbumButton = findViewById(R.id.saveAlbumButton);

        // Setup RecyclerView with grid layout
        selectedPhotosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        selectedPhotosAdapter = new SelectedPhotosAdapter(selectedPhotoUris, this);
        selectedPhotosRecyclerView.setAdapter(selectedPhotosAdapter);

        // Set up button listeners
        addPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickImages();
            }
        });

        saveAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlbum();
            }
        });
    }

    private void checkPermissionAndPickImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_READ_MEDIA_IMAGES);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        // Use the modern Photo Picker for Android 14
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // Multiple images selected
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedPhotoUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    // Single image selected
                    Uri imageUri = data.getData();
                    selectedPhotoUris.add(imageUri);
                }
                selectedPhotosAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveAlbum() {
        String albumName = albumNameInput.getText().toString().trim();

        if (albumName.isEmpty()) {
            Toast.makeText(this, "Please enter an album name", Toast.LENGTH_SHORT).show();
            return;
        }

        AlbumManager albumManager = AlbumManager.getInstance(this);
        List<Album> existing = albumManager.getAlbums();
        for (Album a : existing) {
            if (a.getName().equalsIgnoreCase(albumName)) {
                Toast.makeText(this, "Album name already taken", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (selectedPhotoUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        Toast.makeText(this, "Saving album...", Toast.LENGTH_SHORT).show();

        // Create new album
        Album album = new Album(albumName);

        // Save photos to local storage and add to album
        for (Uri uri : selectedPhotoUris) {
            Photo photo = new Photo(); // Creates a new photo with unique ID
            if (photo.savePhotoFromUri(this, uri)) {
                album.addPhoto(photo);
            }
        }

        // Save album using AlbumManager
        AlbumManager.getInstance(this).saveAlbum(album);

        Toast.makeText(this, "Album created successfully", Toast.LENGTH_SHORT).show();

        // Return to HomeActivity
        finish();
    }
}