package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.freedomchat.Adapters.FragmentsAdapter;
import com.example.freedomchat.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        //ViewPager setup\TapLayout setup
        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    //When menu create
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When clicked on item in menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSetting:
                Toast.makeText(this, "Settings open", Toast.LENGTH_SHORT).show();
                Intent intentSettings = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intentSettings);
                break;

            case R.id.itemLogout:
                auth.signOut();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                break;

            case R.id.groupChatItem:
                Intent intentGC = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intentGC);
                break;
        }
        return true;
    }

    //Notification test
    public void notiTest(){
        // (Personal Experiment not official knowledge)
        // Here checking if android os is oreo or more than oreo
        // Create channel and Manager if OS is >=Oreo
        // Channel is a type of our notification group we can set a Importance at that specific group of notification or maybe more
        // Manager Is just manage and create the notification channel, Note:- Don`t forget that every notification channel creates only one time
        // This code runs one time until application uninstall aur Data cleared
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Music", "MusicNotification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Creating notification objects for every new notification create new object lol
        NotificationCompat.Builder musicNotification = new NotificationCompat.Builder(this, "Music")
                .setContentTitle("Music Notification")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.square_account_icon)
                .setContentText("Hello this is a second noti");

        // According to my experiment in 5/25/2021 I noticed that at upper manager that getting from our system is actually just creating notification channel
        // But this manager is actually checking that this notification is exist in our channel or not if not that will ignore and will not call
        // If yess it will call and notify and build that notification object with unique id and yaa he ignored is object same or not he just focus on id if id
        // -is same he will show only 1 notification even if object is same but if id is unique he don`t care it is same or not he will show that multiple times if we call multiple same objects with unique id
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        //showing notification with unique id
        managerCompat.notify(8329, musicNotification.build());
        managerCompat.notify(839, musicNotification.build());

        Toast.makeText(this, "Notification called", Toast.LENGTH_SHORT).show();
    }
}