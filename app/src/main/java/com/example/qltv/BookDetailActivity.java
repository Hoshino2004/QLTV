package com.example.qltv;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class BookDetailActivity extends AppCompatActivity {
    ImageView bookImage;
    TextView bookName, bookQuantity,  bookCategory, bookAuthor, bookDescription;
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


    private void addEvents() {
        Intent intent = getIntent();
        Glide.with(this).load(intent.getStringExtra("bookImage")).error(R.drawable.ic_error34).into(bookImage);
        bookName.setText(intent.getStringExtra("bookName"));
        bookQuantity.setText(String.valueOf(intent.getIntExtra("bookQuantity", 0)));
        bookCategory.setText(intent.getStringExtra("bookCategory"));
        bookAuthor.setText(intent.getStringExtra("bookAuthor"));
        bookDescription.setText(intent.getStringExtra("bookDescription"));
    }

    private void addControls() {
        bookImage = findViewById(R.id.imgBookDetail);
        bookName = findViewById(R.id.tvBookTitleDetail);
        bookQuantity = findViewById(R.id.tvQuantityDetail);
        bookCategory = findViewById(R.id.tvCategoryDetail);
        bookAuthor = findViewById(R.id.tvAuthorDetail);
        bookDescription = findViewById(R.id.tvDescriptionDetail);
    }
}