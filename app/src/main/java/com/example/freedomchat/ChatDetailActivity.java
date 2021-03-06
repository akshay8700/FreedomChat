package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.freedomchat.Adapters.ChatAdapter;
import com.example.freedomchat.Models.MessageModel;
import com.example.freedomchat.Models.Users;
import com.example.freedomchat.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    String receiverToken;
    String myName;
    String senderID;
    String currentPhotoPath;

    String senderRoom;
    String receiverRoom;

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private static final int MY_CAMERA_INTENT_REQUEST_CODE = 123;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth     = FirebaseAuth.getInstance();
        storage  = FirebaseStorage.getInstance();

        //Getting userId userName and UserPic data from UsersAdapter with the help of intent
        //When user clicked on any chat from chatting list the rv will send data of that specific user into chatdetail activity
        senderID              = auth.getUid();
        String receiverID     = getIntent().getStringExtra("userID");
        String userName       = getIntent().getStringExtra("userName");
        String userPic        = getIntent().getStringExtra("UserPic");

        // Checking storage permission if not granted request from user
        requestStoragePermission();

        //Now setting that data in profile and name section and others
        binding.userName.setText(userName);
        Picasso.get().load(userPic).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.userPic);

        //Setting up for RV Defining list and adapter
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiverID);
        binding.rvChat.setAdapter(chatAdapter);

        //Setting up for RV setting layout in RV
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show from down when open messages
        binding.rvChat.setLayoutManager(layoutManager);

        //Variable for giving unique id in Firebase for sender and reciever messages group
        senderRoom = senderID + receiverID;
        receiverRoom = receiverID + senderID;

        getReceiverTokenForNotification(receiverID);
        getUserName();
        pressBack();
        showMessageOnDisplay(messageModels, chatAdapter);
        onSendClick();
        onUserNameClick(receiverID);
        onAttachmentClick();
        onClickCamera();
    }

    private void getReceiverTokenForNotification(String receiverID) {
        database.getReference().child("Users").child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                receiverToken = users.getUserToken();

                Log.i("userToken", "receiverToken: " + receiverToken);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getUserName() {
        // getting users name not friend name
        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                myName = users.getUserName();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void pressBack() {
        //When press back go to the chat list
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showMessageOnDisplay(ArrayList<MessageModel> messageModels, ChatAdapter chatAdapter) {
        //After user sended a message this code will show that message in chatting display means in recyclerView with chat bubble
        //Basically we are getting chat data from firebase to chatdetailactivity
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            model.setMessageID(dataSnapshot.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override

                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    // When user click on send we will send that message into firebase database
    public void onSendClick() {
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send time in the form of String
                String timeInString = "demoTime: 6:24PM";

                // Getting time from DateFormat
                DateFormat formatter = new SimpleDateFormat("hh:mm aa");
                String dateFormatted = formatter.format(new Date().getTime());
                timeInString = dateFormatted;
                // setting sender Message, Time, senderID into MessageModel
                String message = binding.etSendMessage.getText().toString();
                final MessageModel model = new MessageModel(senderID, message);
                model.setTimestamp(timeInString);

                binding.etSendMessage.setText("");
                // Sending MessageModel to firebase database on specific messages of all messages
                // In simple words Message id and Time details and Message text into firebase for every message by sender
                database.getReference().child("chats")
                        .child(senderRoom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                    }
                });

                // Send notification
                // Getting and setting all notification related data like title message token
                String notTitle = myName;
                String token = receiverToken;
                Context context = getApplicationContext();
                Activity mActivity = ChatDetailActivity.this;

                sendNotification(notTitle, message, token, context, mActivity);
            }
        });
    }
    // Attachment Button
    public void onAttachmentClick() {
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 38);
            }
        });
    }
    // When sender click on send button receiver will receive notification with this method
    public static void sendNotification(String title, String message, String token, Context context, Activity mActivity) {
        if (title != null && !title.isEmpty() && !message.isEmpty() && !token.isEmpty()) {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token , title, message, context, mActivity);

            notificationsSender.SendNotifications();
        }
        else {
            Toast.makeText(context, "Please fill title content and token", Toast.LENGTH_SHORT).show();
        }
    }
    // Show user about
    public void onUserNameClick(String receiverID) {
        binding.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, Friend_About.class);
                intent.putExtra("FriendID", receiverID);
                startActivity(intent);
            }
        });
    }
    // Intent results of Actions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Getting selected photo
        // Ex: sending photo to friend from gallery
        // checking request code for correct photo sender Intent that i created for getting pic from gallery
        if(requestCode == 38) {
            // Result should not null
            if(data != null) {
                // getting data from data if its not null its mean we are ready to upload it into firebase with timestamp and message unique push id
                if(data.getData() != null) {
                    // setting photo in variable from using it
                    Uri selectedPhotoURI = data.getData();
                    // getting time in mili with the help of calender class, For everytime unique name of photo when ever send
                    Calendar calendar = Calendar.getInstance();
                    // making storagereference its like a making emty folers in firebase before storing anything
                    StorageReference storageReference = storage.getReference()
                            .child("chats").child("AZ_" + calendar.getTimeInMillis());
                    // putting photo in firebase
                    storageReference.putFile(selectedPhotoURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            // now getting downloadable url of our photo
                            if(task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // our downloadable image path is now in variable
                                        String imagePath = uri.toString();
                                        // setting message information but for photo
                                        String message = binding.etSendMessage.getText().toString();
                                        final MessageModel model = new MessageModel(senderID, message);
                                        // Timestamp setup
                                        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                                        String timeInAmPm = dateFormat.format(new Date().getTime());
                                        model.setTimestamp(timeInAmPm);
                                        model.setImageUrl(imagePath);
                                        model.setMessage("Photo");
                                        binding.etSendMessage.setText("");

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .push()
                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .push()
                                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });

                                        // Send notification
                                        // Getting and setting all notification related data like title message token
                                        String notTitle = myName;
                                        String token = receiverToken;
                                        Context context = getApplicationContext();
                                        Activity mActivity = ChatDetailActivity.this;

                                        sendNotification(notTitle, message, token, context, mActivity);
                                    }
                                });

                            }
                        }
                    });
                }
            }
        }

            // If we get requestCode of camera means photo is clicked by camera our intent is working fine and getted image from camera
            if(requestCode == MY_CAMERA_INTENT_REQUEST_CODE) {
                // Result should not null
                if (data != null) {
                    Calendar calendar = Calendar.getInstance();
                    // making storagereference its like a making emty folders in firebase before storing anything
                    StorageReference storageReference = storage.getReference()
                            .child("Captured").child("Captured" + calendar.getTimeInMillis());

                    storageReference.putFile(Uri.parse(currentPhotoPath)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            Toast.makeText(ChatDetailActivity.this, "Uploaded Sucessfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    // Its just a result of dangerous permissions depend on user if user accepted or not we will add conditions on allow and denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // MY_WRITE_EXTERNAL_STORAGE_REQUEST_CODE is a var with unique number for our camera request so we can differentiate different permissions
        // When asking for permission we give a unique code for that permission just like Intent

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // capturing picture
                dispatchTakePictureIntent();
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_WRITE_EXTERNAL_STORAGE_REQUEST_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission GRANTED", Toast.LENGTH_SHORT).show();
                binding.sendImage.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Storage Permission DENIED", Toast.LENGTH_SHORT).show();
                binding.sendImage.setVisibility(View.INVISIBLE);
            }
        }
    }
    // Method to convert bitmap to uri, Pass context and bitmap and get uri of bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.freedomchat.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, MY_CAMERA_INTENT_REQUEST_CODE);
            }
        }
    }

    private void onClickCamera() {
        // Click photo/sendImage/Camera Icon
        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for saving any time of files in storage")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ChatDetailActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for capture image")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ChatDetailActivity.this,
                                    new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }
}