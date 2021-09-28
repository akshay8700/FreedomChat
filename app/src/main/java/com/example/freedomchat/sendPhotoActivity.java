package com.example.freedomchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.freedomchat.databinding.ActivitySendPhotoBinding;

import java.io.FileInputStream;

public class sendPhotoActivity extends AppCompatActivity {
    ActivitySendPhotoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Getting our image after capture
        Bitmap capturedImageBitmap = null;
        String filename = getIntent().getStringExtra("capturedImage");
        try {
            FileInputStream is = this.openFileInput(filename);
            capturedImageBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set image in ImageView
        binding.sendPhotoImageView.setImageBitmap(capturedImageBitmap);

        // Clicked on cross
        binding.crossImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(sendPhotoActivity.this, ChatDetailActivity.class);
                startActivity(intent);
            }
        });
    }
}