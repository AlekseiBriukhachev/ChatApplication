package com.aleksei.traskchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText usernameEditText;
    private Button loginSignUpButton;
    private TextView toggleLoginSignUpTextView;
    private boolean loginModeActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeInsets.bottom > 0 ? imeInsets.bottom : systemBars.bottom);

            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }


        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);

        loginSignUpButton.setOnClickListener(v -> loginSignUpUser(emailEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim()));

        toggleLoginSignUpTextView.setOnClickListener(v -> toggleLoginMode());
    }

    private void toggleLoginMode() {
        if (loginModeActive) {
            loginModeActive = false;
            loginSignUpButton.setText(R.string.sign_up);
            toggleLoginSignUpTextView.setText(R.string.or_login);
            confirmPasswordEditText.setVisibility(View.VISIBLE);
        } else {
            loginModeActive = true;
            loginSignUpButton.setText(R.string.log_in);
            toggleLoginSignUpTextView.setText(R.string.or_sign_up);
            confirmPasswordEditText.setVisibility(View.GONE);
        }
    }

    private void loginSignUpUser(String email, String password) {
        if (loginModeActive) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> loginOrSignUp(task, "SignIn"));
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> loginOrSignUp(task, "SignUp"));
        }
    }

    private void loginOrSignUp(Task<AuthResult> task, String authMethod) {
        if (task.isSuccessful()) {
            Log.d(TAG, authMethod + ":success");
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
//                                updateUI(user);
        } else {
            Log.w(TAG, authMethod + "failure", task.getException());
            Toast.makeText(SignInActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
//                                updateUI(null);
        }
    }
}