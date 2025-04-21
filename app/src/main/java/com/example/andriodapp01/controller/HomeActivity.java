package com.example.andriodapp01.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.AlbumAdapter;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView albumRecyclerView;
    private AlbumAdapter albumAdapter;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        albumRecyclerView = findViewById(R.id.albumRecyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        // Set up RecyclerView
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set up FAB click listener
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch CreateAlbumActivity when FAB is clicked
                Intent intent = new Intent(HomeActivity.this, CreateAlbumActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh album list when returning to this activity
        loadAlbums();
    }

    private void loadAlbums() {
        // Get albums from AlbumManager
        List<Album> albums = AlbumManager.getInstance(this).getAlbums();

        // Initialize adapter if needed or update existing one
        if (albumAdapter == null) {
            albumAdapter = new AlbumAdapter(albums, this);
            albumRecyclerView.setAdapter(albumAdapter);
        } else {
            albumAdapter.updateAlbums(albums);
            albumAdapter.notifyDataSetChanged();
        }
    }
}