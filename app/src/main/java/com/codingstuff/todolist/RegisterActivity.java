package com.codingstuff.todolist;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button mRegisterButton;
    ProgressBar mProgressBar;
    FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    EditText mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUIViews();
        mAuth = FirebaseAuth.getInstance();
        mRegisterButton.setOnClickListener(view -> register());

    }

    private void initUIViews() {
        mRegisterButton = findViewById(R.id.registerButton);
        mProgressBar = findViewById(R.id.progressBar);
        mPassword = findViewById(R.id.passwordEditText);
        mEmail = findViewById(R.id.emailEditText);
    }

    private void register() {
        mProgressBar.setVisibility(View.VISIBLE);
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorMsg("Fill Email/Password");
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        final Map<String, Object> hashMap = new HashMap<>();
                        hashMap.put("emails", FieldValue.arrayUnion(email));
                        mDB.collection("emails").document("emails").update(hashMap);
                        showErrorMsg("Success");
                        finish();
                    } else {
                        showErrorMsg("Authentication failed.");
                    }
                });

    }

    private void showErrorMsg(String msg) {
        Snackbar.make(findViewById(R.id.registerLayout), msg, Snackbar.LENGTH_SHORT).show();
    }
}