package com.example.androidlearn.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.main.MainActivity; // Import màn hình chính để mở Profile
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class TeacherStudentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_student_detail);

        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            TextView tvName = findViewById(R.id.tvStudentNameTitle);
            TextView tvProgress = findViewById(R.id.tvCurrentProgress);
            RecyclerView rcv = findViewById(R.id.rcvScoreList);

            // Nút Back
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());

            // --- SỬA LỖI 2: CẬP NHẬT MENU DƯỚI ---
            setupBottomNavigation();

            // Nhận dữ liệu
            int studentId = getIntent().getIntExtra("STUDENT_ID", -1);
            String studentName = getIntent().getStringExtra("STUDENT_NAME");
            if (tvName != null) tvName.setText(studentName);

            // Hiển thị tiến độ
            Chapter current = dbHelper.getCurrentChapter(studentId);
            if (current != null) {
                tvProgress.setText("Đang học: " + current.getTenChuong());
            } else {
                tvProgress.setText("Chưa bắt đầu học");
            }

            // Hiển thị danh sách
            List<Chapter> allChapters = dbHelper.getAllChapters();
            StudentScoreAdapter adapter = new StudentScoreAdapter(allChapters, dbHelper, studentId);
            rcv.setLayoutManager(new LinearLayoutManager(this));
            rcv.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // --- ADAPTER ---
    public class StudentScoreAdapter extends RecyclerView.Adapter<StudentScoreAdapter.ViewHolder> {
        private List<Chapter> chapters;
        private DatabaseHelper db;
        private int studentId;

        public StudentScoreAdapter(List<Chapter> chapters, DatabaseHelper db, int studentId) {
            this.chapters = chapters;
            this.db = db;
            this.studentId = studentId;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_chapter_score, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Chapter c = chapters.get(position);
            holder.tvName.setText(c.getTenChuong());

            // --- SỬA LỖI 1: LẤY SỐ CÂU HỎI THẬT TỪ DATABASE ---
            // Thay vì int total = 20; (cứng), ta dùng hàm đếm:
            int totalQuestions = db.getQuestionsByChapter(c.getMaChuong()).size();

            // Lấy điểm
            int score = db.getQuizScore(studentId, c.getMaChuong());

            // Logic hiển thị
            if (totalQuestions == 0) {
                holder.tvScore.setText("Không có bài");
                holder.tvScore.setTextColor(android.graphics.Color.GRAY);
            } else if (score == -1) {
                holder.tvScore.setText("Chưa thi");
                holder.tvScore.setTextColor(android.graphics.Color.RED);
            } else {
                holder.tvScore.setText(score + "/" + totalQuestions); // Sẽ hiện 2/2 thay vì 2/20
                // Tô màu: Xanh nếu >= 50% điểm, Đỏ nếu thấp hơn
                if (score >= totalQuestions / 2.0) {
                    holder.tvScore.setTextColor(android.graphics.Color.parseColor("#1976D2"));
                } else {
                    holder.tvScore.setTextColor(android.graphics.Color.RED);
                }
            }

            // Click để chấm bài tập tự luận
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherStudentDetailActivity.this, TeacherGradingActivity.class);
                intent.putExtra("STUDENT_ID", studentId);
                intent.putExtra("CHAPTER_ID", c.getMaChuong());
                intent.putExtra("CHAPTER_NAME", c.getTenChuong());
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return chapters.size(); }
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvScore;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvChapterName);
                tvScore = v.findViewById(R.id.tvScore);
            }
        }
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