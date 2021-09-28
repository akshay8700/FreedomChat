package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.freedomchat.databinding.ActivitySendPhotoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.util.Calendar;

public class sendPhotoActivity extends AppCompatActivity {
    ActivitySendPhotoBinding binding;
    FirebaseStorage storage;

    Bitmap capturedImageBitmap = null;
    FileInputStream inputStream = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();

        // Getting our image after capture
        String filename = getIntent().getStringExtra("capturedImage");
        try {
            inputStream= this.openFileInput(filename);
            capturedImageBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
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

        // Clicked on send button
        binding.sendPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                // making storagereference its like a making emty folers in firebase before storing anything
                StorageReference storageReference = storage.getReference()
                        .child("Captured").child("Captured" + calendar.getTimeInMillis());

                storageReference.putStream(inputStream).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        Toast.makeText(sendPhotoActivity.this, "Uploaded Sucessfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}