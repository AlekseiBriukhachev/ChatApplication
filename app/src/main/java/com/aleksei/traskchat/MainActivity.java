package com.aleksei.traskchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private static final int RC_IMAGE_PICKER = 124;
    private ListView messageListView;
    private ChatMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;
    private String userName;
    private List<ChatMessage> messages;
    private FirebaseDatabase database;
    private DatabaseReference messagesDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener messagesEventListener;
    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeInsets.bottom > 0 ? imeInsets.bottom : systemBars.bottom);

            return insets;
        });

        database = FirebaseDatabase.getInstance("https://traskchat-aaa49-default-rtdb.europe-west1.firebasedatabase.app/");
        storage = FirebaseStorage.getInstance();
//        storage = FirebaseStorage.getInstance("gs://traskchat-aaa49.appspot.com");
        messagesDatabaseReference = database.getReference().child("messages");
        chatImagesStorageReference = storage.getReference().child("chat_images");
        messagesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                adapter.add(message);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messagesDatabaseReference.addChildEventListener(messagesEventListener);

        Intent intent = getIntent();
        userName = intent != null ? intent.getStringExtra("userName") : "Default User";

        messages = new ArrayList<>();
        messageListView = findViewById(R.id.messageListView);
        adapter = new ChatMessageAdapter(this, R.layout.message_item, messages);
        messageListView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        sendImageButton = findViewById(R.id.sendImageButton);
        sendImageButton.setOnClickListener(this);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(this);

        messageEditText = findViewById(R.id.messageEditText);
        messageEditText.addTextChangedListener(this);
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sendImageButton) {
            Toast.makeText(this, "Image Button", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE_PICKER);
        } else if (id == R.id.sendMessageButton) {
            Toast.makeText(this, "Message sent", Toast.LENGTH_LONG).show();
            ChatMessage message = new ChatMessage();
            message.setMessage(messageEditText.getText().toString());
            message.setName(userName != null ? userName : "Sucka");
            message.setImageUrl(null);
            String format = SimpleDateFormat.getInstance().format(Calendar.getInstance().getTime());
            message.setTime(format);

            messagesDatabaseReference.push().setValue(message);
            messageEditText.setText("");
        }
    }

    private void handleSendImage() {
        Toast.makeText(this, "Image Button", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE_PICKER);
    }

    private void handleSendMessage() {
        Toast.makeText(this, "Message sent", Toast.LENGTH_LONG).show();
        ChatMessage message = new ChatMessage();
        message.setMessage(messageEditText.getText().toString());
        message.setName(userName != null ? userName : "Sucka");
        message.setImageUrl(null);
        message.setTime(SimpleDateFormat.getInstance().format(Calendar.getInstance().getTime()));

        messagesDatabaseReference.push().setValue(message);
        messageEditText.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        sendMessageButton.setEnabled(!s.toString().trim().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image selected", Toast.LENGTH_LONG).show();
            Uri selectedImageUri = data.getData();
            StorageReference imageReference = chatImagesStorageReference.child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

//            uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        ChatMessage message = new ChatMessage();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        messagesDatabaseReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }
}