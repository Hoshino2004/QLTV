package com.example.qltv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qltv.model.Book;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Random;
import java.util.UUID;

public class AddBookActivity extends AppCompatActivity {
    Button btnThem;
    EditText bookIdAdd, bookNameAdd, bookQuantityAdd, bookDescriptionAdd, bookAuthorAdd;
    Spinner bookCategorySpinner;

    private static final int PICK_IMAGE_REQUEST = 71;
    Uri filePath;
    ImageView bookImageView;
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseDatabase database;
    DatabaseReference booksRef;
    String generatedBookId;

    DatabaseReference categoryRef;
    List<String> categoryList = new ArrayList<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // Hiển thị nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setTitle("");

        addControls();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseDatabase.getInstance();
        booksRef = database.getReference("Book");
        categoryRef = database.getReference("Category");

        // Load categories into Spinner
        loadCategories();

        // Tạo mã sách tự động và kiểm tra trùng lặp
        generateUniqueBookId();

        // Chọn ảnh khi nhấn vào ImageView
        bookImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
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

    // Hàm load thể loại sách từ Firebase và đưa vào Spinner
    private void loadCategories() {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của các thể loại và thêm vào danh sách
                    String category = snapshot.getValue(String.class);
                    categoryList.add(category);
                }

                // Tạo adapter và gán vào Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddBookActivity.this,
                        android.R.layout.simple_spinner_item, categoryList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bookCategorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddBookActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tạo ID sách tự động và kiểm tra trùng lặp
    private void generateUniqueBookId() {
        generatedBookId = generateBookId();  // Tạo ID sách ban đầu
        booksRef.child(generatedBookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nếu ID đã tồn tại, tạo lại ID khác và kiểm tra tiếp
                    generateUniqueBookId();
                } else {
                    // Nếu ID không tồn tại, hiển thị lên EditText
                    bookIdAdd.setText(generatedBookId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddBookActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tạo ID sách tự động theo kiểu S0000
    private String generateBookId() {
        Random random = new Random();
        int number = random.nextInt(10000);  // Tạo số ngẫu nhiên từ 0 đến 9999
        return String.format("S%04d", number);  // Định dạng số thành Sxxxx
    }

    // Mở thư viện ảnh để chọn
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                bookImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Upload ảnh lên Firebase Storage
    private void uploadImage() {
        if (filePath != null) {
            StorageReference ref = storageReference.child("images/" + generatedBookId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Lấy link ảnh sau khi upload thành công
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    addBookToDatabase(imageUrl);  // Gọi hàm lưu thông tin sách và link ảnh
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddBookActivity.this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Hàm lưu sách vào Realtime Database với link ảnh
    private void addBookToDatabase(String imageUrl) {
        String id = bookIdAdd.getText().toString().trim();
        String name = bookNameAdd.getText().toString().trim();
        String quantityText = bookQuantityAdd.getText().toString().trim();
        String description = bookDescriptionAdd.getText().toString().trim();
        String author = bookAuthorAdd.getText().toString().trim();
        String category = bookCategorySpinner.getSelectedItem().toString(); // Lấy giá trị thể loại từ Spinner

        if (TextUtils.isEmpty(name)) {
            bookNameAdd.setError("Vui lòng nhập tên sách");
            storageReference.child("images/" + id).delete();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                bookQuantityAdd.setError("Số lượng phải lớn hơn 0");
                storageReference.child("images/" + id).delete();
                return;
            }
        } catch (NumberFormatException e) {
            bookQuantityAdd.setError("Số lượng không hợp lệ");
            storageReference.child("images/" + id).delete();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            bookDescriptionAdd.setError("Vui lòng nhập mô tả");
            storageReference.child("images/" + id).delete();
            return;
        }

        if (TextUtils.isEmpty(author)) {
            bookAuthorAdd.setError("Vui lòng nhập tác giả");
            storageReference.child("images/" + id).delete();
            return;
        }

        Book book = new Book(id, name, imageUrl, quantity, author, category, description, "Còn sách");
        addBook(book);
        startActivity(new Intent(AddBookActivity.this, BookListActivity.class));
    }

    private void addBook(Book book) {
        // Write a message to the database

        String pathObject = book.getId();
        booksRef.child(pathObject).setValue(book, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                Toast.makeText(AddBookActivity.this, "Thêm sách thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addControls() {
        bookImageView = findViewById(R.id.book_image_view);
        bookIdAdd = findViewById(R.id.book_id_add);
        bookNameAdd = findViewById(R.id.book_name_add);
        bookQuantityAdd = findViewById(R.id.book_quantity_add);
        bookDescriptionAdd = findViewById(R.id.book_description_add);
        bookAuthorAdd = findViewById(R.id.book_author_add);
        bookCategorySpinner = findViewById(R.id.book_category_spinner);
        btnThem = findViewById(R.id.btnThem);
    }
}