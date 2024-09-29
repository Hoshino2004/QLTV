package com.example.qltv;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText edtEmailResetPass;
    private Button btnSendEmail;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initUi();
        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickForgotPassword();
            }
        });
    }

    private void onClickForgotPassword() {
        String email = edtEmailResetPass.getText().toString().trim();
        progressDialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initUi() {
        edtEmailResetPass = findViewById(R.id.edt_email_reset_password);
        btnSendEmail = findViewById(R.id.btn_send_email);
        progressDialog = new ProgressDialog(this);
    }
}