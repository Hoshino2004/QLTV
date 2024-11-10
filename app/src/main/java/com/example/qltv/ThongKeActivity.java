package com.example.qltv;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qltv.model.Information;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        getSupportActionBar().setTitle("Thống kê top 3 sách mượn nhiều nhất");

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Information"); // Đường dẫn tới phần "Information" trong Firebase

        // Khởi tạo BarChart
        barChart = findViewById(R.id.barChart);

        // Lấy dữ liệu từ Firebase
        getBorrowRecordsFromFirebase();

    }

    // Lấy dữ liệu mượn sách từ Firebase Realtime Database
    private void getBorrowRecordsFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Tạo một danh sách chứa các bản ghi mượn sách
                List<Information> borrowRecords = new ArrayList<>();
                Map<String, String> bookNamesMap = new HashMap<>(); // Bản đồ lưu tên sách

                // Lặp qua tất cả các bản ghi trong "Information"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy dữ liệu từng mượn sách
                    Information information = snapshot.getValue(Information.class);
                    if (information != null) {
                        borrowRecords.add(information);
                        // Lưu tên sách vào map
                        bookNamesMap.put(information.getIdBook(), information.getNameBook());
                    }
                }

                // Gọi hàm thống kê top 3 sách mượn nhiều nhất theo tháng
                Map<String, Map<String, Integer>> monthBookCountMap = getBookBorrowCount(borrowRecords);
                List<String> topBooks = getTop3BooksByMonth(monthBookCountMap, "2024-11");

                // Hiển thị kết quả lên biểu đồ với tên sách
                showTopBooksOnChart(topBooks, monthBookCountMap.get("2024-11"), bookNamesMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi khi lấy dữ liệu từ Firebase
                Log.e("Firebase", "Error getting data", databaseError.toException());
            }
        });
    }

    // Thống kê số lần mượn sách theo tháng (YYYY-MM)
    private Map<String, Map<String, Integer>> getBookBorrowCount(List<Information> borrowRecords) {
        Map<String, Map<String, Integer>> monthBookCountMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Lặp qua tất cả các bản ghi mượn sách
        for (Information information : borrowRecords) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateFormat.parse(information.getBorrowDate()));
                int month = calendar.get(Calendar.MONTH) + 1; // Lấy tháng (1-12)
                int year = calendar.get(Calendar.YEAR); // Lấy năm

                // Đảm bảo tháng có định dạng 2 chữ số
                String formattedMonth = String.format("%02d", month);
                String monthYear = year + "-" + formattedMonth;

                // Nếu chưa có map cho tháng này, tạo mới
                if (!monthBookCountMap.containsKey(monthYear)) {
                    monthBookCountMap.put(monthYear, new HashMap<>());
                }

                Map<String, Integer> bookCountMap = monthBookCountMap.get(monthYear);

                // Tăng số lần mượn cho sách này trong tháng này
                bookCountMap.put(information.getIdBook(), bookCountMap.getOrDefault(information.getIdBook(), 0) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return monthBookCountMap;
    }

    // Lấy Top 3 sách mượn nhiều nhất trong tháng
    private List<String> getTop3BooksByMonth(Map<String, Map<String, Integer>> monthBookCountMap, String monthYear) {
        // Lấy map của tháng cụ thể
        Map<String, Integer> bookCountMap = monthBookCountMap.get(monthYear);
        if (bookCountMap == null) {
            return new ArrayList<>(); // Trả về danh sách rỗng nếu không có dữ liệu
        }

        // Sắp xếp các sách theo số lần mượn (từ cao đến thấp)
        List<Map.Entry<String, Integer>> sortedBooks = new ArrayList<>(bookCountMap.entrySet());
        sortedBooks.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Lấy Top 3 sách
        List<String> topBooks = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sortedBooks.size()); i++) {
            topBooks.add(sortedBooks.get(i).getKey()); // Lấy ID sách
        }

        return topBooks;
    }

    // Hiển thị top 3 sách lên biểu đồ với tên sách
    private void showTopBooksOnChart(List<String> topBooks, Map<String, Integer> bookCountMap, Map<String, String> bookNamesMap) {
        // Dữ liệu cho biểu đồ
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> bookNames = new ArrayList<>();

        int index = 0;
        for (String bookId : topBooks) {
            int count = bookCountMap.getOrDefault(bookId, 0);
            entries.add(new BarEntry(index++, count));
            // Lấy tên sách từ bản đồ bookNamesMap
            String bookName = bookNamesMap.get(bookId);
            bookNames.add(bookName);
        }

        // Tạo dữ liệu cho biểu đồ
        BarDataSet barDataSet = new BarDataSet(entries, "Số lần mượn sách");
        BarData barData = new BarData(barDataSet);

        // Điều chỉnh kích thước chữ của Legend (label "Books Borrowed")
        barChart.getLegend().setTextSize(18f); // Tăng kích thước chữ thành 14sp hoặc bất kỳ kích thước nào bạn muốn

        // Điều chỉnh kích thước chữ hiển thị trên thanh
        barDataSet.setValueTextSize(18f); // Tăng kích thước chữ cho dễ đọc

        // Điều chỉnh độ rộng của thanh
        barData.setBarWidth(0.8f); // Giá trị này có thể từ 0.1 đến 1.0, tùy thuộc vào độ rộng mong muốn

        // Hiển thị biểu đồ
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false); // Vô hiệu hóa Description Label

        // Tạo khoảng đệm phía trên để tránh chữ bị che mất
        barChart.setExtraTopOffset(20f); // Tăng khoảng đệm phía trên (đơn vị là dp)

        // Tăng kích thước chữ trên trục hoành (trục X)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(14f); // Tăng kích thước chữ trên trục hoành

        // Tăng kích thước chữ cho trục tung
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextSize(18f); // Điều chỉnh kích thước chữ cho trục tung bên trái

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setTextSize(18f); // Điều chỉnh kích thước chữ cho trục tung bên phải (nếu dùng)

        barChart.invalidate(); // Cập nhật biểu đồ

        // Hiển thị nhãn (tên sách) trên trục X
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(bookNames));
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
            Intent intent = new Intent(ThongKeActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}