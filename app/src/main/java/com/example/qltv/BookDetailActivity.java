package com.example.qltv;

import static java.lang.Integer.parseInt;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qltv.model.Information;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BookDetailActivity extends AppCompatActivity {
    ImageView bookImage;
    TextView bookName, bookQuantity,  bookCategory, bookAuthor, bookDescription, bookStatus;
    Button btnThemNguoiMuon;

    String generatedBookId;
    String idReal;

    DatabaseReference informationRef;

    DatabaseReference bookRef;
    FirebaseDatabase database;

    private DatabaseReference studentRef;
    private List<String> studentList;
    private ArrayAdapter<String> studentAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        // Hiển thị nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setTitle("");
        addControls();
        addEvents();
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

    // Hàm tạo ID sách tự động và kiểm tra trùng lặp
    private void generateUniqueInformationId() {
        generatedBookId = generateInformationId();  // Tạo ID sách ban đầu
        informationRef.child(generatedBookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nếu ID đã tồn tại, tạo lại ID khác và kiểm tra tiếp
                    generateUniqueInformationId();
                } else {
                    // Nếu ID không tồn tại, hiển thị lên EditText
                    idReal = generatedBookId;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tạo ID sách tự động theo kiểu S0000
    private String generateInformationId() {
        Random random = new Random();
        int number = random.nextInt(10000);  // Tạo số ngẫu nhiên từ 0 đến 9999
        return String.format("I%04d", number);  // Định dạng số thành Sxxxx
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(Calendar.getInstance().getTime());
    }

    // Hàm kiểm tra nếu date2 lớn hơn date1 không quá 2 tuần
    public boolean isDateWithinTwoWeeks(String dateStr1, String dateStr2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false); // Đảm bảo kiểm tra định dạng chặt chẽ

        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date date1 = dateFormat.parse(dateStr1);
            Date date2 = dateFormat.parse(dateStr2);

            // Tính khoảng cách thời gian giữa hai ngày tính theo milliseconds
            long diffInMillis = date2.getTime() - date1.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // Kiểm tra nếu date2 lớn hơn date1 và khoảng cách không quá 14 ngày
            return diffInDays > 0 && diffInDays <= 14;
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // trả về false nếu có lỗi khi chuyển đổi chuỗi
        }
    }


    private void addEvents() {
        Intent intent = getIntent();
        Glide.with(this).load(intent.getStringExtra("bookImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Không lưu cache trên đĩa
                .skipMemoryCache(true)
                .error(R.drawable.ic_error34)// Không lưu cache trong bộ nhớ
                .into(bookImage);
        bookName.setText(intent.getStringExtra("bookName"));
        bookQuantity.setText(String.valueOf(intent.getIntExtra("bookQuantity", 0)));
        bookCategory.setText(intent.getStringExtra("bookCategory"));
        bookAuthor.setText(intent.getStringExtra("bookAuthor"));
        bookDescription.setText(intent.getStringExtra("bookDescription"));
        bookStatus.setText(intent.getStringExtra("bookStatus"));

        if (bookStatus.getText().equals("Còn sách")) {
            bookStatus.setTextColor(Color.GREEN);
        } else {
            bookStatus.setTextColor(Color.RED);
            btnThemNguoiMuon.setVisibility(View.GONE);
        }

        btnThemNguoiMuon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBorrowerDialog();
            }
        });
    }

    private void showAddBorrowerDialog() {
        generateUniqueInformationId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_them_nguoi_muon, null);
        builder.setView(dialogView);

        Spinner spinnerStudent = dialogView.findViewById(R.id.spinnerStudent);
        EditText etReturnDate = dialogView.findViewById(R.id.etReturnDate);
        Button btnAddBorrower = dialogView.findViewById(R.id.btnAddBorrower);

        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentList);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudent.setAdapter(studentAdapter);
        loadStudents();

        final Calendar calendar = Calendar.getInstance();

        etReturnDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                etReturnDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
            }, year, month, day);

            datePickerDialog.show();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnAddBorrower.setOnClickListener(v -> {
            Intent intent = getIntent();
            String selectedStudent = spinnerStudent.getSelectedItem().toString();
            String dueDate = etReturnDate.getText().toString();

            if (isDateWithinTwoWeeks(getCurrentDate(), dueDate)) {
                Information information = new Information(idReal, intent.getStringExtra("bookId"), intent.getStringExtra("bookImage"), intent.getStringExtra("bookName"), selectedStudent, getCurrentDate(), dueDate, "Đang mượn");
                informationRef.child(idReal).setValue(information);
                int quantity = parseInt(bookQuantity.getText().toString());
                bookRef.child(intent.getStringExtra("bookId")).child("quantity").setValue(quantity - 1);
                bookQuantity.setText(String.valueOf(quantity - 1));
                if (bookQuantity.getText().equals("0")) {
                    btnThemNguoiMuon.setVisibility(View.GONE);
                    bookStatus.setText("Hết sách");
                    bookRef.child(intent.getStringExtra("bookId")).child("status").setValue("Hết sách");
                    bookStatus.setTextColor(Color.RED);
                }
                Toast.makeText(BookDetailActivity.this, "Thêm người mượn thành công", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            } else {
                Toast.makeText(BookDetailActivity.this, "Ngày trả không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudents() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                studentList.clear();  // Xóa danh sách cũ
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String maSV = snapshot.child("maSV").getValue(String.class);
                    String tenSV = snapshot.child("tenSV").getValue(String.class);
                    studentList.add(maSV + " - " + tenSV); // Thêm sinh viên vào danh sách
                }
                // Cập nhật adapter cho Spinner khi dữ liệu Firebase đã sẵn sàng
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BookDetailActivity.this, "Lỗi khi tải dữ liệu sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addControls() {
        bookImage = findViewById(R.id.imgBookDetail);
        bookName = findViewById(R.id.tvBookTitleDetail);
        bookQuantity = findViewById(R.id.tvQuantityDetail);
        bookCategory = findViewById(R.id.tvCategoryDetail);
        bookAuthor = findViewById(R.id.tvAuthorDetail);
        bookDescription = findViewById(R.id.tvDescriptionDetail);
        bookStatus = findViewById(R.id.tvStatusDetail);
        btnThemNguoiMuon = findViewById(R.id.btnThemNguoiMuon);

        // Khởi tạo tham chiếu đến nhánh "Student" trong Firebase Realtime Database
        studentRef = FirebaseDatabase.getInstance().getReference("Student");
        studentList = new ArrayList<>();

        // Initialize Firebase Storage
        database = FirebaseDatabase.getInstance();
        informationRef = database.getReference("Information");

        bookRef = database.getReference("Book");
    }
}