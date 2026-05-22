package com.example.a9_btl.ui.main;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment; // QUAN TRỌNG: Phải dùng thư viện này

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.ui.chat.ChatFragment;
import com.example.a9_btl.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(settingsPrefs.getBoolean("KEY_DARK_MODE", false)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 2. Mặc định load HomeFragment khi vừa mở ứng dụng
        boolean navigateToProfile = getIntent().hasExtra("NAVIGATE_TO")
                && "PROFILE".equals(getIntent().getStringExtra("NAVIGATE_TO"));
        boolean navigateToChat = getIntent().hasExtra("NAVIGATE_TO")
                && "CHAT".equals(getIntent().getStringExtra("NAVIGATE_TO"));

        if (savedInstanceState == null && !navigateToProfile && !navigateToChat) {
            loadFragment(new HomeFragment());
        }

        // Trong MainActivity.java

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // 1. Lấy thông tin User hiện tại để kiểm tra quyền
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                int userId = prefs.getInt("KEY_USER_ID", -1);
                DatabaseHelper db = new DatabaseHelper(this);
                com.example.a9_btl.model.User user = db.getUserById(userId);

                // 2. Kiểm tra quyền hạn (2 = Giáo viên)
                if (user != null && user.getQuyenHan() == 2) {
                    // ==> NẾU LÀ GIÁO VIÊN -> CHUYỂN VỀ TEACHER MAIN ACTIVITY
                    Intent intent = new Intent(MainActivity.this, com.example.a9_btl.ui.teacher.TeacherMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    // ==> NẾU LÀ SINH VIÊN -> GIỮ NGUYÊN LOGIC CŨ (Load HomeFragment)
                    // (Giả sử hàm loadFragment của bạn tên là loadFragment hoặc replaceFragment)
                    // loadFragment(new HomeFragment());

                    // HOẶC nếu bạn dùng ViewPager thì set current item:
                    // viewPager.setCurrentItem(0);

                    // Tạm thời mình để code logic cơ bản của SV ở đây:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new com.example.a9_btl.ui.main.HomeFragment()) // Đảm bảo ID và tên Fragment đúng
                            .commit();
                    return true;
                }
            }
            else if (itemId == R.id.nav_chat) {
                selectedFragment = new ChatFragment(); // MỞ DANH SÁCH CHAT
            }
            else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment) // Đảm bảo bạn có FrameLayout id là frame_container trong activity_main.xml
                        .commit();
                return true;
            }
            return false;
        });

        // XỬ LÝ CHUYỂN TAB TỪ MÀN HÌNH KHÁC TRỞ VỀ
        if (getIntent().hasExtra("NAVIGATE_TO")) {
            String tabName = getIntent().getStringExtra("NAVIGATE_TO");

            if ("CHAT".equals(tabName)) {
                // Tự động bấm vào nút Chat
                bottomNavigation.setSelectedItemId(R.id.nav_chat);
            } else if ("PROFILE".equals(tabName)) {
                // Mở trực tiếp hồ sơ để giảng viên không bị điều hướng ngược về màn teacher home
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
                loadFragment(new ProfileFragment());
            }
        }
    }

    /**
     * Hàm hỗ trợ thay thế Fragment trong FrameLayout
     * @param fragment Fragment cần hiển thị (phải thuộc androidx.fragment.app.Fragment)
     */
    private void loadFragment(Fragment fragment) {
        // Sử dụng getSupportFragmentManager() cho chuẩn AndroidX
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateChatBadge(); // <--- GỌI HÀM CẬP NHẬT BADGE
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateChatBadge();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateChatBadge();
    }

    private void updateChatBadge() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // 1. Lấy ID User
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int myId = prefs.getInt("KEY_USER_ID", 1);

        // 2. Hỏi Database
        DatabaseHelper db = new DatabaseHelper(this);
        int unreadCount = db.getUnreadCount(myId); // Lấy số lượng tin chưa đọc

        // 3. Xử lý chấm đỏ (Badge)
        // Lấy hoặc tạo Badge cho nút Chat (R.id.nav_chat)
        com.google.android.material.badge.BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chat);

        if (unreadCount > 0) {
            badge.setVisible(true); // Hiện chấm đỏ
            badge.setNumber(unreadCount); // Hiện số (Ví dụ: 3). Nếu không thích hiện số thì xóa dòng này.
            badge.setBackgroundColor(android.graphics.Color.RED); // Màu đỏ
            badge.setBadgeTextColor(android.graphics.Color.WHITE); // Chữ trắng
        } else {
            badge.setVisible(false); // Ẩn đi nếu không có tin mới
        }
    }
}