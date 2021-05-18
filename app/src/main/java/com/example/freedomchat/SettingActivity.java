package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.freedomchat.Models.Users;
import com.example.freedomchat.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        //Back button
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //When click on plus button or we can say add button
        //Update profile button or Add pic
        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        //When open settings window show updated user data in profile and name and about and others
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.userProfile);
                        binding.typenameET.setText(users.getUserName());
                        binding.typeAboutET.setText(users.getAbout());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //Save Name and About when click on save button
        binding.saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.typenameET.getText().toString();
                String about = binding.typeAboutET.getText().toString();

                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", name);
                obj.put("about", about);

                database.getReference().child("Users").child(auth.getUid()).updateChildren(obj);
                //Backup
                database.getReference().child("UsersBackup").child(auth.getUid()).updateChildren(obj);
                Toast.makeText(SettingActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //When we get image from gallery, Here now set image in profile and send to Firebase storage and many more
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data.getData() != null){
            Uri uri = data.getData();

            binding.userProfile.setImageURI(uri);
            final StorageReference reference = storage.getReference().child("profile_pictures").child(auth.getUid());
            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Send profile pic from firebase storage to firebase users data
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(auth.getUid()).child("profilePic").setValue(uri.toString());
                            //Backup
                            database.getReference().child("UsersBackup").child(auth.getUid()).child("profilePic").setValue(uri.toString());
                            Toast.makeText(SettingActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}