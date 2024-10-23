package com.example.qltv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qltv.R;
import com.example.qltv.model.Student;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> mListStudent;
    private List<Student> mListStudentFull;

    public StudentAdapter(List<Student> mListStudent, OnItemLongClickListener onItemLongClickListener) {
        this.mListStudent = mListStudent;
        mListStudentFull = new ArrayList<>(mListStudent);
        this.onItemLongClickListener = onItemLongClickListener;
    }

    // Interface for long click
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }


    private OnItemLongClickListener onItemLongClickListener;

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.example.qltv.R.layout.item_student, parent, false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = mListStudent.get(position);
        if (student == null)
            return;

        holder.studentName.setText(student.getTenSV());
        holder.studentId.setText(student.getMaSV());
        holder.studentPhone.setText(student.getSdtSV());
    }

    @Override
    public int getItemCount() {
        if (mListStudent != null)
            return mListStudent.size();
        return 0;
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, studentId, studentPhone;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.tv_student_name);
            studentId = itemView.findViewById(R.id.tv_student_id);
            studentPhone = itemView.findViewById(R.id.tv_student_phone);

            // Handle long click
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemLongClickListener.onItemLongClick(position);
                        }
                    }
                    return true; // Tiêu thụ sự kiện long click
                }
            });
        }
    }

    // Filter method for searching
    public void filter(String text) {
        mListStudent.clear();
        if (text.isEmpty()) {
            mListStudent.addAll(mListStudentFull);
        } else {
            String filterPattern = normalizeString(text.trim());

            for (Student student : mListStudentFull) {
                String studentName = normalizeString(student.getTenSV());
                String studentId = normalizeString(student.getMaSV());

                // Tìm kiếm theo cả tên và mã sinh viên
                if (studentName.contains(filterPattern) || studentId.contains(filterPattern)) {
                    mListStudent.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }


    public void updateFullList(List<Student> fullList) {
        this.mListStudentFull = fullList;
    }

    public String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public String normalizeString(String input) {
        input = input.toLowerCase();
        // Loại bỏ dấu tiếng Việt
        String normalized = removeDiacritics(input);

        // Thay thế các ký tự đặc biệt
        normalized = normalized.replaceAll("đ", "d");
        normalized = normalized.replaceAll("[âáàạảã]", "a");
        normalized = normalized.replaceAll("[êéèẹẻẽ]", "e");
        normalized = normalized.replaceAll("[ôơóòọỏõ]", "o");
        normalized = normalized.replaceAll("[ưúùụủũ]", "u");
        normalized = normalized.replaceAll("[íìịỉĩ]", "i");
        normalized = normalized.replaceAll("[ýỳỵỷỹ]", "y");

        return normalized;
    }
}

