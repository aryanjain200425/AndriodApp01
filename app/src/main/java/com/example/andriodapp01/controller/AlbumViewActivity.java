package com.example.andriodapp01.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.PhotoAdapter;
import com.example.andriodapp01.model.MovePhotoDialog;
import com.example.andriodapp01.model.Tag;
import com.example.andriodapp01.model.TagManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlbumViewActivity extends AppCompatActivity implements
        PhotoAdapter.OnPhotoActionListener,
        MovePhotoDialog.OnPhotoMoveListener {

    public static final String EXTRA_ALBUM_ID = "album_id";

    private RecyclerView photosRecyclerView;
    private TextView albumNameTextView;
    private TextView photoCountTextView;
    private TextView emptyAlbumTextView;
    private FloatingActionButton fabAddPhoto;

    private AlbumManager albumManager;
    private TagManager tagManager;
    private Album album;
    private PhotoAdapter photoAdapter;

    private ImageButton editAlbumNameButton;
    private ImageButton slideshowButton;
    private ImageButton btnBack;
    private ImageButton btnSearch;

    // Activity result launcher for selecting photos
    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedPhoto
    );

    // Add these fields after existing field declarations
    private boolean isSelectionMode = false;
    private Set<Photo> selectedPhotos = new HashSet<>();
    private MenuItem sortMenuItem;
    private MenuItem batchDeleteMenuItem;
    private MenuItem batchMoveMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        // Initialize managers
        albumManager = AlbumManager.getInstance(this);
        tagManager = TagManager.getInstance(this);

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

        // Initialize views
        albumNameTextView = findViewById(R.id.albumNameTextView);
        photoCountTextView = findViewById(R.id.photoCountTextView);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        emptyAlbumTextView = findViewById(R.id.emptyAlbumTextView);
        fabAddPhoto = findViewById(R.id.fabAddPhoto);

        editAlbumNameButton = findViewById(R.id.editAlbumNameButton);
        editAlbumNameButton.setOnClickListener(v -> showEditAlbumNameDialog());

        slideshowButton = findViewById(R.id.slideshowButton);
        slideshowButton.setOnClickListener(v -> startSlideshow());
        slideshowButton.setEnabled(!album.getPhotos().isEmpty());

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize and set up the search button
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> startSearchActivity());

        // Set up RecyclerView
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set album name in header
        albumNameTextView.setText(album.getName());

        // Set up photo count text
        updatePhotoCountText();

        // Set up UI
        updateAlbumInfo();

        // Set up click listener for add photo button
        fabAddPhoto.setOnClickListener(v -> {
            photoPickerLauncher.launch("image/*");
        });

        setupSortingMenu();
    }

    // Add method to start the SearchActivity
    private void startSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    // Add method to update photo count text
    private void updatePhotoCountText() {
        int photoCount = album.getPhotos().size();
        String photoCountText = photoCount + (photoCount == 1 ? " photo" : " photos");
        photoCountTextView.setText(photoCountText);
    }

    // Add this method to handle the slideshow button click
    private void startSlideshow() {
        if (album.getPhotos().isEmpty()) {
            Toast.makeText(this, "Add photos to view slideshow", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start slideshow activity
        SlideshowActivity.start(this, album.getId());
    }

    private void showEditAlbumNameDialog() {
        // Create an EditText for the dialog
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(album.getName()); // Pre-fill with current name
        input.setSelectAllOnFocus(true); // Select all text when dialog opens

        new MaterialAlertDialogBuilder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        // Update album name
                        album.setName(newName);

                        // Save changes
                        albumManager.saveAlbum(album);

                        // Update UI
                        albumNameTextView.setText(newName);

                        Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // We no longer need this method since we're using a dedicated back button
    // If you want to keep it for future toolbar use, you can leave it
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAlbumInfo() {
        // Set up adapter with photos
        List<Photo> photos = album.getPhotos();

        // Update photo count text
        updatePhotoCountText();

        if (photos.isEmpty()) {
            emptyAlbumTextView.setVisibility(View.VISIBLE);
            photosRecyclerView.setVisibility(View.GONE);
            // Update slideshow button state
            slideshowButton.setEnabled(false);
        } else {
            emptyAlbumTextView.setVisibility(View.GONE);
            photosRecyclerView.setVisibility(View.VISIBLE);
            // Update slideshow button state
            slideshowButton.setEnabled(true);

            if (photoAdapter == null) {
                photoAdapter = new PhotoAdapter(photos, this, this);
                photoAdapter.setCurrentAlbum(album);
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
        photoAdapter.removePhoto(photo);

        // Save changes
        albumManager.saveAlbum(album);

        // Update UI
        updateAlbumInfo();

        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddTag(Photo photo, int position) {
        // Show tag type selection dialog
        showTagTypeSelectionDialog(photo, position);
    }

    private void showTagTypeSelectionDialog(Photo photo, int position) {
        final String[] tagTypes = {"Person", "Location"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Tag")
                .setItems(tagTypes, (dialog, which) -> {
                    String tagType = which == 0 ? Tag.TYPE_PERSON : Tag.TYPE_LOCATION;
                    showTagInputDialog(photo, position, tagType);
                })
                .show();
    }

    private void showTagInputDialog(Photo photo, int position, String tagType) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_input, null);
        TextView prefixTextView = dialogView.findViewById(R.id.prefixTextView);
        EditText valueEditText = dialogView.findViewById(R.id.valueEditText);

        String prefix = tagType + ":";
        prefixTextView.setText(prefix);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add " + tagType.substring(0, 1).toUpperCase() + tagType.substring(1) + " Tag")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String value = valueEditText.getText().toString().trim();
                    if (!value.isEmpty()) {
                        // Create and add tag
                        Tag tag = tagManager.createTag(tagType, value);
                        photo.addTagId(tag.getId());
                        
                        // Save changes
                        albumManager.saveAlbum(album);
                        
                        // Update UI
                        photoAdapter.notifyItemChanged(position);
                        Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTagClick(String tagId, Photo photo) {
        // Get the tag
        Tag tag = tagManager.getTagById(tagId);
        if (tag != null) {
            // Show tag info
            Toast.makeText(this, "Tag: " + tag.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMovePhoto(Photo photo, int position) {
        // Show dialog to select destination album
        MovePhotoDialog dialog = MovePhotoDialog.newInstance(photo, album);
        dialog.show(getSupportFragmentManager(), "MovePhotoDialog");
    }

    @Override
    public void onPhotoMoved(Photo photo, Album sourceAlbum, Album destinationAlbum) {
        // Remove photo from source album
        sourceAlbum.removePhoto(photo);

        // Add photo to destination album
        destinationAlbum.addPhoto(photo);

        // Save both albums
        albumManager.saveAlbum(sourceAlbum);
        albumManager.saveAlbum(destinationAlbum);

        // Update UI
        updateAlbumInfo();

        Toast.makeText(this, "Photo moved to " + destinationAlbum.getName(), Toast.LENGTH_SHORT).show();
    }

    private void setupSortingMenu() {
        MaterialToolbar toolbar = findViewById(R.id.albumToolbar);
        toolbar.inflateMenu(R.menu.album_view_menu);
        Menu menu = toolbar.getMenu();
        
        sortMenuItem = menu.findItem(R.id.action_sort);
        batchDeleteMenuItem = menu.findItem(R.id.action_batch_delete);
        batchMoveMenuItem = menu.findItem(R.id.action_batch_move);
        
        sortMenuItem.setOnMenuItemClickListener(item -> {
            showSortDialog();
            return true;
        });
    }

    private void showSortDialog() {
        String[] sortOptions = {"Date (Newest First)", "Date (Oldest First)", "Tags (A-Z)", "Tags (Z-A)"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Sort Photos")
            .setItems(sortOptions, (dialog, which) -> {
                switch (which) {
                    case 0:
                        sortPhotosByDate(true);
                        break;
                    case 1:
                        sortPhotosByDate(false);
                        break;
                    case 2:
                        sortPhotosByTags(true);
                        break;
                    case 3:
                        sortPhotosByTags(false);
                        break;
                }
            })
            .show();
    }

    private void sortPhotosByDate(boolean newestFirst) {
        List<Photo> photos = album.getPhotos();
        if (newestFirst) {
            photos.sort((p1, p2) -> Long.compare(p2.getCreationDate(), p1.getCreationDate()));
        } else {
            photos.sort((p1, p2) -> Long.compare(p1.getCreationDate(), p2.getCreationDate()));
        }
        photoAdapter.updatePhotos(photos);
        albumManager.saveAlbum(album);
    }

    private void sortPhotosByTags(boolean ascending) {
        List<Photo> photos = album.getPhotos();
        photos.sort((p1, p2) -> {
            String tags1 = getPhotoTagsString(p1);
            String tags2 = getPhotoTagsString(p2);
            return ascending ? tags1.compareTo(tags2) : tags2.compareTo(tags1);
        });
        photoAdapter.updatePhotos(photos);
        albumManager.saveAlbum(album);
    }

    private String getPhotoTagsString(Photo photo) {
        StringBuilder tags = new StringBuilder();
        for (String tagId : photo.getTagIds()) {
            Tag tag = tagManager.getTagById(tagId);
            if (tag != null) {
                tags.append(tag.getValue());
            }
        }
        return tags.toString();
    }

    // Add batch operation methods
    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        selectedPhotos.clear();
        
        batchDeleteMenuItem.setVisible(isSelectionMode);
        batchMoveMenuItem.setVisible(isSelectionMode);
        photoAdapter.setSelectionMode(isSelectionMode);
        
        // Update FAB to show different icon in selection mode
        fabAddPhoto.setImageResource(isSelectionMode ? 
            R.drawable.ic_close : 
            R.drawable.ic_add_photo);
    }

    private void performBatchDelete() {
        if (selectedPhotos.isEmpty()) return;
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Selected Photos")
            .setMessage("Delete " + selectedPhotos.size() + " selected photos?")
            .setPositiveButton("Delete", (dialog, which) -> {
                for (Photo photo : selectedPhotos) {
                    album.removePhoto(photo);
                }
                albumManager.saveAlbum(album);
                updateAlbumInfo();
                toggleSelectionMode();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}