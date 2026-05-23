package com.example.androidlearn.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlearn.R;
import com.example.androidlearn.adapter.CourseAdapter;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.assignment.AssignmentActivity;
import com.example.androidlearn.ui.main.MainActivity; // Import MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class CourseActivity extends AppCompatActivity {

    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course); // Layout mới đã có bottomNavigation

        RecyclerView rcv = findViewById(R.id.rcvCourse);
        rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this)); // Xếp dọc

        DatabaseHelper db = new DatabaseHelper(this);
        java.util.List<Chapter> chapters = db.getAllChapters();

        CourseAdapter adapter = new CourseAdapter(this, chapters);
        rcv.setAdapter(adapter);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 1. Ánh xạ thanh Menu
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Đặt mặc định chọn nút Home (vì Khóa học thuộc về Home)
        bottomNav.setSelectedItemId(R.id.nav_home);

        setupBottomNavigation();
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

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderProgress(); // <--- GỌI HÀM NÀY
        // ... các code load dữ liệu khác
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(CourseActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    // Chuyển sang Tab Chat
                    Intent intent = new Intent(CourseActivity.this, MainActivity.class);
                    intent.putExtra("NAVIGATE_TO", "CHAT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Chuyển sang Tab Profile
                    Intent intent = new Intent(CourseActivity.this, MainActivity.class);
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