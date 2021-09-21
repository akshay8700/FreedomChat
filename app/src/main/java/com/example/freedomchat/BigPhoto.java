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
import com.zolad.zoominimageview.ZoomInImageView;
import com.zolad.zoominimageview.ZoomInImageViewAttacher;

import org.jetbrains.annotations.NotNull;

public class BigPhoto extends AppCompatActivity {

    FirebaseDatabase database;
    ImageView bigPhoto;
    String senderImageURI = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_photo);

        bigPhoto = findViewById(R.id.bigUserPic);
        database = FirebaseDatabase.getInstance();

        // Adding zoom in and out function in imageview
        ZoomInImageViewAttacher zoomInImageViewAttacher = new ZoomInImageViewAttacher();
        zoomInImageViewAttacher.attachImageView(bigPhoto);

        // Getting receiverID
        // For viewing picture of friends
        Intent intent = getIntent();
        String receiverID = intent.getStringExtra("ReceiverID");

        // Getting image URI
        senderImageURI = intent.getStringExtra("InChatPhotoURI");


        // Full size image when click while chatting on image
        if(senderImageURI != null) {
            Picasso.get().load(senderImageURI).placeholder(R.drawable.image).into(bigPhoto);
        }

        // Viewing full size image from home page of chatting app, When clicked on friends profile images
        if(receiverID != null) {
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
        }
    } // Ending of onCreate();
} // Ending of class BigPhoto