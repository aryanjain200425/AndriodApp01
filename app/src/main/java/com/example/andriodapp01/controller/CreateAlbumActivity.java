package com.example.andriodapp01.controller;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.andriodapp01.R;
import com.example.andriodapp01.model.SelectedPhotosAdapter;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.AlbumManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateAlbumActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_MEDIA_IMAGES = 2;
    private static final int MAX_PHOTOS = 20;
    private static final int MIN_ALBUM_NAME_LENGTH = 3;
    private ProgressDialog progressDialog;
    private ItemTouchHelper itemTouchHelper;

    private EditText albumNameInput;
    private RecyclerView selectedPhotosRecyclerView;
    private Button addPhotosButton;
    private Button saveAlbumButton;

    private List<Photo> selectedPhotos = new ArrayList<>();
    private List<Uri> selectedPhotoUris = new ArrayList<>(); // Store URIs temporarily
    private SelectedPhotosAdapter selectedPhotosAdapter;
    private boolean isSaving = false;

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

        setupDragAndDrop();
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
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        int remainingSlots = MAX_PHOTOS - selectedPhotoUris.size();
        if (remainingSlots <= 0) {
            Toast.makeText(this, "Maximum photo limit reached", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, remainingSlots);
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

    private boolean validateInput() {
        String albumName = albumNameInput.getText().toString().trim();

        if (albumName.length() < MIN_ALBUM_NAME_LENGTH) {
            albumNameInput.setError("Album name must be at least " + MIN_ALBUM_NAME_LENGTH + " characters");
            return false;
        }

        if (!albumName.matches("^[a-zA-Z0-9\\s]+$")) {
            albumNameInput.setError("Album name can only contain letters, numbers and spaces");
            return false;
        }

        if (selectedPhotoUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one photo", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveAlbum() {
        if (!validateInput() || isSaving) {
            return;
        }

        isSaving = true;
        String albumName = albumNameInput.getText().toString().trim();

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating album...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create album in background
        new Thread(() -> {
            Album album = new Album(albumName);

            // Save photos
            int total = selectedPhotoUris.size();
            int current = 0;

            for (Uri uri : selectedPhotoUris) {
                current++;
                final int progress = current;

                runOnUiThread(() ->
                        progressDialog.setMessage("Saving photo " + progress + " of " + total));

                Photo photo = new Photo();
                if (photo.savePhotoFromUri(this, uri)) {
                    album.addPhoto(photo);
                }
            }

            // Save album
            AlbumManager.getInstance(this).saveAlbum(album);

            // Update UI on main thread
            runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(this, "Album created successfully", Toast.LENGTH_SHORT).show();
                isSaving = false;
                finish();
            });
        }).start();
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT |
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                 @NonNull RecyclerView.ViewHolder viewHolder,
                                 @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(selectedPhotoUris, fromPosition, toPosition);
                selectedPhotosAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(selectedPhotosRecyclerView);
    }

    private void showImagePreview(Uri imageUri) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Glide.with(this)
                .load(imageUri)
                .into(imageView);

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(imageView);
        dialog.show();
    }
}