package com.example.a9_btl.ui.assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.adapter.AssignmentAdapter; // Import Adapter mới tạo
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class AssignmentListActivity extends AppCompatActivity {

    private RecyclerView rcvAssignment;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        dbHelper = new DatabaseHelper(this);

        // Lấy ID học sinh
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getInt("KEY_USER_ID", -1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rcvAssignment = findViewById(R.id.rcvAssignment);
        rcvAssignment.setLayoutManager(new GridLayoutManager(this, 2));

        loadData();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        updateHeaderProgress();
    }

    private void updateHeaderProgress() {
        TextView tvCurrentChapter = findViewById(R.id.tvCurrentChapter);
        if (tvCurrentChapter != null) {
            Chapter current = dbHelper.getCurrentChapter(userId);
            if (current != null) {
                tvCurrentChapter.setText(current.getTenChuong());
            }
        }
    }

    private void loadData() {
        List<Chapter> chapters = dbHelper.getAllChapters();

        // Gọi Adapter từ file riêng
        AssignmentAdapter adapter = new AssignmentAdapter(this, chapters, dbHelper, userId);
        rcvAssignment.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView == null) return;

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    navigateToMain("");
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    navigateToMain("CHAT");
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateToMain("PROFILE");
                    return true;
                }
                return false;
            }
        });
    }

    private void navigateToMain(String tab) {
        Intent intent = new Intent(AssignmentListActivity.this, MainActivity.class);
        if (!tab.isEmpty()) intent.putExtra("NAVIGATE_TO", tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}