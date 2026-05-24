package com.example.androidlearn.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.course.LessonActivity;
// Nhớ import LessonActivity hoặc màn hình chi tiết bài học của bạn
// import com.example.androidlearn.ui.course.LessonDetailActivity;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Chapter> list;
    private Context context;
    private DatabaseHelper dbHelper;
    private int myUserId;

    public CourseAdapter(Context context, List<Chapter> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DatabaseHelper(context);

        // Lấy ID người dùng
        this.myUserId = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("KEY_USER_ID", 1);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Chapter chapter = list.get(position);
        holder.tvName.setText(chapter.getTenChuong());

        // --- LOGIC MỜ / TỎ ---
        boolean isUnlocked = dbHelper.isChapterUnlocked(myUserId, chapter.getMaChuong());

        if (isUnlocked) {
            // MỞ KHOÁ: Sáng rõ, bấm được
            holder.itemView.setAlpha(1.0f);
            holder.tvStatus.setText("Mở");
            holder.tvStatus.setTextColor(Color.parseColor("#1976D2")); // Màu xanh

            holder.itemView.setOnClickListener(v -> {
                // Chuyển sang màn hình chi tiết bài học (PDF/Video...)
                 Intent intent = new Intent(context, LessonActivity.class);
                 intent.putExtra("CHAPTER_ID", chapter.getMaChuong());
                 context.startActivity(intent);

                Toast.makeText(context, "Vào học chương: " + chapter.getTenChuong(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // BỊ KHOÁ
            holder.itemView.setAlpha(0.5f);
            holder.tvStatus.setText("Khoá");
            holder.tvStatus.setTextColor(android.graphics.Color.GRAY);

            holder.itemView.setOnClickListener(v -> {
                // Thông báo rõ ràng hơn
                Toast.makeText(context,
                        "Bạn phải đạt >5 điểm Quiz VÀ nộp bài tập chương trước mới được mở!",
                        Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChapterName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}