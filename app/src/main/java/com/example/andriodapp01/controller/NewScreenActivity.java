package com.example.andriodapp01.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.andriodapp01.R;

public class NewScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_screen);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewScreenActivity.this, HomeActivity.class);
            // Optional: use finish() if you just want to go back
            startActivity(intent);
        });
    }
}
