package com.example.qltv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PersonalActivity extends AppCompatActivity {

    Button btnSignOut;
    TextView userEmail;

    // Lấy đối tượng FirebaseAuth
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // Lấy người dùng hiện tại
    FirebaseUser currentUser = auth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        addControls();
        addEvents();
    }

    private void addEvents() {
        if (currentUser != null) {
            // Lấy email người dùng
            userEmail.setText(currentUser.getEmail());
        }
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(PersonalActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent intent = new Intent(PersonalActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControls() {
        btnSignOut = findViewById(R.id.btnSignOut);
        userEmail = findViewById(R.id.userEmail);
    }
}