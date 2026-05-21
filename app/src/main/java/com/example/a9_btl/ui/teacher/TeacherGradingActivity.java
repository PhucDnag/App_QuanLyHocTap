package com.example.a9_btl.ui.teacher;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherGradingActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int studentId, chapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_grading);

        dbHelper = new DatabaseHelper(this);
        studentId = getIntent().getIntExtra("STUDENT_ID", -1);
        chapterId = getIntent().getIntExtra("CHAPTER_ID", -1);
        String chapterName = getIntent().getStringExtra("CHAPTER_NAME");

        // Ánh xạ
        TextView tvTitle = findViewById(R.id.tvQuestionTitle);
        TextView tvQuestion = findViewById(R.id.tvQuestionContent);
        TextView tvAnswer = findViewById(R.id.tvStudentAnswer);
        EditText edtScore = findViewById(R.id.edtScore);
        EditText edtFeedback = findViewById(R.id.edtFeedback);
        Button btnSubmit = findViewById(R.id.btnSubmitGrade);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvTitle.setText(chapterName);

        // 1. Lấy câu hỏi
        String question = dbHelper.getAssignmentQuestion(chapterId);
        tvQuestion.setText(question);

        // 2. Lấy bài làm của SV
        String[] submission = dbHelper.getSubmissionFullDetail(studentId, chapterId);
        if (submission != null) {
            tvAnswer.setText(submission[0]); // Nội dung bài làm

            // Nếu đã chấm điểm rồi thì hiện lại
            if (submission[2] != null && !submission[2].equals("-1.0")) {
                edtScore.setText(submission[2]);
            }
            if (submission[3] != null) {
                edtFeedback.setText(submission[3]);
            }
        } else {
            tvAnswer.setText("Sinh viên chưa nộp bài này.");
            btnSubmit.setEnabled(false); // Không cho chấm nếu chưa nộp
        }

        // 3. Xử lý lưu điểm
        btnSubmit.setOnClickListener(v -> {
            String sScore = edtScore.getText().toString();
            String feedback = edtFeedback.getText().toString();

            if (sScore.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập điểm", Toast.LENGTH_SHORT).show();
                return;
            }

            double score = Double.parseDouble(sScore);
            dbHelper.gradeSubmission(studentId, chapterId, score, feedback);
            Toast.makeText(this, "Đã lưu điểm!", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại trang trước
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