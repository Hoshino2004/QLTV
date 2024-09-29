package com.example.qltv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qltv.model.Book;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {
    Button btnThem;
    EditText bookIdAdd, bookNameAdd, bookImageAdd, bookQuantityAdd,
            bookDescriptionAdd, bookAuthorAdd, bookCategoryAdd;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        addControls();
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String image = bookImageAdd.getText().toString().trim();
                String id = bookIdAdd.getText().toString().trim();
                String name = bookNameAdd.getText().toString().trim();
                int quantity = Integer.parseInt(bookQuantityAdd.getText().toString().trim());
                String description = bookDescriptionAdd.getText().toString().trim();
                String author = bookAuthorAdd.getText().toString().trim();
                String category = bookCategoryAdd.getText().toString().trim();
                Book book = new Book(id, name, image, quantity, author, category, description);
                Toast.makeText(AddBookActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                addBook(book);
            }
        });

    }

    private void addControls() {
        bookIdAdd = findViewById(R.id.book_id_add);
        bookNameAdd = findViewById(R.id.book_name_add);
        bookImageAdd = findViewById(R.id.book_image_add);
        bookQuantityAdd = findViewById(R.id.book_quantity_add);
        bookDescriptionAdd = findViewById(R.id.book_description_add);
        bookAuthorAdd = findViewById(R.id.book_author_add);
        bookCategoryAdd = findViewById(R.id.book_category_add);
        btnThem = findViewById(R.id.btnThem);
    }


    private void addBook(Book book) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Books");

        String pathObject = book.getId();
        myRef.child(pathObject).setValue(book, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

            }
        });
    }
}