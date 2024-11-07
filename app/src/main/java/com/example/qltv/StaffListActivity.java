package com.example.qltv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltv.adapter.StaffAdapter;
import com.example.qltv.adapter.StudentAdapter;
import com.example.qltv.model.Staff;
import com.example.qltv.model.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StaffListActivity extends AppCompatActivity {
    RecyclerView rcvStaff;
    StaffAdapter mStaffAdapter;
    List<Staff> mListStaff;

    DatabaseReference userRef;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_list);
        getSupportActionBar().setTitle("Quản lý nhân viên");
        addControl();
        getListStaff();
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
            Intent intent = new Intent(StaffListActivity.this, AddStaffActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.nav_home) {
            Intent intent = new Intent(StaffListActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControl() {
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("User");

        rcvStaff = findViewById(R.id.rcvStaff);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StaffListActivity.this);
        rcvStaff.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(StaffListActivity.this, DividerItemDecoration.VERTICAL);
        rcvStaff.addItemDecoration(dividerItemDecoration);

        mListStaff = new ArrayList<>();
        mStaffAdapter = new StaffAdapter(mListStaff);
        rcvStaff.setAdapter(mStaffAdapter);
    }

    private void getListStaff() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListStaff != null) {
                    mListStaff.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Staff staff = dataSnapshot.getValue(Staff.class);
                    if (!staff.getEmail().equals("thinh631631@gmail.com")) {
                        mListStaff.add(staff);
                    }
                }
                mStaffAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StaffListActivity.this, "Lỗi hiển thị", Toast.LENGTH_SHORT).show();
            }
        });
    }
}