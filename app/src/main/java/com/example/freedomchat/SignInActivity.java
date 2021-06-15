package com.example.freedomchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.freedomchat.Models.Users;
import com.example.freedomchat.databinding.ActivitySignInBinding;
import com.example.freedomchat.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class
SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;

    public static String userToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        //Getting Firebase authentication instance
        //For checking is user have already account or not if yess than signIn if No go to singup and create new one
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //Setting up progressDialog
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("We'r logging into ur account");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //SignIn button what to do when user click on signIn
        binding.singInBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.emailEditText.getText().toString().isEmpty()){
                    binding.emailEditText.setError("Type email");
                    return;
                }
                if(binding.passwordET.getText().toString().isEmpty()){
                    binding.passwordET.setError("Type password");
                    return;
                }

                progressDialog.show();
                auth.signInWithEmailAndPassword(binding.emailEditText.getText().toString(), binding.passwordET.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Login sucessfully done!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //SignIn with google button
        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //If user have not account go to the signUp activity
        binding.doesNotHaveAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        //If user signin before app data clear and uninstall,
        //sign in user without asking signin details.
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
        }

        // set token for notifications send
        setToken();
    }

    //Part of google  sign in
    int RC_SIGN_IN = 69;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Part of google
    //After we choosed account while sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Users myusers = new Users();
                            myusers.setUserId(user.getUid());
                            myusers.setUserName(user.getDisplayName());
                            myusers.setProfilePic(user.getPhotoUrl().toString());

                            //Getting profile data from google than setting into user profile while sign in
                            database.getReference().child("Users").child(user.getUid()).setValue(myusers);

                            //Checking backup
                            setUserProfile(user);

                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Signed in with google", Toast.LENGTH_SHORT).show();
                            //updateUI(user);

                            setToken();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            //updateUI(null);
                        }
                    }
                });
    }

    //Backup check
    public void setUserProfile(FirebaseUser user){
        final Users[] backupData = {new Users()};

        //Get backup data
        database.getReference().child("UsersBackup").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users backup = snapshot.getValue(Users.class);
                backupData[0] = backup;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Set backup data into Users profile in firebase if backup exist
        database.getReference().child("UsersBackup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if backup exist
                if((snapshot.child(user.getUid()).exists())){
                    Users backup = backupData[0];
                    //Users backup = snapshot.getValue(Users.class);
                    database.getReference().child("Users").child(user.getUid()).setValue(backup).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            updateBackup(user);
                        }
                    });

                    Toast.makeText(SignInActivity.this, "Exist", Toast.LENGTH_LONG).show();
                }
                else{
                    updateBackup(user);
                    Toast.makeText(SignInActivity.this, "Not exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Update backup data
    public void updateBackup(FirebaseUser user){
        database.getReference().child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users update = snapshot.getValue(Users.class);
                database.getReference().child("UsersBackup").child(user.getUid()).setValue(update);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setToken() {
        // Send this token in firebase user details than get friend token from friend auth id from firebase
        // Getting userToken
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            userToken = Objects.requireNonNull(task.getResult()).getToken();

                            // Show userToken in logcat
                            Log.i("userToken", "User token: " + userToken);

                            // Saving token into database
                            if(auth.getUid() != null) {
                                database.getReference().child("Users").child(auth.getUid()).child("userToken").setValue(userToken);

                                Log.i("userToken", "userToken saved! " + userToken);
                            }
                            else {
                                Log.i("userToken", "authentication is null means user does not login");
                            }

                        }
                    }
                });
    }
}