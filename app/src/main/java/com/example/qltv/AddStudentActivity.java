package com.example.qltv;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qltv.model.Book;
import com.example.qltv.model.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddStudentActivity extends AppCompatActivity {
    Button btnThemStudent;
    EditText studentIdAdd, studentNameAdd, studentPhoneAdd;

    FirebaseDatabase database;
    DatabaseReference studentsRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Hiển thị nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setTitle("");

        addControls();

        // Initialize Firebase Storage
        database = FirebaseDatabase.getInstance();
        studentsRef = database.getReference("Student");
        btnThemStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBookToDatabase();
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

    // Hàm lưu sách vào Realtime Database với link ảnh
    private void addBookToDatabase() {
        String id = studentIdAdd.getText().toString().trim();
        String name = studentNameAdd.getText().toString().trim();
        String phone = studentPhoneAdd.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            studentIdAdd.setError("Vui lòng nhập mã sinh viên");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            studentNameAdd.setError("Vui lòng nhập tên sinh viên");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            studentPhoneAdd.setError("Vui lòng nhập số điện thoại");
            return;
        }

        Student student = new Student(id, name, phone);

        studentsRef.child(id).setValue(student);

        startActivity(new Intent(AddStudentActivity.this, StudentListActivity.class));
    }

    private void addControls() {
        studentIdAdd = findViewById(R.id.student_id_add);
        studentNameAdd = findViewById(R.id.student_name_add);
        studentPhoneAdd = findViewById(R.id.student_phone_add);
        btnThemStudent = findViewById(R.id.btnThemStudent);
    }
}