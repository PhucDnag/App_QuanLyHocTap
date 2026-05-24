package com.example.androidlearn.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

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
        TextView tvAttachmentLabel = findViewById(R.id.tvAttachmentLabel);
        TextView tvAttachment = findViewById(R.id.tvAttachment);
        View cardAttachment = findViewById(R.id.cardAttachment);
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
            String answerText = submission[0];
            String fileUri = submission[1];

            if (answerText == null || answerText.trim().isEmpty()) {
                tvAnswer.setText("Sinh viên không nhập nội dung văn bản.");
            } else {
                tvAnswer.setText(answerText); // Nội dung bài làm
            }

            if (fileUri != null && !fileUri.trim().isEmpty()) {
                tvAttachmentLabel.setVisibility(View.VISIBLE);
                cardAttachment.setVisibility(View.VISIBLE);
                tvAttachment.setText("Mở file: " + getDisplayFileName(fileUri));
                cardAttachment.setOnClickListener(v -> openSubmittedFile(fileUri));
            }

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

    private String getDisplayFileName(String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);
            String lastPath = uri.getLastPathSegment();
            if (lastPath == null || lastPath.trim().isEmpty()) {
                return "file bài nộp";
            }
            int slashIndex = lastPath.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex < lastPath.length() - 1) {
                return lastPath.substring(slashIndex + 1);
            }
            return lastPath;
        } catch (Exception e) {
            return "file bài nộp";
        }
    }

    private void openSubmittedFile(String fileUri) {
        try {
            Uri uri;
            String mimeType = "*/*";

            if (fileUri.startsWith("content://")) {
                uri = Uri.parse(fileUri);
            } else {
                File file = new File(fileUri);
                if (!file.exists()) {
                    Toast.makeText(this, "File không còn tồn tại trong bộ nhớ ứng dụng.", Toast.LENGTH_LONG).show();
                    return;
                }
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                String extension = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                if (extension != null && !extension.isEmpty()) {
                    String detectedType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    if (detectedType != null) {
                        mimeType = detectedType;
                    }
                }
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Mở file bài nộp"));
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được file. Hãy cài ứng dụng đọc PDF/Word hoặc yêu cầu sinh viên nộp lại file.", Toast.LENGTH_LONG).show();
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