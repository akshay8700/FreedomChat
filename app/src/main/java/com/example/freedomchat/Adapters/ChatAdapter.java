package com.example.freedomchat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freedomchat.Models.MessageModel;
import com.example.freedomchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter {

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

        if(holder.getClass() == senderViewHolder.class) {
            ((senderViewHolder)holder).senderText.setText(messageModel.getMessage());
            ((senderViewHolder) holder).senderTime.setText("demo");

            DateFormat formatter = new SimpleDateFormat("hh:mm aa");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateFormatted = formatter.format(messageModel.getTimestamp());

            Log.i("Akku", "Time: " + dateFormatted);

            // if its a photo from sender, Show photo to
            // friend and Me in simple words with this code i m sending photos
            if(messageModel.getMessage().equals("Photo")) {
                ((senderViewHolder) holder).photo.setVisibility(View.VISIBLE);
                ((senderViewHolder) holder).senderText.setVisibility(View.INVISIBLE);
                Picasso.get().load(messageModel.getImageUrl()).placeholder(R.drawable.ic_twotone_access_time_24).into(((senderViewHolder) holder).photo);
            }
        }
        else{
            ((recieverViewHolder)holder).recieverText.setText(messageModel.getMessage());
            ((recieverViewHolder) holder).recieverTime.setText("Donut");

            if(messageModel.getMessage().equals("Photo")) {
                ((recieverViewHolder) holder).receiverPhoto.setVisibility(View.VISIBLE);
                ((recieverViewHolder) holder).recieverText.setVisibility(View.INVISIBLE);
                Picasso.get().load(messageModel.getImageUrl()).placeholder(R.drawable.ic_twotone_access_time_24).into(((recieverViewHolder) holder).receiverPhoto);
            }
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
        public ImageView receiverPhoto;

        public recieverViewHolder(@NonNull View itemView) {
            super(itemView);

            recieverText = itemView.findViewById(R.id.recieverText);
            recieverTime = itemView.findViewById(R.id.recieverTime);
            receiverPhoto = itemView.findViewById(R.id.receiverPhoto);
        }
    }

    public class senderViewHolder extends RecyclerView.ViewHolder {

        public TextView senderText;
        public TextView senderTime;
        public ImageView photo;

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            photo      = itemView.findViewById(R.id.photo);
        }
    }
}
