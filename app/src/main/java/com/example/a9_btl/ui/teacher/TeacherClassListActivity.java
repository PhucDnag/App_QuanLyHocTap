package com.example.a9_btl.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.adapter.TeacherClassAdapter;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class TeacherClassListActivity extends AppCompatActivity {

    private RecyclerView rcvClassList;
    private DatabaseHelper dbHelper;
    private TeacherClassAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_class_list);

        dbHelper = new DatabaseHelper(this);
        rcvClassList = findViewById(R.id.rcvClassList);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupBottomNavigation();

        // Đặt mặc định không chọn cái nào (vì đây là màn hình con)
        // Hoặc nếu bạn coi đây là màn Home thì để nav_home
//        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(new com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // --- SỬA Ở ĐÂY: CHUYỂN VỀ TRANG CHỦ GIÁO VIÊN ---
                    Intent intent = new Intent(TeacherClassListActivity.this, TeacherMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Đóng màn hình hiện tại
                    return true;
                }
                else if (itemId == R.id.nav_chat) {
                    // Chuyển sang Chat Giáo viên
//                    Intent intent = new Intent(TeacherClassListActivity.this, TeacherChatActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    // Tạm thời chuyển về Main (Profile) hoặc làm trang Profile riêng cho GV
                    Intent intent = new Intent(TeacherClassListActivity.this, com.example.a9_btl.ui.main.MainActivity.class);
                    intent.putExtra("NAVIGATE_TO", "PROFILE");
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // 1. Lấy ID Giáo viên
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int teacherId = prefs.getInt("KEY_USER_ID", -1);

        // 2. Lấy danh sách lớp mà giáo viên dạy
        List<String> myClasses = dbHelper.getTeachingClasses(teacherId);

        if (myClasses.isEmpty()) {
            Toast.makeText(this, "Bạn chưa quản lý lớp nào!", Toast.LENGTH_SHORT).show();
        }

        // 3. Cài đặt RecyclerView
        rcvClassList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeacherClassAdapter(this, myClasses, dbHelper, new TeacherClassAdapter.IOnClassClickListener() {
            @Override
            public void onClick(String className) {
                // ĐÃ XÓA DẤU // Ở ĐẦU DÒNG
                Intent intent = new Intent(TeacherClassListActivity.this, TeacherClassDetailActivity.class);
                intent.putExtra("CLASS_NAME", className);
                startActivity(intent);
            }
        });

        rcvClassList.setAdapter(adapter);
    }
    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);
        // Coi như đây là màn hình con, không set Selected Item để tránh hiểu nhầm
        // Hoặc có thể set là nav_home nếu muốn
        bottomNav.getMenu().setGroupCheckable(0, false, true);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Về trang chủ
                Intent intent = new Intent(this, TeacherMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_chat) {
                // Sang màn hình Chat (Nếu bạn đã làm)
//                startActivity(new Intent(this, TeacherChatActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                // --- SỬA LỖI 2: MỞ MÀN HÌNH HỒ SƠ ---
                // Chuyển sang MainActivity và yêu cầu mở tab Profile
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "PROFILE");
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ... code cũ ...

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
}