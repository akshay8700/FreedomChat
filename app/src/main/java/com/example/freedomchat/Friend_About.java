package com.example.freedomchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.freedomchat.databinding.ActivityFriendAboutBinding;

public class Friend_About extends AppCompatActivity {

    ActivityFriendAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Getting intent
        Intent intent = getIntent();
        String friendID = intent.getStringExtra("FriendID");

        Toast.makeText(this, friendID, Toast.LENGTH_SHORT).show();
    }
}