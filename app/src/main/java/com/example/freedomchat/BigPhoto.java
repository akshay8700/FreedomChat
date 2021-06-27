package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.freedomchat.Models.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class BigPhoto extends AppCompatActivity {

    FirebaseDatabase database;
    ImageView bigPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_photo);

        bigPhoto = findViewById(R.id.bigUserPic);
        database = FirebaseDatabase.getInstance();

        // Getting receiverID
        // For viewing picture of friends
        Intent intent = getIntent();
        String receiverID = intent.getStringExtra("ReceiverID");

        database.getReference().child("Users").child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.ic_baseline_account_circle_24).into(bigPhoto);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    } // Ending of onCreate();
} // Ending of class BigPhoto