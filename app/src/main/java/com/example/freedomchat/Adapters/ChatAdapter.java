package com.example.freedomchat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freedomchat.Models.MessageModel;
import com.example.freedomchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{

    public ArrayList<MessageModel> list;
    public Context context;
    String recieverID;

    int SENDER_VIEWTYPE = 1;
    int RECIEVER_VIEWTYPE = 2;

    public ChatAdapter(ArrayList<MessageModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> list, Context context, String recieverID) {
        this.list = list;
        this.context = context;
        this.recieverID = recieverID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == SENDER_VIEWTYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new senderViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever, parent, false);
            return new recieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEWTYPE;
        }
        else{
            return RECIEVER_VIEWTYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = list.get(position);

        // Send notification to friends


        // Delete message on long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String sender = FirebaseAuth.getInstance().getUid() + recieverID;
                                database.getReference().child("chats").child(sender)
                                        .child(messageModel.getMessageID()).setValue(null);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                return false;
            }
        });

        if(holder.getClass() == senderViewHolder.class){
            ((senderViewHolder)holder).senderText.setText(messageModel.getMessage());
        }
        else{
            ((recieverViewHolder)holder).recieverText.setText(messageModel.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class recieverViewHolder extends RecyclerView.ViewHolder
    {

        public TextView recieverText;
        public TextView recieverTime;

        public recieverViewHolder(@NonNull View itemView) {
            super(itemView);

            recieverText = itemView.findViewById(R.id.recieverText);
            recieverTime = itemView.findViewById(R.id.recieverTime);
        }
    }

    public class senderViewHolder extends RecyclerView.ViewHolder {

        public TextView senderText;
        public TextView senderTime;

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }
}
