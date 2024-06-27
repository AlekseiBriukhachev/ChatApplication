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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

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
    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;

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
            startActivity(new Intent(SignInActivity.this, ChatActivity.class));
        }
        database = FirebaseDatabase.getInstance("https://traskchat-aaa49-default-rtdb.europe-west1.firebasedatabase.app/");
        usersDatabaseReference = database.getReference().child("users");


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
        loginModeActive = !loginModeActive;
        loginSignUpButton.setText(loginModeActive ? R.string.log_in : R.string.sign_up);
        toggleLoginSignUpTextView.setText(loginModeActive ? R.string.or_sign_up : R.string.or_login);
        confirmPasswordEditText.setVisibility(loginModeActive ? View.GONE : View.VISIBLE);
        usernameEditText.setVisibility(loginModeActive ? View.GONE : View.VISIBLE);
    }

    private void loginSignUpUser(String email, String password) {
        if (isInputValid(email, password)) {
            if (loginModeActive) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> loginOrSignUp(task, "SignIn"));
            } else {
                if (password.equals(confirmPasswordEditText.getText().toString().trim())) {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> loginOrSignUp(task, "SignUp"));
                } else {
                    Toast.makeText(this, "The password does not match", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean isInputValid(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.trim().length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginOrSignUp(Task<AuthResult> task, String authMethod) {
        Intent intent = new Intent(SignInActivity.this, ChatActivity.class);
        if (task.isSuccessful()) {
            Log.d(TAG, authMethod + ":success");
            FirebaseUser user = auth.getCurrentUser();
            ChatUser chatUser = null;
            if (authMethod.equals("SignUp")) {
                chatUser = createChatUser(Objects.requireNonNull(user));
            } else if (authMethod.equals("SignIn")) {
                getUserByEmail(Objects.requireNonNull(user), new GetUserCallback() {
                    @Override
                    public void onSuccess(ChatUser user) {
                        intent.putExtra("userName", user.getName());
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, error);
                        Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            intent.putExtra("userName", chatUser != null ? chatUser.getName() : "Default User");
            startActivity(intent);
        } else {
            String message = authMethod.equals("SignIn") ? "User not found" : "Authentication failed.";
            Log.w(TAG, authMethod + " failure", task.getException());
            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private ChatUser createChatUser(FirebaseUser firebaseUser) {
        ChatUser user = new ChatUser();
        user.setId(firebaseUser.getUid());
        user.setName(usernameEditText.getText().toString().trim());
        user.setEmail(firebaseUser.getEmail());

        usersDatabaseReference.push().setValue(user);
        return user;
    }

    private  void getUserByEmail(FirebaseUser firebaseUser, final GetUserCallback callback) {

        String emailToFind = firebaseUser.getEmail();

        usersDatabaseReference.orderByChild("email").equalTo(emailToFind)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String id = userSnapshot.child("id").getValue(String.class);
                        String username = userSnapshot.child("name").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);

                        ChatUser chatUser = new ChatUser(id, username, email);
                        callback.onSuccess(chatUser);
                        return;
                    }
                } else {
                    callback.onFailure("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
}