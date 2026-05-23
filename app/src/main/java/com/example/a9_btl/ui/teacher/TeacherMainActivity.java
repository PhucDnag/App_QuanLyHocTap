package com.example.androidlearn.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.ui.auth.LoginActivity;
import com.example.androidlearn.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class TeacherMainActivity extends AppCompatActivity {

    private TextView tvChapterCount, tvClassCount;
    private CardView cardManageClass, cardManageCourse, cardAnalytics;
    private DatabaseHelper dbHelper;
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        dbHelper = new DatabaseHelper(this);

        // 1. Lấy ID Giáo viên từ Session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        teacherId = prefs.getInt("KEY_USER_ID", -1);

        // 2. Ánh xạ View
        tvChapterCount = findViewById(R.id.tvChapterCount);
        tvClassCount = findViewById(R.id.tvClassCount);
        cardManageClass = findViewById(R.id.cardManageClass);
        cardManageCourse = findViewById(R.id.cardManageCourse);
        cardAnalytics = findViewById(R.id.cardAnalytics);


        // 3. Load Dữ Liệu Động (Dashboard)
        loadDashboardData();

        // 4. Xử lý sự kiện Click Menu Grid
        cardManageClass.setOnClickListener(v -> {
            // Chuyển sang màn hình Danh sách lớp (Sẽ làm ở bước sau)
            Intent intent = new Intent(TeacherMainActivity.this, TeacherClassListActivity.class);
            startActivity(intent);
        });

        cardManageCourse.setOnClickListener(v -> {
            // Chuyển sang màn hình Quản lý Chương học (Sẽ làm ở bước sau)
            Intent intent = new Intent(TeacherMainActivity.this, TeacherCourseListActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherMainActivity.this, TeacherAnalyticsActivity.class);
            startActivity(intent);
        });

        // 5. Cài đặt Bottom Navigation
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi quay về trang chủ (để cập nhật số nếu có thay đổi)
        loadDashboardData();

        // Cập nhật Badge tin nhắn
        DatabaseHelper db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int teacherId = prefs.getInt("KEY_USER_ID", -1);

        int unreadCount = db.getTotalUnreadCountForTeacher(teacherId);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);

        if (bottomNav != null) {
            com.google.android.material.badge.BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chat);
            if (unreadCount > 0) {
                badge.setVisible(true);
                badge.setNumber(unreadCount);
            } else {
                badge.setVisible(false);
            }
        }
    }

    private void loadDashboardData() {
        // Lấy số liệu từ Database
        int totalChapters = dbHelper.getChapterCount();
        int totalClasses = dbHelper.getClassCount(teacherId);

        // Hiển thị lên giao diện
        tvChapterCount.setText(totalChapters + " chương");
        tvClassCount.setText(totalClasses + " lớp");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);
        bottomNav.setSelectedItemId(R.id.nav_home); // Mặc định chọn Home

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Đang ở Home rồi thì không làm gì
                    return true;
                }
                else if (itemId == R.id.nav_chat) {
                    // Chuyển sang màn hình Chat dành cho GV
                    Intent intent = new Intent(TeacherMainActivity.this, TeacherChatActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(TeacherMainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }


}