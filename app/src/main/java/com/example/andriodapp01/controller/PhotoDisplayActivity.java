package com.example.andriodapp01.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.Tag;
import com.example.andriodapp01.model.TagManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class PhotoDisplayActivity extends AppCompatActivity {
    public static final String EXTRA_ALBUM_ID = "album_id";
    public static final String EXTRA_PHOTO_POSITION = "photo_position";

    private ImageView photoImageView;
    private ChipGroup tagChipGroup;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnBack;

    private AlbumManager albumManager;
    private TagManager tagManager;
    private Album album;
    private int currentPosition;
    private List<Photo> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        // Initialize managers
        albumManager = AlbumManager.getInstance(this);
        tagManager = TagManager.getInstance(this);

        // Get album and photo position from intent
        String albumId = getIntent().getStringExtra(EXTRA_ALBUM_ID);
        currentPosition = getIntent().getIntExtra(EXTRA_PHOTO_POSITION, 0);

        if (albumId != null) {
            album = albumManager.getAlbumById(albumId);
            photos = album.getPhotos();
        }

        if (album == null || photos.isEmpty()) {
            Toast.makeText(this, "Photo not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        photoImageView = findViewById(R.id.photoImageView);
        tagChipGroup = findViewById(R.id.tagChipGroup);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // Set click listeners
        btnPrevious.setOnClickListener(v -> showPreviousPhoto());
        btnNext.setOnClickListener(v -> showNextPhoto());
        btnBack.setOnClickListener(v -> finish());

        // Display initial photo
        displayCurrentPhoto();
        updateNavigationButtons();
    }

    private void displayCurrentPhoto() {
        Photo currentPhoto = photos.get(currentPosition);
        
        // Load photo into ImageView
        photoImageView.setImageURI(currentPhoto.getUri());

        // Clear and update tags
        tagChipGroup.removeAllViews();
        List<String> tagIds = currentPhoto.getTagIds();
        if (tagIds != null) {
            for (String tagId : tagIds) {
                Tag tag = tagManager.getTagById(tagId);
                if (tag != null) {
                    addTagChip(tag);
                }
            }
        }
    }

    private void addTagChip(Tag tag) {
        Chip chip = new Chip(this);
        chip.setText(tag.getName());
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.white);
        tagChipGroup.addView(chip);
    }

    private void showPreviousPhoto() {
        if (currentPosition > 0) {
            currentPosition--;
            displayCurrentPhoto();
            updateNavigationButtons();
        }
    }

    private void showNextPhoto() {
        if (currentPosition < photos.size() - 1) {
            currentPosition++;
            displayCurrentPhoto();
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        btnPrevious.setEnabled(currentPosition > 0);
        btnNext.setEnabled(currentPosition < photos.size() - 1);
    }
}
