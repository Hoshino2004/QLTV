package com.example.qltv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltv.R;
import com.example.qltv.model.Book;
import com.example.qltv.model.Staff;

import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {
    private List<Staff> mListStaff;

    public StaffAdapter(List<Staff> mListStaff) {
        this.mListStaff = mListStaff;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);

        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        Staff staff = mListStaff.get(position);
        if (staff == null) {
            return;
        }

        holder.staffEmail.setText(staff.getEmail());
        holder.staffPassword.setText(staff.getPassword());
        holder.staffName.setText(staff.getName());
        holder.staffPhone.setText(staff.getPhone());
    }

    @Override
    public int getItemCount() {
        if (mListStaff != null)
            return mListStaff.size();
        return 0;
    }

    public class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView staffEmail, staffPassword, staffName, staffPhone;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            staffEmail = itemView.findViewById(R.id.tv_staff_email);
            staffPassword = itemView.findViewById(R.id.tv_staff_password);
            staffName = itemView.findViewById(R.id.tv_staff_name);
            staffPhone = itemView.findViewById(R.id.tv_staff_phone);
        }
    }
}
