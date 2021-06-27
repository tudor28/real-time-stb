package com.example.licenta;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;

public class DriverRegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private EditText name, email, password;
    Button Register;
    DatabaseReference databaseReference;
    FirebaseAuth Auth;
    private String selectedLine;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth.AuthStateListener firebaseAuthListener;
    private Spinner line;
    //String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DriverRegisterActivity.this, DriverLoginActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        //userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        name = (EditText) findViewById(R.id.name1);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        line = (Spinner) findViewById(R.id.line);
        Register = (Button) findViewById(R.id.register);

        Auth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.line,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        line.setAdapter(adapter);
        line.setOnItemSelectedListener(this);

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                String nameText = name.getText().toString();

                if(selectedLine.equals("Choose a line...") || emailText.matches("") || passwordText.matches("") || nameText.matches("")){
                    Toast.makeText(DriverRegisterActivity.this, "Incomplete registration details!", Toast.LENGTH_SHORT).show();
                }else {
                    final String registerEmail = email.getText().toString();
                    final String registerPassword = password.getText().toString();
                    // final String registerName = name.getText().toString();
                    //final String registerLine = line.getSelectedItem().toString().trim();
                    Auth.createUserWithEmailAndPassword(registerEmail, registerPassword).addOnCompleteListener(DriverRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverRegisterActivity.this, "Registration error!", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = Auth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);

                                String dbName = name.getText().toString();
                                String dbLine = line.getSelectedItem().toString();
                                String dbEmail = email.getText().toString();

                                Map newPost = new HashMap();
                                newPost.put("Email", dbEmail);
                                newPost.put("Name", dbName);
                                newPost.put("Line", dbLine);
                                newPost.put("User type", "STB Driver");
                                current_user_db.setValue(newPost);
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getItemAtPosition(position).equals("Choose a line...")){
            //do nothing
        }
        selectedLine = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new DriverRegisterActivity.AsteriskPasswordTransformationMethod.PasswordCharSequence(source);
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

    public void onBackPressed() {
        Intent intent = new Intent(DriverRegisterActivity.this, DriverLoginActivity.class);
        startActivity(intent);
        finish();
    }

}


