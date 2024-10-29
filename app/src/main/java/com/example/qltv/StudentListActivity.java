package com.example.qltv;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qltv.adapter.BookAdapter;
import com.example.qltv.adapter.StudentAdapter;
import com.example.qltv.model.Book;
import com.example.qltv.model.Student;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    RecyclerView rcvStudent;
    StudentAdapter mStudentAdapter;
    List<Student> mListStudent;

    EditText edtSearchStudent;

    DatabaseReference studentsRef;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        addControl();
        getListStudent();
        // Thêm TextWatcher để thực hiện tìm kiếm
        edtSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mStudentAdapter.filter(s.toString()); // Lọc danh sách theo text người dùng nhập
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add) {
            Intent intent = new Intent(StudentListActivity.this, AddStudentActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.nav_home) {
            Intent intent = new Intent(StudentListActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControl() {
        // Initialize Firebase Storage

        database = FirebaseDatabase.getInstance();
        studentsRef = database.getReference("Student");

        edtSearchStudent = findViewById(R.id.edtSearchStudent);

        rcvStudent = findViewById(R.id.rcvStudent);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StudentListActivity.this);
        rcvStudent.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(StudentListActivity.this, DividerItemDecoration.VERTICAL);
        rcvStudent.addItemDecoration(dividerItemDecoration);

        mListStudent = new ArrayList<>();

        mStudentAdapter = new StudentAdapter(mListStudent, new StudentAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Student student = mListStudent.get(position);
                showUpdateDialog(student.getMaSV(), student.getTenSV(), student.getSdtSV());
            }
        });
        rcvStudent.setAdapter(mStudentAdapter);


    }

    private void getListStudent() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListStudent != null) {
                    mListStudent.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    mListStudent.add(student);
                }
                // Cập nhật mListBookFull để lưu trữ toàn bộ danh sách
                mStudentAdapter.updateFullList(new ArrayList<>(mListStudent));
                mStudentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentListActivity.this, "Lỗi hiển thị", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showUpdateDialog(String id, String name, String sdt) {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(StudentListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.update_student, null);
        mDialog.setView(mDialogView);

        EditText studentIdUpdate = mDialogView.findViewById(R.id.student_id_update);
        EditText studentNameUpdate = mDialogView.findViewById(R.id.student_name_update);
        EditText studentPhoneUpdate = mDialogView.findViewById(R.id.student_phone_update);

        Button btnUpdate = mDialogView.findViewById(R.id.btnUpdateStudent);
        Button btnDelete = mDialogView.findViewById(R.id.btnDeleteStudent);


        studentIdUpdate.setText(id);
        studentIdUpdate.setEnabled(false);
        studentNameUpdate.setText(name);
        studentPhoneUpdate.setText(sdt);

        mDialog.show();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = studentNameUpdate.getText().toString();
                String newPhone = studentPhoneUpdate.getText().toString();

                if (TextUtils.isEmpty(newName)) {
                    studentNameUpdate.setError("Vui lòng nhập tên sinh viên");
                    return;
                }
                if (TextUtils.isEmpty(newPhone)) {
                    studentPhoneUpdate.setError("Vui lòng nhập số điện thoại");
                    return;
                }

                updateData(id, newName, newPhone);
                studentNameUpdate.setText("");
                studentPhoneUpdate.setText("");

                startActivity(new Intent(StudentListActivity.this, StudentListActivity.class));
                Toast.makeText(StudentListActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();


            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StudentListActivity.this);
                builder.setTitle("Xóa sinh viên");
                builder.setMessage("Bạn có chắc muốn xóa sinh viên này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecord(id);
                    }
                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void updateData(String id, String name, String sdt) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Student").child(id);
        Student student = new Student(id, name, sdt);
        DbRef.setValue(student);
    }

    private void deleteRecord(String id) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Student").child(id);
        Task<Void> mTask = DbRef.removeValue();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("Xóa sinh viên thành công");
                mStudentAdapter.notifyDataSetChanged();
                startActivity(new Intent(StudentListActivity.this, StudentListActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Xóa sinh viên thất bại");
                //startActivity(new Intent(getContext(), DataManagementFragment.class));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(StudentListActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}