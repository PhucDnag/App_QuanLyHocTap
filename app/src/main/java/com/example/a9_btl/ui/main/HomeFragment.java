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
import com.example.a9_btl.ui.assignment.AssignmentActivity;
import com.example.a9_btl.ui.assignment.AssignmentListActivity;
import com.example.a9_btl.ui.course.CourseActivity;
import com.example.a9_btl.ui.course.LessonActivity; // Ví dụ màn hình khóa học
import com.example.a9_btl.ui.quiz.QuizActivity;
// import com.example.a9_btl.ui.quiz.QuizSelectorActivity; // Import màn hình Quiz của bạn

public class HomeFragment extends Fragment {

    private CardView cardCourse, cardQuiz, cardAssignment, cardChat;

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
        cardCourse = view.findViewById(R.id.cardCourse);
        cardQuiz = view.findViewById(R.id.cardQuiz);
        cardAssignment = view.findViewById(R.id.cardAssignment);
        cardChat = view.findViewById(R.id.cardChat);

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

        cardChat.setOnClickListener(v -> {
            // 1. Tìm cái thanh Menu dưới đáy (BottomNavigation) đang nằm ở MainActivity
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottomNavigation);

            // 2. Ra lệnh cho nó: "Hãy chuyển sang tab Chat ngay lập tức!"
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_chat);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        updateHomeProgress(); // Gọi hàm cập nhật mỗi khi màn hình hiện lên
    }

    private void updateHomeProgress() {
        // 1. Lấy View từ giao diện (Đảm bảo bạn đã đặt ID là tvCurrentChapter trong fragment_home.xml)
        if (getView() == null) return;
        android.widget.TextView tvCurrentChapter = getView().findViewById(R.id.tvCurrentChapter);

        if (tvCurrentChapter != null) {
            // 2. Lấy ID User đang đăng nhập
            android.content.SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
            int myId = prefs.getInt("KEY_USER_ID", 1);

            // 3. Hỏi Database xem đang học đến đâu
            DatabaseHelper db = new DatabaseHelper(getContext());
            com.example.a9_btl.model.Chapter current = db.getCurrentChapter(myId);

            // 4. Cập nhật chữ
            if (current != null) {
                tvCurrentChapter.setText(current.getTenChuong());
            }
        }
    }
}