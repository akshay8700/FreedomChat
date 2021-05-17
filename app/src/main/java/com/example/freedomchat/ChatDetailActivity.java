package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.freedomchat.Adapters.ChatAdapter;
import com.example.freedomchat.Models.MessageModel;
import com.example.freedomchat.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

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
        String recieverID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        String userPic = getIntent().getStringExtra("UserPic");

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
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, recieverID);
        binding.rvChat.setAdapter(chatAdapter);

        //Setting up for RV setting layout in RV
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvChat.setLayoutManager(layoutManager);

        //Variable for giving unique id in Firebase for sender and reciever messages group
        final String senderRoom = senderID + recieverID;
        final String recieverRoom = recieverID + senderID;

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

        //When user click on send we will send that message into firebase database
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
            }
        });
    }
}