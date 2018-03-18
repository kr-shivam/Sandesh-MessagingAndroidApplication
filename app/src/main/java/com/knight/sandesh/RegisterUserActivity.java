package com.knight.sandesh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterUserActivity extends AppCompatActivity {

    // Initialize variables for layout items
    private TextInputLayout mUsername;
    private TextInputLayout mUserEmailId;
    private TextInputLayout mUserPassword;
    private Button mRegisterButton;

    //Init Toolbar variable
    private Toolbar mToolbar;

    //Init Progressbar variable
    private ProgressDialog mRegProgress;

    //Init Firebase Authentication variable
    private FirebaseAuth mAuth;

    //Init Firebase Database variable
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //Get Firebase Authentication Instance
        mAuth = FirebaseAuth.getInstance();

        //Adding a toolbar at top - Setting title and a back button
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New to Sandesh?");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //for back arrow

        //Initialize Progressbar
        mRegProgress = new ProgressDialog(this);

        // Get the fields from layout and store in variables
        mUsername = (TextInputLayout) findViewById(R.id.register_username);
        mUserEmailId = (TextInputLayout) findViewById(R.id.register_email);
        mUserPassword = (TextInputLayout) findViewById(R.id.register_password);
        mRegisterButton = (Button) findViewById(R.id.btn_join_me);

        //Adding functionality to Register button
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Retrieve those values entered in field and store in different varibles.
                String register_username = mUsername.getEditText().getText().toString();
                String register_user_email = mUserEmailId.getEditText().getText().toString();
                String register_user_password = mUserPassword.getEditText().getText().toString();

                //If the fields are not empty, proceed!
                if(!TextUtils.isEmpty(register_username)||(!TextUtils.isEmpty(register_user_email))||(!TextUtils.isEmpty(register_user_password))){

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Hang on! We are registering you!");
                    mRegProgress.setCanceledOnTouchOutside(false); // To disable touch while progress dialog is running
                    mRegProgress.show();

                    //method for registering new user
                    registerUser(register_username, register_user_email, register_user_password);

                }


            }
        });

    }

    private void registerUser(final String register_username, String register_user_email, final String register_user_password) {

        //Firebase method with Auth variable to create user with email and password
        mAuth.createUserWithEmailAndPassword(register_user_email,register_user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //If the user creation process is successful, proceed
                if(task.isSuccessful()){

                    //Get the userid of current user
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    //Adding childs to the object in database >>> Open "console.firebase.google.com" to see progress
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    //Creating a Hashmap which will store values for the new user
                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put("name", register_username);
                    userMap.put("status", "Hi, I am new to Sandesh.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    //Adding values to the database
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //If the registration is done and DB accepts values, dismiss the progress dialog
                                mRegProgress.dismiss();

                                //String current_user_id = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                mDatabase.child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Providing intent for the application to proceed further
                                        Intent mainIntent = new Intent(RegisterUserActivity.this, MainActivity.class);

                                        // To prevent back button after login, we clear tasks and initiate new task
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                });


                            }
                        }
                    });


                }else{
                    mRegProgress.hide();
                    Toast.makeText(RegisterUserActivity.this, "Oops! Looks like something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
            });
        }
}
