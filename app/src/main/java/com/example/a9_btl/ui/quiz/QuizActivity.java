package com.example.a9_btl.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.adapter.QuizAdapter;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.assignment.AssignmentActivity;
import com.example.a9_btl.ui.chat.ChatFragment;
import com.example.a9_btl.ui.main.HomeFragment;
import com.example.a9_btl.ui.main.MainActivity;
import com.example.a9_btl.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private RecyclerView rcvQuiz;
    private DatabaseHelper dbHelper;
    private QuizAdapter adapter;
    private List<Chapter> listChapters;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        dbHelper = new DatabaseHelper(this);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cài đặt RecyclerView
        rcvQuiz = findViewById(R.id.rcvQuiz);

        // Chia làm 2 cột
        rcvQuiz.setLayoutManager(new GridLayoutManager(this, 2));
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Load lại dữ liệu mỗi khi màn hình hiện lên (để cập nhật màu sắc)
        updateHeaderProgress();
    }

    private void loadData() {
        // 1. Lấy toàn bộ chương từ Database
        listChapters = dbHelper.getAllChapters();

        // 2. Đổ vào Adapter
        adapter = new QuizAdapter(this, listChapters);
        rcvQuiz.setAdapter(adapter);
    }

    private void updateHeaderProgress() {
        // 1. Lấy ID user
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int myId = prefs.getInt("KEY_USER_ID", 1);

        // 2. Tìm chương hiện tại
        DatabaseHelper db = new DatabaseHelper(this);
        Chapter currentChapter = db.getCurrentChapter(myId);

        // 3. Ánh xạ và gán chữ lên thẻ Tiến độ
        TextView tvCurrentChapter = findViewById(R.id.tvCurrentChapter); // ID trong XML
        if (currentChapter != null && tvCurrentChapter != null) {
            tvCurrentChapter.setText(currentChapter.getTenChuong());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    // Chuyển sang Tab Chat
                    Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                    intent.putExtra("NAVIGATE_TO", "CHAT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Chuyển sang Tab Profile
                    Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                    intent.putExtra("NAVIGATE_TO", "PROFILE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

}