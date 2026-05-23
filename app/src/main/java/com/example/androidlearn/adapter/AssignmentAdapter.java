package com.example.androidlearn.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.assignment.AssignmentActivity;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    private Context context;
    private List<Chapter> chapters;
    private DatabaseHelper db;
    private int userId;

    public AssignmentAdapter(Context context, List<Chapter> chapters, DatabaseHelper db, int userId) {
        this.context = context;
        this.chapters = chapters;
        this.db = db;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng đúng file layout bạn đã có: item_assignment
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter c = chapters.get(position);
        holder.tvTitle.setText(c.getTenChuong());

        // 1. Kiểm tra khóa chương (Chương 1 luôn mở)
        boolean isUnlocked = db.isChapterUnlocked(userId, c.getMaChuong());
        if (c.getMaChuong() == 1) isUnlocked = true;

        if (!isUnlocked) {
            // --- ĐANG KHÓA ---
            holder.tvStatus.setText("🔒 Đang khóa");
            // Set màu xám
            if(holder.card != null) holder.card.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.itemView.setEnabled(false);
            return;
        }

        // 2. Lấy thông tin bài làm: [Text, File, Grade, Feedback]
        String[] submission = db.getSubmissionFullDetail(userId, c.getMaChuong());

        if (submission == null) {
            // --- CHƯA NỘP ---
            holder.tvStatus.setText("Chưa nộp bài");
            holder.tvStatus.setTextColor(Color.RED);
            if(holder.card != null) holder.card.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AssignmentActivity.class);
                intent.putExtra("CHAPTER_ID", c.getMaChuong());
                context.startActivity(intent);
            });
        } else {
            // --- ĐÃ NỘP ---
            double grade = -1;
            try {
                grade = Double.parseDouble(submission[2]);
            } catch (Exception e) { grade = -1; }
            String feedback = submission[3];

            if (grade == -1) {
                // --- CHỜ CHẤM ---
                holder.tvStatus.setText("Đã nộp (Chờ chấm)");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Cam
                if(holder.card != null) holder.card.setCardBackgroundColor(Color.parseColor("#FFF3E0"));

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, AssignmentActivity.class);
                    intent.putExtra("CHAPTER_ID", c.getMaChuong());
                    context.startActivity(intent);
                });
            } else {
                // --- ĐÃ CÓ ĐIỂM ---
                holder.tvStatus.setText("Điểm: " + grade);
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá đậm
                holder.tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
                if(holder.card != null) holder.card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));

                // Bấm vào để xem Lời phê
                double finalGrade = grade;
                holder.itemView.setOnClickListener(v -> {
                    showFeedbackDialog(c.getTenChuong(), finalGrade, feedback);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    // Hàm hiển thị Popup lời phê
    private void showFeedbackDialog(String title, double score, String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            feedback = "Giáo viên không có nhận xét gì thêm.";
        }
        new AlertDialog.Builder(context)
                .setTitle("Kết quả: " + title)
                .setMessage("⭐ ĐIỂM SỐ: " + score + "\n\n💬 LỜI PHÊ:\n" + feedback)
                .setPositiveButton("Đóng", null)
                .setIcon(android.R.drawable.star_big_on)
                .show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;
        CardView card;

        public ViewHolder(@NonNull View v) {
            super(v);
            // Ánh xạ View từ file item_assignment.xml
            // Bạn nhớ kiểm tra ID bên file XML phải khớp với ở đây nhé!
            tvTitle = v.findViewById(R.id.tvAssignmentTitle);
            tvStatus = v.findViewById(R.id.tvAssignmentStatus);

            // Tìm CardView (để đổi màu nền)
            // Nếu root view là CardView thì ép kiểu, nếu không thì findViewById
            if (v instanceof CardView) {
                card = (CardView) v;
            } else {
                // Nếu CardView nằm bên trong, hãy chắc chắn ID là cardView hoặc sửa lại cho khớp
                try {
                    card = v.findViewById(R.id.cardView);
                } catch (Exception e) {}
            }
        }
    }
}