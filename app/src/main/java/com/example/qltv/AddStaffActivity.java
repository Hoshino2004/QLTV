package com.example.qltv;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qltv.model.Staff;
import com.example.qltv.model.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStaffActivity extends AppCompatActivity {
    Button btnThemStaff;
    EditText staffEmailAdd, staffPasswordAdd, staffNameAdd, staffPhoneAdd;;

    FirebaseDatabase database;
    DatabaseReference userRef;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        // Hiển thị nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setTitle("");

        addControls();

        // Initialize Firebase Storage
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("User");
        mAuth = FirebaseAuth.getInstance();
        btnThemStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStaff();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  // Đây là ID của nút back
                onBackPressed();     // Quay lại Activity trước đó
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addControls() {
        btnThemStaff = findViewById(R.id.btnThemStaff);
        staffEmailAdd = findViewById(R.id.staff_email_add);
        staffPasswordAdd = findViewById(R.id.staff_password_add);
        staffNameAdd = findViewById(R.id.staff_name_add);
        staffPhoneAdd = findViewById(R.id.staff_phone_add);

    }

    private void addStaff() {
        String email = staffEmailAdd.getText().toString().trim();
        String password = staffPasswordAdd.getText().toString().trim();
        String name = staffNameAdd.getText().toString().trim();
        String phone = staffPhoneAdd.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            staffEmailAdd.setError("Vui lòng nhập email nhân viên");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            staffPasswordAdd.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            staffPasswordAdd.setError("Mật khẩu phải nhập ít nhất 6 kí tự");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            staffNameAdd.setError("Vui lòng nhập họ tên nhân viên");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            staffNameAdd.setError("Vui lòng nhập số điện thoại nhân viên");
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            FirebaseUser user = mAuth.getCurrentUser();  // Get the currently signed-in user
                            if (user != null) {
                                String uid = user.getUid();
                                Staff staff = new Staff(uid, email, password, name, phone, "Nhân viên");
                                userRef.child(uid).setValue(staff);
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AddStaffActivity.this, "Tạo tài khoản thành công, vui lòng xác thực email", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                            mAuth.signInWithEmailAndPassword("thinh631631@gmail.com", "123456");
                                        } else {
                                            Toast.makeText(AddStaffActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            finish(); // Close activity after registration
                        } else {
                            finish();
                            // Registration failed
                            Toast.makeText(AddStaffActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}