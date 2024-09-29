package com.example.qltv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qltv.R;
import com.example.qltv.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder>{
    private List<Book> mListBook;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    private OnItemLongClickListener onItemLongClickListener;

    public BookAdapter(List<Book> mListSP, OnItemLongClickListener onItemLongClickListener) {
        this.mListBook = mListSP;
        this.onItemLongClickListener = onItemLongClickListener;
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
        Glide.with(holder.itemView.getContext()).load(book.getImage()).error(R.drawable.firefly).into(holder.bookImage);
        holder.bookName.setText(book.getName());
        holder.bookQuantity.setText(String.valueOf(book.getQuantity()));
        holder.bookAuthor.setText(book.getAuthor());
        holder.bookDescription.setText(book.getDescription());
        holder.bookCategory.setText(book.getCategory());
    }

    @Override
    public int getItemCount() {
        if (mListBook != null)
            return mListBook.size();
        return 0;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookName, bookQuantity, bookAuthor, bookDescription, bookCategory;
        ImageView bookImage;
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.book_image);
            bookName = itemView.findViewById(R.id.book_name);
            bookQuantity = itemView.findViewById(R.id.book_quantity);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookDescription = itemView.findViewById(R.id.book_description);
            bookCategory = itemView.findViewById(R.id.book_category);
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
}
