package com.example.andriodapp01.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.PhotoAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlbumViewActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoActionListener {

    public static final String EXTRA_ALBUM_ID = "album_id";

    private RecyclerView photosRecyclerView;
    private TextView albumNameTextView;
    private TextView photoCountTextView;
    private TextView emptyAlbumTextView;
    private FloatingActionButton fabAddPhoto;

    private AlbumManager albumManager;
    private Album album;
    private PhotoAdapter photoAdapter;

    // Activity result launcher for selecting photos
    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedPhoto
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        // Initialize album manager
        albumManager = AlbumManager.getInstance(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Album");
        }

        // Initialize views
        albumNameTextView = findViewById(R.id.albumNameTextView);
        photoCountTextView = findViewById(R.id.photoCountTextView);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        emptyAlbumTextView = findViewById(R.id.emptyAlbumTextView);
        fabAddPhoto = findViewById(R.id.fabAddPhoto);

        // Set up RecyclerView
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Get album ID from intent
        String albumId = getIntent().getStringExtra(EXTRA_ALBUM_ID);
        if (albumId != null) {
            album = albumManager.getAlbumById(albumId);
        }

        // Check if album exists
        if (album == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up UI
        updateAlbumInfo();

        // Set up click listener for add photo button
        fabAddPhoto.setOnClickListener(v -> {
            photoPickerLauncher.launch("image/*");
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAlbumInfo() {
        albumNameTextView.setText(album.getName());

        int photoCount = album.getPhotoCount();
        String photoCountText = photoCount + " " + (photoCount == 1 ? "photo" : "photos");
        photoCountTextView.setText(photoCountText);

        // Set up adapter with photos
        List<Photo> photos = album.getPhotos();

        if (photos.isEmpty()) {
            emptyAlbumTextView.setVisibility(View.VISIBLE);
            photosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyAlbumTextView.setVisibility(View.GONE);
            photosRecyclerView.setVisibility(View.VISIBLE);

            if (photoAdapter == null) {
                photoAdapter = new PhotoAdapter(photos, this, this);
                photosRecyclerView.setAdapter(photoAdapter);
            } else {
                photoAdapter.updatePhotos(photos);
            }
        }
    }

    private void handleSelectedPhoto(Uri photoUri) {
        if (photoUri != null) {
            // Create new photo
            Photo photo = new Photo();

            // Save photo from URI
            boolean saved = photo.savePhotoFromUri(this, photoUri);

            if (saved) {
                // Add photo to album
                album.addPhoto(photo);

                // Save album with new photo
                albumManager.saveAlbum(album);

                // Update UI
                updateAlbumInfo();

                Toast.makeText(this, "Photo added to album", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPhotoClick(Photo photo, int position) {
        // Future: Open photo detail view
        Toast.makeText(this, "Photo clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPhotoDelete(Photo photo, int position) {
        // Remove from album
        album.removePhoto(photo);

        // Update adapter
        photoAdapter.removePhoto(position);

        // Save changes
        albumManager.saveAlbum(album);

        // Update UI
        updateAlbumInfo();

        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddTag(Photo photo, int position) {
        // Show dialog to enter tag
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Tag")
                .setMessage("Enter a tag for this photo:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String tag = input.getText().toString().trim();
                    if (!tag.isEmpty()) {
                        // Add tag to photo
                        photo.addTagId(tag);

                        // Save album with updated photo
                        albumManager.saveAlbum(album);

                        // Update UI - this will make the chip group visible
                        photoAdapter.notifyItemChanged(position);

                        Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTagClick(String tagId, Photo photo) {
        // Future: Filter photos by tag or show tag details
        Toast.makeText(this, "Tag: " + tagId, Toast.LENGTH_SHORT).show();
    }
}