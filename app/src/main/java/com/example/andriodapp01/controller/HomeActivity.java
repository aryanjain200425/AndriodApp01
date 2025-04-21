package com.example.andriodapp01.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.andriodapp01.R;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome to Photos App");

        Button goToNewScreenBtn = findViewById(R.id.goToNewScreenBtn);
        goToNewScreenBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewScreenActivity.class);
            startActivity(intent);
        });
    }
}
