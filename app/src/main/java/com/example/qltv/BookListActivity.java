package com.example.qltv;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltv.adapter.BookAdapter;
import com.example.qltv.model.Book;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    RecyclerView rcvBook;
    BookAdapter mBookAdapter;
    List<Book> mListBook;

    EditText edtSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        addControl();
        getListBook();
        // Thêm TextWatcher để thực hiện tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBookAdapter.filter(s.toString()); // Lọc danh sách theo text người dùng nhập
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
        if (id == R.id.nav_add){
            Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControl() {
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
                showUpdateDialog(book.getId(), book.getName(), book.getImage(), book.getQuantity(), book.getAuthor(), book.getCategory(), book.getDescription());
            }
        });
        rcvBook.setAdapter(mBookAdapter);


    }
    private void getListBook()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Books");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListBook != null){
                    mListBook.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Book book = dataSnapshot.getValue(Book.class);
                    mListBook.add(book);
                }
                // Cập nhật mListBookFull để lưu trữ toàn bộ danh sách
                mBookAdapter.updateFullList(new ArrayList<>(mListBook));
                mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookListActivity.this,"Lỗi hiển thị",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showUpdateDialog(String id, String name, String image, int quantity, String author, String category, String description)
    {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(BookListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.update_database, null);
        mDialog.setView(mDialogView);

        EditText bookIdUpdate = mDialogView.findViewById(R.id.book_id_update);
        EditText bookNameUpdate = mDialogView.findViewById(R.id.book_name_update);
        EditText bookImageUpdate = mDialogView.findViewById(R.id.book_image_update);
        EditText bookQuantityUpdate = mDialogView.findViewById(R.id.book_quantity_update);
        EditText bookAuthorUpdate = mDialogView.findViewById(R.id.book_author_update);
        EditText bookCategoryUpdate = mDialogView.findViewById(R.id.book_category_update);
        EditText bookDescriptionUpdate = mDialogView.findViewById(R.id.book_description_update);

        Button btnUpdate = mDialogView.findViewById(R.id.btnUpdate);
        Button btnDelete = mDialogView.findViewById(R.id.btnDelete);

        bookIdUpdate.setText(String.valueOf(id));
        bookIdUpdate.setEnabled(false);
        bookNameUpdate.setText(name);
        bookImageUpdate.setText(image);
        bookQuantityUpdate.setText(String.valueOf(quantity));
        bookAuthorUpdate.setText(author);
        bookCategoryUpdate.setText(category);
        bookDescriptionUpdate.setText(description);

        mDialog.show();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = bookNameUpdate.getText().toString();
                String newImage = bookImageUpdate.getText().toString();
                int newQuantity = Integer.parseInt(bookQuantityUpdate.getText().toString());
                String newAuthor = bookAuthorUpdate.getText().toString();
                String newCategory = bookCategoryUpdate.getText().toString();
                String newDescription = bookDescriptionUpdate.getText().toString();
                updateData(id, newName, newImage, newQuantity, newAuthor, newCategory, newDescription);
                Toast.makeText(BookListActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                bookNameUpdate.setText("");
                bookImageUpdate.setText("");
                bookQuantityUpdate.setText("");
                bookAuthorUpdate.setText("");
                bookCategoryUpdate.setText("");
                bookDescriptionUpdate.setText("");
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteRecord(id);
                AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Bạn có chắc muốn xóa sách này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecord(id);}
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
    private void updateData(String id, String name, String image, int quantity, String author, String category, String description){
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Books").child(id);
        Book book = new Book(id, name, image, quantity, author, category, description);
        DbRef.setValue(book);
    }

    private void deleteRecord(String id) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Books").child(id);
        Task<Void> mTask = DbRef.removeValue();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("Deleted");
                mBookAdapter.notifyDataSetChanged();
                //  startActivity(new Intent(getContext(), DataManagementFragment.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Error deleting record");
                //startActivity(new Intent(getContext(), DataManagementFragment.class));
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(BookListActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}