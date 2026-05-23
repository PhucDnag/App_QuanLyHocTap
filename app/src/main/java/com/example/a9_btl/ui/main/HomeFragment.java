package com.example.a9_btl.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.ui.aibot.AiBotActivity;
import com.example.a9_btl.ui.assignment.AssignmentActivity;
import com.example.a9_btl.ui.assignment.AssignmentListActivity;
import com.example.a9_btl.ui.course.CourseActivity;
import com.example.a9_btl.ui.course.LessonActivity; // Ví dụ màn hình khóa học
import com.example.a9_btl.ui.quiz.QuizActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
// import com.example.a9_btl.ui.quiz.QuizSelectorActivity; // Import màn hình Quiz của bạn

public class HomeFragment extends Fragment {

    private CardView cardCourse, cardQuiz, cardAssignment, cardAbility;
    private ExtendedFloatingActionButton fabAiBot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Ánh xạ layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các nút (CardView) từ XML
        cardCourse    = view.findViewById(R.id.cardCourse);
        cardQuiz      = view.findViewById(R.id.cardQuiz);
        cardAssignment = view.findViewById(R.id.cardAssignment);
        cardAbility   = view.findViewById(R.id.cardAbility);
        fabAiBot      = view.findViewById(R.id.fabAiBot);

        // FAB — Mở AI Trợ Giảng
        if (fabAiBot != null) {
            fabAiBot.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AiBotActivity.class);
                startActivity(intent);
            });
        }

        // 2. Bắt sự kiện Click cho nút "BÀI HỌC"
        cardCourse.setOnClickListener(v -> {
            // Chuyển sang màn hình LessonActivity (Màn hình chi tiết khóa học)
            Intent intent = new Intent(getActivity(), CourseActivity.class);
            startActivity(intent);
        });

        // 3. Bắt sự kiện Click cho nút "KIỂM TRA"
        cardQuiz.setOnClickListener(v -> {
            // Chuyển sang màn Quiz
//            Toast.makeText(getActivity(), "Chuyển đến màn hình kiểm tra", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(getActivity(), QuizActivity.class);
             startActivity(intent);
        });

        // 4. Bắt sự kiện Click cho nút "BÀI TẬP"
        cardAssignment.setOnClickListener(v -> {
            // Chuyển sang màn Quiz
//            Toast.makeText(getActivity(), "Chuyển đến màn hình kiểm tra", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), AssignmentListActivity.class);
            startActivity(intent);
        });

        cardAbility.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StudentAbilityActivity.class);
            startActivity(intent);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        updateHomeProgress(); // Gọi hàm cập nhật mỗi khi màn hình hiện lên
    }

    private void updateHomeProgress() {
        // 1. Lấy View từ giao diện
        if (getView() == null) return;
        android.widget.TextView tvCurrentChapter = getView().findViewById(R.id.tvCurrentChapter);
        android.widget.ProgressBar progressBar = getView().findViewById(R.id.progressBar);

        // 2. Lấy ID User đang đăng nhập
        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
        int myId = prefs.getInt("KEY_USER_ID", 1);

        // 3. Khởi tạo Database
        DatabaseHelper db = new DatabaseHelper(getContext());

        // 4. Cập nhật Tên Chương Hiện Tại
        if (tvCurrentChapter != null) {
            com.example.a9_btl.model.Chapter current = db.getCurrentChapter(myId);
            if (current != null) {
                tvCurrentChapter.setText(current.getTenChuong());
            } else {
                tvCurrentChapter.setText("Chưa bắt đầu học");
            }
        }

        // 5. Cập nhật Thanh Tiến Độ (ProgressBar)
        if (progressBar != null) {
            java.util.List<DatabaseHelper.ChapterProgress> list = db.getStudyProgress(myId);
            int totalPercent = 0;
            if (list != null && !list.isEmpty()) {
                for (DatabaseHelper.ChapterProgress p : list) {
                    totalPercent += p.getProgressPercent();
                }
                int overallProgress = totalPercent / list.size();
                progressBar.setProgress(overallProgress);
            } else {
                progressBar.setProgress(0);
            }
        }
    }
}