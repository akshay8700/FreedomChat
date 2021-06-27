package com.example.freedomchat.Fragments;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.freedomchat.Adapters.UsersAdapter;
import com.example.freedomchat.MainActivity;
import com.example.freedomchat.Models.Users;
import com.example.freedomchat.R;
import com.example.freedomchat.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    public ChatsFragment()
    {
        // Required empty public constructor
    }
    FragmentChatsBinding binding;

    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    UsersAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        //Initializing
        database = FirebaseDatabase.getInstance();
        recyclerView = binding.chatRecyclerView;
        layoutManager = new LinearLayoutManager(getContext());
        adapter = new UsersAdapter(list, getContext());

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //Add users in RV of chat tab
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Users users = dataSnapshot.getValue(Users.class);
                    users.setUserId(dataSnapshot.getKey());

                    //Delete login user
                    if(!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                        list.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}