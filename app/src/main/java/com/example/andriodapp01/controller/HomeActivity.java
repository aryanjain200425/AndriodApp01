package com.example.andriodapp01.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.AlbumAdapter;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements AlbumAdapter.OnAlbumActionListener {

    private RecyclerView albumRecyclerView;
    private AlbumAdapter albumAdapter;
    private FloatingActionButton fabAdd;
    private ImageButton btnSearch;
    private AlbumManager albumManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize album manager
        albumManager = AlbumManager.getInstance(this);

        // Initialize views
        albumRecyclerView = findViewById(R.id.albumRecyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        btnSearch = findViewById(R.id.btnSearch);

        // Set up RecyclerView
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set up FAB click listener
        fabAdd.setOnClickListener(v -> {
            // Launch CreateAlbumActivity when FAB is clicked
            Intent intent = new Intent(HomeActivity.this, CreateAlbumActivity.class);
            startActivity(intent);
        });

        // Set up Search button click listener
        btnSearch.setOnClickListener(v -> {
            // Launch SearchActivity when Search button is clicked
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh album list when returning to this activity
        loadAlbums();
    }

    @Override
    public void onAlbumClick(Album album) {
        // Navigate to AlbumViewActivity
        Intent intent = new Intent(this, AlbumViewActivity.class);
        intent.putExtra(AlbumViewActivity.EXTRA_ALBUM_ID, album.getId());
        startActivity(intent);
    }

    private void loadAlbums() {
        // Get albums from AlbumManager
        List<Album> albums = albumManager.getAlbums();

        // Initialize adapter if needed or update existing one
        if (albumAdapter == null) {
            albumAdapter = new AlbumAdapter(albums, this, this); // Pass 'this' as the listener
            albumRecyclerView.setAdapter(albumAdapter);
        } else {
            albumAdapter.updateAlbums(albums);
        }
    }

    @Override
    public void onAlbumDelete(Album album) {
        // Show confirmation dialog before deleting
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete the album \"" + album.getName() + "\"? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the album
                    deleteAlbum(album);
                })
                .show();
    }

    private void deleteAlbum(Album album) {
        // Delete from storage using AlbumManager
        albumManager.deleteAlbum(album.getId());

        // Update UI using adapter method
        albumAdapter.removeAlbum(album);

        // Show confirmation to user
        Toast.makeText(this, "Album \"" + album.getName() + "\" deleted", Toast.LENGTH_SHORT).show();
    }
}