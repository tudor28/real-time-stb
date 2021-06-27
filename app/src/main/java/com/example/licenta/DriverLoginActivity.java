package com.example.licenta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


public class DriverLoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private Button Login, Registration, DriverReg;
    private FirebaseAuth Auth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    public String userType;
    public boolean gotUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseAuth.getInstance().signOut();
        }

        Auth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    /*Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();*/
                    return;
                }

            }
        };

        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        DriverReg = (Button) findViewById(R.id.register);
        Login = (Button) findViewById(R.id.login);
        /*Registration = (Button) findViewById(R.id.register);

        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String password = Password.getText().toString();
                Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DriverLoginActivity.this, "Registration error!", Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = Auth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });*/
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String password = Password.getText().toString();
                if(email.matches("") || password.matches("")){
                    Toast.makeText(DriverLoginActivity.this, "Please enter full login details!", Toast.LENGTH_SHORT).show();
                }else {
                    Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "Login error!", Toast.LENGTH_SHORT).show();
                            } else {
                                if (Auth.getCurrentUser() != null) {
                                    String user_id = Auth.getCurrentUser().getUid();
                                    DatabaseReference dbLoginRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                    dbLoginRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                            /*Map<String,Object> dbMap = (Map<String, Object>) dataSnapshot.getValue();
                                            userType = dbMap.get("User type").toString();
                                            gotUser = true;*/
                                                // if (userType.equals("STB Driver")) {
                                                Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                // }
                                                // Log.d(TAG,userType);

                                                // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            /*if(userType == "Customer") {
                                                Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                                return;
                                            }
                                            else {
                                                Toast.makeText(CustomerLoginActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                                            }*/
                                            } else {
                                                Toast.makeText(DriverLoginActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
        EditText edittext = (EditText)findViewById(R.id.password);
        edittext.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        DriverReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverLoginActivity.this, DriverRegisterActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }
    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                return mSource.length(); // Return default
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        Auth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Auth.removeAuthStateListener(firebaseAuthListener);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DriverLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
