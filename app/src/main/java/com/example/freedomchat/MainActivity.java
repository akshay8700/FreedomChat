package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Notification;
import android.app.NotificationChannel;
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
        notiTest();

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
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//
//        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Music")
                .setContentTitle("MyNotification")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.square_account_icon)
                .setContentText("Hello this is a test");

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(69, builder.build());

        Toast.makeText(this, "Notification called", Toast.LENGTH_SHORT).show();
    }
}