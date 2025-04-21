package com.example.andriodapp01.controller;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.andriodapp01.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private FrameLayout albumContainer;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Header (text is already set via @string/your_albums in XML)
        TextView headerText = findViewById(R.id.headerText);

        // Container where you'll later inflate or bind your RecyclerView/album grid
        albumContainer = findViewById(R.id.albumContainer);

        // "+" button in bottom‑right
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> {
            // TODO: launch "add new album" flow
        });
    }
}
