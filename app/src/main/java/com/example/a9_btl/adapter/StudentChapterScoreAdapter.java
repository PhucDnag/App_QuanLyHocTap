package com.example.a9_btl.adapter; // Hoặc package adapter tùy bạn đặt

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import java.util.List;

public class StudentChapterScoreAdapter extends RecyclerView.Adapter<StudentChapterScoreAdapter.ViewHolder> {

    private Context context;
    private List<Chapter> chapterList;
    private DatabaseHelper dbHelper;
    private int studentId;

    public StudentChapterScoreAdapter(Context context, List<Chapter> chapterList, DatabaseHelper dbHelper, int studentId) {
        this.context = context;
        this.chapterList = chapterList;
        this.dbHelper = dbHelper;
        this.studentId = studentId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn đã có file layout: item_student_chapter_score.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_chapter_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.tvName.setText(chapter.getTenChuong());

        // Lấy điểm số
        int score = dbHelper.getQuizScore(studentId, chapter.getMaChuong());
        int totalQuestions = dbHelper.getQuestionsByChapter(chapter.getMaChuong()).size();

        if (totalQuestions == 0) {
            holder.tvScore.setText("Không có bài");
            holder.tvScore.setTextColor(android.graphics.Color.GRAY);
        } else if (score == -1) {
            holder.tvScore.setText("Chưa làm");
            holder.tvScore.setTextColor(android.graphics.Color.RED);
        } else {
            holder.tvScore.setText(score + "/" + totalQuestions);
            // Logic màu sắc
            if (score >= totalQuestions / 2.0) {
                holder.tvScore.setTextColor(android.graphics.Color.parseColor("#1976D2")); // Xanh
            } else {
                holder.tvScore.setTextColor(android.graphics.Color.RED);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScore;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChapterName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}