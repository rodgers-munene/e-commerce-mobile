package com.example.swiftcart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    //variable declaration
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    TextView linkLogin;
    private EditText firstName, lastName, email, password, confirmPassword;
    private ProgressBar progressBar;
    private Button signupBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        //Authentication logic

        // assigning values to the variables
        mAuth = FirebaseAuth.getInstance(); // initializes a firebase instance
        db = FirebaseFirestore.getInstance();
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        CheckBox signupCheckbox = findViewById(R.id.signupCheckbox);
        signupBtn = findViewById(R.id.signupBtn);
        progressBar =findViewById(R.id.progressBar);

        // enable button only if check box is checked

        signupCheckbox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            signupBtn.setEnabled(isChecked);
        }));

        //signup button functionality
        signupBtn.setOnClickListener(v -> {
            String firstN = firstName.getText().toString().trim();
            String lastN = lastName.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if(validateInput(firstN, lastN, userEmail, pass, confirmPass)){
                registerUser(firstN, lastN, userEmail, pass);
            }
        });

        // switch activities for a user who already has an account
        linkLogin = findViewById(R.id.linkLogin);

        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInput( String firstN, String lastN, String userEmail, String pass, String confirmPass) {
        if (firstN.isEmpty()){
            firstName.setError("This Field is Required!!");
            return false;
        }
        if (lastN.isEmpty()){
            lastName.setError("This Field is Required!!");
            return false;
        }
        if (userEmail.isEmpty()){
            email.setError("This Field is Required!!");
            return false;
        }
        if (pass.isEmpty()){
            password.setError("This Field is Required!!");
            return false;
        }
        if(confirmPass.isEmpty()){
            confirmPassword.setError("This Field is Required!!");
            return false;
        }
        if(!confirmPass.equals(pass)){
           confirmPassword.setError("Passwords Do Not Match!!");
           return false;
        }
        return true;
    }

    private void registerUser(String firstName, String lastName, String email, String password){
        //set loading animation to begin once sign up button is clicked and disable the button
        progressBar.setVisibility(View.VISIBLE);
        signupBtn.setEnabled(false);
        //create new firebase authentication account using the provided email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( task -> { //listens to completion of task

                    if ( task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null; //ensures the user is not null
                        String uid = user.getUid();

                        // create the user data map
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("firstName", firstName);
                        userMap.put("lastName", lastName);
                        userMap.put("email", email);


                        //
                        db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {

                                    //stop the loading animation and enable the button again
                                    progressBar.setVisibility(View.GONE);
                                    signupBtn.setEnabled(true);

                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Data save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }else {
                        Toast.makeText(this, "Sign-up failed: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                );
    }
}