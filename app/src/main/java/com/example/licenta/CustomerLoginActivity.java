package com.example.licenta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private Button Login, Registration;
    private static final String TAG = "intrare";

    private FirebaseAuth Auth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    public String userType;
    public boolean gotUser = false;
    public String uT = "Customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        Auth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){
                    /*Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();*/
                    return;
                }

                /*if(user != null && userType != "Customer" && gotUser == true){
                    Toast.makeText(CustomerLoginActivity.this, "Login error!", Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);

        Login = (Button) findViewById(R.id.login);
        Registration = (Button) findViewById(R.id.registration);

        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String password = Password.getText().toString();
                if(email.matches("") || password.matches("")){
                    Toast.makeText(CustomerLoginActivity.this, "Incomplete registration details!", Toast.LENGTH_SHORT).show();
                }else {
                    Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(CustomerLoginActivity.this, "Registration error!", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = Auth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);

                                String dbName = Email.getText().toString();
                                String dbType = "Customer";

                                Map newPost = new HashMap();
                                newPost.put("Email", dbName);
                                newPost.put("User type", dbType);

                                current_user_db.setValue(newPost);
                            }
                        }
                    });
                }
            }
        });


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String password = Password.getText().toString();
                if(email.matches("") || password.matches("")){
                    Toast.makeText(CustomerLoginActivity.this, "Please enter full login details!", Toast.LENGTH_SHORT).show();
                }else {
                    Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(CustomerLoginActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                            } else {
                                if (Auth.getCurrentUser() != null) {
                                    String user_id = Auth.getCurrentUser().getUid();
                                    DatabaseReference dbLoginRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                                    dbLoginRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                                                startActivity(intent);
                                                finish();
                                            /*Map<String,Object> dbMap = (Map<String, Object>) dataSnapshot.getValue();
                                            userType = dbMap.get("User type").toString();
                                            /*gotUser = true;
                                            Log.d(TAG,userType);
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
                                                Toast.makeText(CustomerLoginActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
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

                /*DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                Log.d(TAG,user_id);
                DatabaseReference dbLoginRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                dbLoginRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Map<String,Object> dbMap = (Map<String, Object>) dataSnapshot.getValue();
                            String userType = dbMap.get("User type").toString();
                            Log.d(TAG,userType);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
            }
        });

        EditText edittext = (EditText)findViewById(R.id.password);
        edittext.setTransformationMethod(new CustomerLoginActivity.AsteriskPasswordTransformationMethod());
    }
    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new CustomerLoginActivity.AsteriskPasswordTransformationMethod.PasswordCharSequence(source);
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
        Intent intent = new Intent(CustomerLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
