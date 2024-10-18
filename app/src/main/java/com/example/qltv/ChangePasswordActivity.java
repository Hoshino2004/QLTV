package com.example.qltv;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword;
    private Button btnChangePassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword2);
        auth = FirebaseAuth.getInstance();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = etOldPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword)) {
                    etOldPassword.setError("Enter your old password");
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    etNewPassword.setError("Enter a new password");
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

                    // Re-authenticate the user
                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the password
                            user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Password update failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}