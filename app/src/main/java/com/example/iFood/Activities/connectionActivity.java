package com.example.iFood.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iFood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * This Activity is responsible for handling the connection of the user
 * if the user doesn't have internet connection he will be transferred here.
 */
public class connectionActivity extends AppCompatActivity {
    FloatingActionButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        btn = findViewById(R.id.btnRefresh);

        btn.setOnClickListener(v -> {
            Intent login = new Intent(connectionActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        });
    }
}
