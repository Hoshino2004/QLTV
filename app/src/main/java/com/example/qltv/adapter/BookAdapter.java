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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> mListBook;
    private List<Book> mListBookFull;
    DatabaseReference bookRef;
    FirebaseDatabase database;

    // Interface for long click
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    // Interface for click
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemClickListener;

    public BookAdapter(List<Book> mListBook, OnItemLongClickListener onItemLongClickListener, OnItemClickListener onItemClickListener) {
        this.mListBook = mListBook;
        mListBookFull = new ArrayList<>(mListBook);
        this.onItemLongClickListener = onItemLongClickListener;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.example.qltv.R.layout.item_book, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = mListBook.get(position);
        if (book == null)
            return;

        Glide.with(holder.itemView.getContext()).load(book.getImage())
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Không lưu cache trên đĩa
                .skipMemoryCache(true)                      // Không lưu cache trong bộ nhớ
                .error(R.drawable.ic_error34)
                .into(holder.bookImage);

        holder.bookName.setText(book.getName());
        holder.bookQuantity.setText(String.valueOf(book.getQuantity()));
        holder.bookAuthor.setText(book.getAuthor());
        holder.bookCategory.setText(book.getCategory());

        database = FirebaseDatabase.getInstance();
        bookRef = database.getReference("Book");

        if (book.getQuantity() > 0) {
            bookRef.child(book.getId()).child("status").setValue("Còn sách");
            holder.bookStatus.setText(book.getStatus());
        }
        else {
            bookRef.child(book.getId()).child("status").setValue("Hết sách");
            holder.bookStatus.setText(book.getStatus());
        }

        if (book.getStatus().equals("Còn sách")) {
            holder.bookStatus.setTextColor(Color.GREEN);
        }
        else {
            holder.bookStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        if (mListBook != null)
            return mListBook.size();
        return 0;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookName, bookQuantity, bookAuthor, bookCategory, bookStatus;
        ImageView bookImage;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.book_image);
            bookName = itemView.findViewById(R.id.book_name);
            bookQuantity = itemView.findViewById(R.id.book_quantity);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookCategory = itemView.findViewById(R.id.book_category);
            bookStatus = itemView.findViewById(R.id.book_status);

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

            // Handle normal click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    // Filter method for searching
    public void filterBooks(String text, String selectedCategory, String selectedStatus) {
        mListBook.clear();

        if (text.isEmpty() && (selectedCategory == null || selectedCategory.equals("--Tất cả thể loại--"))
                && (selectedStatus == null || selectedStatus.equals("--Tình trạng--"))) {
            mListBook.addAll(mListBookFull);  // Nếu không có bộ lọc, hiển thị tất cả sách
        } else {
            String filterPattern = normalizeString(text.trim());

            for (Book book : mListBookFull) {
                String bookName = normalizeString(book.getName());
                String bookAuthor = normalizeString(book.getAuthor());

                // Kiểm tra tên sách có chứa từ khóa tìm kiếm
                boolean matchesName = bookName.contains(filterPattern);
                boolean matchesAuthor = bookAuthor.contains(filterPattern);

                // Kiểm tra thể loại sách nếu có
                boolean matchesCategory = selectedCategory == null || selectedCategory.equals("--Tất cả thể loại--") || book.getCategory().equals(selectedCategory);
                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("--Tình trạng--") || book.getStatus().equals(selectedStatus);

                // Nếu sách khớp với cả hai điều kiện
                if ((matchesName || matchesAuthor) && matchesStatus && matchesCategory) {
                    mListBook.add(book);
                }
            }
        }
        notifyDataSetChanged();  // Cập nhật UI
    }



    public void updateFullList(List<Book> fullList) {
        this.mListBookFull = fullList;
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

