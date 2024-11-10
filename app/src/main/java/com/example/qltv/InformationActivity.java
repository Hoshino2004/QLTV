package com.example.qltv;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.qltv.adapter.InformationAdapter;
import com.example.qltv.model.Book;
import com.example.qltv.model.Information;
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

public class InformationActivity extends AppCompatActivity {

    RecyclerView rcvInformation;
    InformationAdapter mInformationAdapter;
    List<Information> mListInformation;

    EditText edtSearchInformation;
    DatabaseReference informationRef;
    DatabaseReference bookRef;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().setTitle("Thông tin mượn sách");
        addControl();
        getListInformation();

        Spinner spinnerStatus = findViewById(R.id.spinnerStatusInformation);

        // Dữ liệu thể loại sách
        List<String> statuses = Arrays.asList("--Tình trạng--", "Đang mượn", "Đã trả", "Đã quá hạn");
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Thêm TextWatcher để thực hiện tìm kiếm
        edtSearchInformation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi phương thức filterBooks mỗi khi văn bản thay đổi
                String searchText = s.toString();  // Lấy văn bản tìm kiếm
                String selectedStatus = spinnerStatus.getSelectedItem().toString();  // Lấy thể loại được chọn từ Spinner

                if (selectedStatus.equals("--Tình trạng--")) {
                    // Nếu chọn "Tất cả thể loại", lọc theo tên sách mà không lọc theo thể loại
                    mInformationAdapter.filterInformations(searchText, null);
                } else {
                    // Lọc theo tên sách và thể loại đã chọn
                    mInformationAdapter.filterInformations(searchText, selectedStatus);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedStatus = statuses.get(position);
                String searchText = edtSearchInformation.getText().toString();  // Lấy văn bản tìm kiếm hiện tại

                if (selectedStatus.equals("--Tình trạng--")) {
                    // Nếu chọn "Tất cả thể loại", lọc theo tên sách không có thể loại
                    mInformationAdapter.filterInformations(searchText, null);
                } else {
                    // Lọc theo tên sách và thể loại đã chọn
                    mInformationAdapter.filterInformations(searchText, selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Không cần xử lý ở đây nữa vì Spinner luôn có giá trị được chọn
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(InformationActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControl() {
        // Initialize Firebase Storage
        database = FirebaseDatabase.getInstance();
        informationRef = database.getReference("Information");
        bookRef = database.getReference("Book");

        edtSearchInformation = findViewById(R.id.edtSearchInformation);

        rcvInformation = findViewById(R.id.rcvInformation);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InformationActivity.this);
        rcvInformation.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(InformationActivity.this, DividerItemDecoration.VERTICAL);
        rcvInformation.addItemDecoration(dividerItemDecoration);

        mListInformation = new ArrayList<>();

        mInformationAdapter = new InformationAdapter(mListInformation, new InformationAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Information information = mListInformation.get(position);
                showUpdateDialog(information.getId(), information.getIdBook(), information.getStatus());
            }
        });
        rcvInformation.setAdapter(mInformationAdapter);


    }

    private void getListInformation() {
        informationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListInformation != null) {
                    mListInformation.clear();
                }

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Information information = dataSnapshot.getValue(Information.class);
                        mListInformation.add(information);
                    }
                    // Cập nhật mListBookFull để lưu trữ toàn bộ danh sách
                    mInformationAdapter.updateFullList(new ArrayList<>(mListInformation));
                    mInformationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InformationActivity.this, "Lỗi hiển thị", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(String id, String idBook, String status) {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(InformationActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.update_information, null);
        mDialog.setView(mDialogView);

        Button btnUpdateStatus = mDialogView.findViewById(R.id.btnUpdateStatus);
        Button btnDeleteInformation = mDialogView.findViewById(R.id.btnDeleteInformation);

        mDialog.show();
        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.equals("Đã trả")) {
                    showToast("Sách này đã trả rồi");
                    return;
                }
                bookRef.child(idBook).child("quantity").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int quantity = snapshot.getValue(Integer.class);
                            bookRef.child(idBook).child("quantity").setValue(quantity + 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Đã trả");
                informationRef.child(id).updateChildren(updates);
                startActivity(new Intent(InformationActivity.this, InformationActivity.class));
                showToast("Cập nhật tình trạng thành công");
            }
        });
        btnDeleteInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteRecord(id);
                AlertDialog.Builder builder = new AlertDialog.Builder(InformationActivity.this);
                builder.setTitle("Xóa thông tin mượn sách");
                builder.setMessage("Bạn có chắc muốn xóa thông tin mượn sách này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (status.equals("Đã trả")) {
                            deleteRecord(id);
                        } else if (status.equals("Đã quá hạn")) {
                            showToast("Không thể xóa thông tin mượn sách đã quá hạn");
                        } else {
                            showToast("Không thể xóa thông tin sách đang mượn");
                        }
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

    private void deleteRecord(String id) {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference("Information").child(id);
        Task<Void> mTask = DbRef.removeValue();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("Xóa thông tin mượn sách thành công");
                startActivity(new Intent(InformationActivity.this, InformationActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Xóa thông tin mượn sách thất bại");
                startActivity(new Intent(InformationActivity.this, InformationActivity.class));
            }
        });

    }

    private void showToast(String message) {
        Toast.makeText(InformationActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}