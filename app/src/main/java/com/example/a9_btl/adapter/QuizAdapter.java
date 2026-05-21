package com.example.a9_btl.adapter;

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

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.model.Question;
import com.example.a9_btl.ui.quiz.QuestionActivity;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Chapter> chapterList;
    private Context context;
    private DatabaseHelper dbHelper;

    public QuizAdapter(Context context, List<Chapter> chapterList) {
        this.context = context;
        this.chapterList = chapterList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);

        // 1. Hiển thị tên chương
        holder.tvChapterName.setText(chapter.getTenChuong());

        // 2. Logic Tô màu & Điểm số (Copy từ logic cũ vào đây)
        // Lấy User ID từ bộ nhớ (Giả sử bạn đã biết cách lấy, tạm để 1)
        int myId = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("KEY_USER_ID", 1);

        // Lấy số câu đúng
        int correctAnswers = dbHelper.getQuizScore(myId, chapter.getMaChuong());

        // Lấy tổng số câu (để tính %)
        int totalQuestions = dbHelper.getQuestionsByChapter(chapter.getMaChuong()).size();

        if (totalQuestions == 0) {
            // Chương chưa có câu hỏi
            holder.cardRoot.setCardBackgroundColor(Color.WHITE);
            holder.tvScore.setText("(Chưa có câu hỏi)");
            holder.itemView.setEnabled(false); // Không cho bấm
        }
        else if (correctAnswers == -1) {
            // Chưa làm bài
            holder.cardRoot.setCardBackgroundColor(Color.WHITE);
            holder.tvScore.setText("Chưa làm");
            holder.itemView.setEnabled(true);
        }
        else {
            // Đã làm -> Hiện điểm và Tô màu
            holder.tvScore.setText(correctAnswers + "/" + totalQuestions);

            double score10 = ((double) correctAnswers / totalQuestions) * 10;
            if (score10 >= 7.0) {
                holder.cardRoot.setCardBackgroundColor(Color.parseColor("#A5D6A7")); // Xanh
            } else {
                holder.cardRoot.setCardBackgroundColor(Color.parseColor("#EF9A9A")); // Đỏ
            }
            holder.itemView.setEnabled(true);
        }

        // --- THÊM LOGIC CHẶN CỬA ---
        int myUserId = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("KEY_USER_ID", 1);
        boolean isUnlocked = dbHelper.isChapterUnlocked(myUserId, chapter.getMaChuong());

        if (!isUnlocked) {
            // NẾU KHOÁ
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Màu xám
            holder.tvScore.setText("Đang khoá");
            holder.itemView.setAlpha(0.6f);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Hãy học xong lý thuyết và làm bài tập chương trước!", Toast.LENGTH_SHORT).show();
            });
        } else {
            // NẾU MỞ -> Chạy logic tô màu Xanh/Đỏ cũ của bạn
            // ... (Copy đoạn code tô màu cũ vào đây) ...

            // Và cho phép bấm
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putExtra("CHAPTER_ID", chapter.getMaChuong());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        CardView cardRoot;
        TextView tvChapterName, tvScore;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            tvChapterName = itemView.findViewById(R.id.tvChapterName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}