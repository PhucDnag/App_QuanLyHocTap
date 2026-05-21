package com.example.a9_btl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;

import java.util.List;

public class TeacherClassAdapter extends RecyclerView.Adapter<TeacherClassAdapter.ViewHolder> {

    private Context context;
    private List<String> classList;
    private DatabaseHelper dbHelper;
    private IOnClassClickListener listener;

    // Interface để xử lý sự kiện click
    public interface IOnClassClickListener {
        void onClick(String className);
    }

    public TeacherClassAdapter(Context context, List<String> classList, DatabaseHelper dbHelper, IOnClassClickListener listener) {
        this.context = context;
        this.classList = classList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String className = classList.get(position);

        // 1. Hiển thị tên lớp
        holder.tvClassName.setText("Lớp " + className);

        // 2. Lấy số lượng học sinh từ DB (DỮ LIỆU ĐỘNG)
        int count = dbHelper.getStudentCountByClass(className);
        holder.tvStudentCount.setText(count + " học sinh");

        // 3. Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onClick(className));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvStudentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
        }
    }
}