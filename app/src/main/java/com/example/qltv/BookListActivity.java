package com.example.qltv;

import static java.security.AccessController.getContext;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qltv.adapter.BookAdapter;
import com.example.qltv.model.Book;
import com.example.qltv.model.Information;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookListActivity extends AppCompatActivity {
    RecyclerView rcvBook;
    BookAdapter mBookAdapter;
    List<Book> mListBook;

    EditText edtSearch;

    DatabaseReference categoryRef;
    DatabaseReference booksRef;
    DatabaseReference informationRef;
    List<String> categoryList = new ArrayList<>();
    Spinner bookCategoryUpdate;

    private static final int PICK_IMAGE_REQUEST = 71;
    Uri filePath;
    ImageView bookImageView;
    FirebaseStorage storage;
    FirebaseDatabase database;
    StorageReference storageReference;
    String generatedBookId;
    String imageUrl;

    public interface ImageUploadCallback {
        void onImageUploaded(String imageUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        getSupportActionBar().setTitle("Quản lý sách");
        addControl();
        getListBook();

        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        Spinner spinnerStatusBook = findViewById(R.id.spinnerStatusBook);

        // Dữ liệu thể loại sách
        List<String> categories = Arrays.asList("--Tất cả thể loại--","Kỹ năng sống", "Thiếu nhi", "Lịch sử", "Trinh thám", "Kinh dị", "Giáo dục");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        List<String> statuses = Arrays.asList("--Tình trạng--", "Còn sách", "Hết sách");
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusBook.setAdapter(statusAdapter);

        // Thêm TextWatcher để thực hiện tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi phương thức filterBooks mỗi khi văn bản thay đổi
                String searchText = s.toString();  // Lấy văn bản tìm kiếm
                String selectedCategory = spinnerCategory.getSelectedItem().toString();  // Lấy thể loại được chọn từ Spinner
                String selectedStatus = spinnerStatusBook.getSelectedItem().toString();

                if (selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, null);
                } else if (!selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, selectedCategory, null);
                } else if (selectedCategory.equals("--Tất cả thể loại--") && !selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, selectedStatus);
                } else {
                    mBookAdapter.filterBooks(searchText, selectedCategory, selectedStatus);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = categories.get(position);
                String searchText = edtSearch.getText().toString();  // Lấy văn bản tìm kiếm hiện tại
                String selectedStatus = spinnerStatusBook.getSelectedItem().toString();

                if (selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, null);
                } else if (!selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, selectedCategory, null);
                } else if (selectedCategory.equals("--Tất cả thể loại--") && !selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, selectedStatus);
                } else {
                    mBookAdapter.filterBooks(searchText, selectedCategory, selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Không cần xử lý ở đây nữa vì Spinner luôn có giá trị được chọn
            }
        });

        spinnerStatusBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = statuses.get(position);
                String searchText = edtSearch.getText().toString();  // Lấy văn bản tìm kiếm hiện tại
                String selectedCategory = spinnerCategory.getSelectedItem().toString();

                if (selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, null);
                } else if (!selectedCategory.equals("--Tất cả thể loại--") && selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, selectedCategory, null);
                } else if (selectedCategory.equals("--Tất cả thể loại--") && !selectedStatus.equals("--Tình trạng--")) {
                    mBookAdapter.filterBooks(searchText, null, selectedStatus);
                } else {
                    mBookAdapter.filterBooks(searchText, selectedCategory, selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
            Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.nav_home) {
            Intent intent = new Intent(BookListActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControl() {
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseDatabase.getInstance();
        booksRef = database.getReference("Book");

        categoryRef = database.getReference("Category");

        informationRef = database.getReference("Information");

        edtSearch = findViewById(R.id.edtSearch);

        rcvBook = findViewById(R.id.rcvBook);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BookListActivity.this);
        rcvBook.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(BookListActivity.this, DividerItemDecoration.VERTICAL);
        rcvBook.addItemDecoration(dividerItemDecoration);

        mListBook = new ArrayList<>();

        mBookAdapter = new BookAdapter(mListBook, new BookAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Book book = mListBook.get(position);
                generatedBookId = book.getId();
                showUpdateDialog(book.getId(), book.getName(), book.getImage(), book.getQuantity(), book.getAuthor(), book.getCategory(), book.getDescription());
            }
        }, new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Book book = mListBook.get(position);
                Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", book.getId());
                intent.putExtra("bookImage", book.getImage());
                intent.putExtra("bookName", book.getName());
                intent.putExtra("bookAuthor", book.getAuthor());
                intent.putExtra("bookQuantity", book.getQuantity());
                intent.putExtra("bookCategory", book.getCategory());
                intent.putExtra("bookDescription", book.getDescription());
                intent.putExtra("bookStatus", book.getStatus());
                startActivity(intent);
            }
        });
        rcvBook.setAdapter(mBookAdapter);


    }

    private void getListBook() {
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListBook != null) {
                    mListBook.clear();
                }
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Book book = dataSnapshot.getValue(Book.class);
                        mListBook.add(book);
                    }
                    // Cập nhật mListBookFull để lưu trữ toàn bộ danh sách
                    mBookAdapter.updateFullList(new ArrayList<>(mListBook));
                    mBookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookListActivity.this, "Lỗi hiển thị", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm load thể loại sách từ Firebase và đưa vào Spinner
    private void loadCategories() {
        categoryList.clear(); // Xóa danh sách cũ
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của các thể loại và thêm vào danh sách
                    String category = snapshot.getValue(String.class);
                    categoryList.add(category);
                }

                // Tạo adapter và gán vào Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BookListActivity.this,
                        android.R.layout.simple_spinner_item, categoryList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bookCategoryUpdate.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BookListActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
    private void uploadImage(final ImageUploadCallback callback) {
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
                                    imageUrl = uri.toString();
                                    if (imageUrl != null) {
                                        callback.onImageUploaded(imageUrl); // Truyền URL qua callback
                                    }
                                }
                            });
                            startActivity(new Intent(BookListActivity.this, BookListActivity.class));
                            Toast.makeText(BookListActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BookListActivity.this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showUpdateDialog(String id, String name, String image, int quantity, String author, String category, String description) {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(BookListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.update_book, null);
        mDialog.setView(mDialogView);

        EditText bookIdUpdate = mDialogView.findViewById(R.id.book_id_update);
        EditText bookNameUpdate = mDialogView.findViewById(R.id.book_name_update);
        bookImageView = mDialogView.findViewById(R.id.book_image_update);
        EditText bookQuantityUpdate = mDialogView.findViewById(R.id.book_quantity_update);
        EditText bookAuthorUpdate = mDialogView.findViewById(R.id.book_author_update);
        bookCategoryUpdate = mDialogView.findViewById(R.id.book_category_spinner_update);
        EditText bookDescriptionUpdate = mDialogView.findViewById(R.id.book_description_update);

        bookImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        loadCategories();

        Button btnUpdate = mDialogView.findViewById(R.id.btnUpdate);
        Button btnDelete = mDialogView.findViewById(R.id.btnDelete);

        bookIdUpdate.setText(String.valueOf(id));
        bookIdUpdate.setEnabled(false);
        bookNameUpdate.setText(name);
        Glide.with(this).load(image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Không lưu cache trên đĩa
                .skipMemoryCache(true)                      // Không lưu cache trong bộ nhớ
                .into(bookImageView);
        bookQuantityUpdate.setText(String.valueOf(quantity));
        bookAuthorUpdate.setText(author);
        bookDescriptionUpdate.setText(description);

        mDialog.show();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = bookNameUpdate.getText().toString();
                String newQuantityText = bookQuantityUpdate.getText().toString();
                String newAuthor = bookAuthorUpdate.getText().toString();
                String newCategory = bookCategoryUpdate.getSelectedItem().toString();
                String newDescription = bookDescriptionUpdate.getText().toString();

                final String[] imageUrl2 = new String[1];
                imageUrl2[0] = image;

                uploadImage(new ImageUploadCallback() {
                    @Override
                    public void onImageUploaded(String imageUrl) {
                        imageUrl2[0] = imageUrl;
                    }
                });

                if (TextUtils.isEmpty(newName)) {
                    bookNameUpdate.setError("Vui lòng nhập tên sách");
                    return;
                }

                int newQuantity;
                try {
                    newQuantity = Integer.parseInt(newQuantityText);
                } catch (NumberFormatException e) {
                    bookQuantityUpdate.setError("Số lượng không hợp lệ");
                    return;
                }

                if (TextUtils.isEmpty(newAuthor)) {
                    bookAuthorUpdate.setError("Vui lòng nhập tên tác giả");
                    return;
                }

                if (TextUtils.isEmpty(newDescription)) {
                    bookDescriptionUpdate.setError("Vui lòng nhập mô tả");
                    return;
                }
                if (newQuantity > 0) {
                    updateData(id, newName, imageUrl2[0], newQuantity, newAuthor, newCategory, newDescription, "Còn sách");
                }
                else {
                    updateData(id, newName, imageUrl2[0], newQuantity, newAuthor, newCategory, newDescription, "Hết sách");
                }

                informationRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Information information = dataSnapshot.getValue(Information.class);
                                    if (information.getIdBook().equals(id) && !name.equals(newName)) {
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("nameBook", newName);
                                        informationRef.child(information.getId()).updateChildren(updates);
                                    }
                                }
                            }
                        }
                    }
                });

                bookNameUpdate.setText("");
                bookImageView.setImageResource(0);
                bookQuantityUpdate.setText("");
                bookAuthorUpdate.setText("");
                bookDescriptionUpdate.setText("");
                if (imageUrl2[0] == image) {
                    startActivity(new Intent(BookListActivity.this, BookListActivity.class));
                    Toast.makeText(BookListActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteRecord(id);
                AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);
                builder.setTitle("Xóa sách");
                builder.setMessage("Bạn có chắc muốn xóa sách này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        informationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    deleteRecord(id); // Nếu không có dữ liệu nào tồn tại, có thể xóa ngay
                                    return;
                                }
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Information information = dataSnapshot.getValue(Information.class);
                                    if (information != null && information.getIdBook().equals(id) && !information.getStatus().equals("Đã trả")) {
                                        showToast("Không thể xóa sách đang có người mượn");
                                        return;
                                    }
                                }
                                deleteRecord(id);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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

    private void updateData(String id, String name, String image, int quantity, String author, String category, String description, String status) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Book").child(id);
        Book book = new Book(id, name, image, quantity, author, category, description, status);
        DbRef.setValue(book);
    }

    private void deleteRecord(String id) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Book").child(id);
        Task<Void> mTask = DbRef.removeValue();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("Xóa sách thành công");
                mBookAdapter.notifyDataSetChanged();
                startActivity(new Intent(BookListActivity.this, BookListActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Xóa sách thất bại");
                //startActivity(new Intent(getContext(), DataManagementFragment.class));
            }
        });
        storageReference.child("images/" + id).delete();

    }

    private void showToast(String message) {
        Toast.makeText(BookListActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}