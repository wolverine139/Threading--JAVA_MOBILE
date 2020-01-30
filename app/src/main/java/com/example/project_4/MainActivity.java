package com.example.project_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity {

    private Button continous;
    private Button guess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continous = findViewById(R.id.continuous_bt);
        guess = findViewById(R.id.guess_bt);
        final Intent i = new Intent(this, gridview.class);
        continous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("key","Continuous");
                startActivity(i);
            }
        });

        guess.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("key","Guess-by-Guess");
                startActivity(i);
            }
        });
    }
}
