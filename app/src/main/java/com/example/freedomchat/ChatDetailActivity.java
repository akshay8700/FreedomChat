package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    String recieverToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //Getting userId userName and UserPic data from UsersAdapter with the help of intent
        //When user clicked on any chat from chatting list the rv will send data of that specific user into chatdetail activity
        final String senderID = auth.getUid();
        String receiverID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        String userPic = getIntent().getStringExtra("UserPic");

        // getting receiver token for notification
        database.getReference().child("Users").child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                recieverToken = users.getUserToken();

                Log.i("userToken", "receiverToken: " + recieverToken);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //Now setting that data in profile and name section and others
        binding.userName.setText(userName);
        Picasso.get().load(userPic).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.userPic);

        //When press back go to the chat list
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //Setting up for RV Defining list and adapter
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiverID);
        binding.rvChat.setAdapter(chatAdapter);

        //Setting up for RV setting layout in RV
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvChat.setLayoutManager(layoutManager);

        //Variable for giving unique id in Firebase for sender and reciever messages group
        final String senderRoom = senderID + receiverID;
        final String recieverRoom = receiverID + senderID;

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

        // When user click on send we will send that message into firebase database
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etSendMessage.getText().toString();
                final MessageModel model = new MessageModel(senderID, message);
                model.setTimestamp(new Date().getTime());
                binding.etSendMessage.setText("");

                database.getReference().child("chats")
                        .child(senderRoom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(recieverRoom)
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


                String notTitle = userName;
                String token = recieverToken;
                Context context = getApplicationContext();
                Activity mActivity = ChatDetailActivity.this;

                sendNotification(notTitle, message, token, context, mActivity);
            }
        });

        Log.i("NotiDetails", "UserName: " + userName);
    }

    // When sender click on send button receiver will receive notification with this method
    public static void sendNotification(String title, String message, String token, Context context, Activity mActivity) {
        if (title != null && !title.isEmpty() && !message.isEmpty() && !token.isEmpty()) {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token , title, message, context, mActivity);

            notificationsSender.SendNotifications();

            Toast.makeText(context, "Send to one is pressed!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Please fill title content and token", Toast.LENGTH_SHORT).show();
        }
    }
}