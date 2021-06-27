package com.example.freedomchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freedomchat.BigPhoto;
import com.example.freedomchat.ChatDetailActivity;
import com.example.freedomchat.Fragments.ChatsFragment;
import com.example.freedomchat.MainActivity;
import com.example.freedomchat.Models.Users;
import com.example.freedomchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    public ArrayList<Users> list;
    private Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_users, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.ic_baseline_account_circle_24).into(holder.userPic);
        holder.userName.setText(users.getUserName());

        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + users
                        .getUserId()).orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                holder.lastMessage.setText(dataSnapshot.child("message").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // if click on user at home screen go into user chat for chatting with him
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userID", users.getUserId());
                intent.putExtra("userName", users.getUserName());
                intent.putExtra("UserPic", users.getProfilePic());

                context.startActivity(intent);
            }
        });

        // Hold on profile pic for viewing profile
        holder.userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBigPhoto = new Intent(context, BigPhoto.class);

                // Send user id to BigPhoto activity for viewing photo from userID throw firebase
                intentBigPhoto.putExtra("ReceiverID", users.getUserId());
                context.startActivity(intentBigPhoto);

            } // End of onclick
        }); // holder.userPic.setOnClickListener

    } // End of onBindViewHolder

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userPic;
        TextView userName;
        TextView lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic = itemView.findViewById(R.id.userPic);
            userName = itemView.findViewById(R.id.userNameTV);
            lastMessage = itemView.findViewById(R.id.lastMessageTV);
        }
    }
}
