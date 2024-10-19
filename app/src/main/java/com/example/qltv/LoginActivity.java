package com.example.qltv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qltv.R;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail2, edtPass2;
    Button btnLogin;
    TextView layoutForgotPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        addEvents();

    }

    private void addEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail2.getText().toString().trim();
                String password = edtPass2.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    edtEmail2.setError("Nhập email của bạn");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    edtPass2.setError("Nhập mật khẩu của bạn");
                    return;
                }

                final FirebaseAuth auth = FirebaseAuth.getInstance();
                progressDialog.show();
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener((task) -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            invalidateOptionsMenu();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Vui lòng xác thực Email", Toast.LENGTH_SHORT).show();
                            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        layoutForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        edtEmail2 = findViewById(R.id.edtEmail2);
        edtPass2 = findViewById(R.id.edtPass2);
        btnLogin = findViewById(R.id.btnLogin);
        progressDialog = new ProgressDialog(this);
        layoutForgotPassword = findViewById(R.id.layout_forgot_password);
    }
}