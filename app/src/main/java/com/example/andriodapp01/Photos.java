package com.example.andriodapp01;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Photos extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        // Example logic
        TextView textView = findViewById(R.id.welcomeText);
        textView.setText("Welcome to Photos App");
    }
}
