package com.example.qltv.adapter;

import android.graphics.Color;
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
import com.example.qltv.model.Book;
import com.example.qltv.model.Information;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.InformationViewHolder> {
    private List<Information> mListInformaion;
    private List<Information> mListInformationFull;

    @NonNull
    @Override
    public InformationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_information, parent, false);

        return new InformationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InformationViewHolder holder, int position) {
        Information information = mListInformaion.get(position);
        if (information == null) {
            return;
        }
        Glide.with(holder.itemView.getContext()).load(information.getImageBook())
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Không lưu cache trên đĩa
                .skipMemoryCache(true)                      // Không lưu cache trong bộ nhớ
                .error(R.drawable.ic_error34)
                .into(holder.bookImageInformation);

        holder.bookNameInformation.setText(information.getNameBook());
        holder.borrowerInformation.setText(information.getBorrower());
        holder.borrowDateInformation.setText(information.getBorrowDate());
        holder.dueDateInformation.setText(information.getDueDate());
        if (compareDates(getCurrentDate(), information.getDueDate())
                && !information.getStatus().equals("Đã trả")) {
            holder.statusInformation.setText("Đã quá hạn");
        }
        else {
            holder.statusInformation.setText(information.getStatus());
        }

        if (information.getStatus().equals("Đang mượn")) {
            holder.statusInformation.setTextColor(Color.YELLOW);
        }
        else if (information.getStatus().equals("Đã trả")) {
            holder.statusInformation.setTextColor(Color.GREEN);
        }
        else {
            holder.statusInformation.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        if (mListInformaion!= null)
            return mListInformaion.size();
        return 0;
    }

    // Interface for long click
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private OnItemLongClickListener onItemLongClickListener;

    public InformationAdapter(List<Information> mListInformation, OnItemLongClickListener onItemLongClickListener) {
        this.mListInformaion = mListInformation;
        mListInformationFull = new ArrayList<>(mListInformaion);
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public class InformationViewHolder extends RecyclerView.ViewHolder {
        ImageView bookImageInformation;
        TextView bookNameInformation, borrowerInformation, borrowDateInformation, dueDateInformation, statusInformation;


        public InformationViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImageInformation = itemView.findViewById(R.id.book_image_information);
            bookNameInformation = itemView.findViewById(R.id.book_name_information);
            borrowerInformation = itemView.findViewById(R.id.borrower_information);
            borrowDateInformation = itemView.findViewById(R.id.borrow_date_information);
            dueDateInformation = itemView.findViewById(R.id.due_date_information);
            statusInformation = itemView.findViewById(R.id.status_information);

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

    // Hàm so sánh 2 chuỗi ngày theo định dạng dd-MM-yyyy
    public boolean compareDates(String dateStr1, String dateStr2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date date1 = dateFormat.parse(dateStr1);
            Date date2 = dateFormat.parse(dateStr2);

            // So sánh 2 ngày
            return date1.compareTo(date2) > 0; // trả về true nếu date1 lớn hơn date2
        } catch (Exception e) {
            e.printStackTrace();
            return false; // trả về false nếu có lỗi khi chuyển đổi chuỗi
        }
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(Calendar.getInstance().getTime());
    }

    // Filter method for searching
    public void filterInformations(String text, String selectedStatus) {
        mListInformaion.clear();

        if (text.isEmpty() && (selectedStatus == null || selectedStatus.equals("--Tình trạng--"))) {
            mListInformaion.addAll(mListInformationFull);  // Nếu không có bộ lọc, hiển thị tất cả sách
        } else {
            String filterPattern = normalizeString(text.trim());

            for (Information information : mListInformationFull) {
                String bookNameInformation = normalizeString(information.getNameBook());
                String borrowerInformation = normalizeString(information.getBorrower());
                String borrowDateInformation = normalizeString(information.getBorrowDate());
                String dueDateInformation = normalizeString(information.getDueDate());

                // Kiểm tra tên sách có chứa từ khóa tìm kiếm
                boolean matchesBookName = bookNameInformation.contains(filterPattern);
                boolean matchesBorrower = borrowerInformation.contains(filterPattern);
                boolean matchesBorrowDate = borrowDateInformation.contains(filterPattern);
                boolean matchesDueDate = dueDateInformation.contains(filterPattern);

                // Kiểm tra thể loại sách nếu có
                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("--Tình trạng--") || information.getStatus().equals(selectedStatus);

                // Nếu sách khớp với cả hai điều kiện
                if ((matchesBookName || matchesBorrower || matchesBorrowDate || matchesDueDate) && matchesStatus) {
                    mListInformaion.add(information);
                }
            }
        }
        notifyDataSetChanged();  // Cập nhật UI
    }

    public void updateFullList(List<Information> fullList) {
        this.mListInformationFull = fullList;
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
