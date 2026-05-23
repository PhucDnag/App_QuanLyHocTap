package com.example.a9_btl.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.main.MainActivity;
import com.example.a9_btl.ui.quiz.QuizActivity;
// import com.example.a9_btl.ui.course.VideoPlayerActivity; // Chung package không cần import
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.saveTo;

import java.io.File;

public class LessonActivity extends AppCompatActivity {

    private TextView tvCurrentChapter;
    private MaterialButton btnBack;
    private CardView cardPdf, cardVideo, cardQuiz, cardAssignment;

    private DatabaseHelper dbHelper;
    private int currentChapterId = 1;
    private String currentChapterName = "Chương học";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ
        tvCurrentChapter = findViewById(R.id.tvCurrentChapter);
        btnBack = findViewById(R.id.btnBack);
        cardPdf = findViewById(R.id.cardPdf);
        cardVideo = findViewById(R.id.cardVideo);
        cardQuiz = findViewById(R.id.cardQuiz);
        cardAssignment = findViewById(R.id.cardAssignment);

        // Nhận dữ liệu
        if (getIntent().hasExtra("CHAPTER_NAME")) {
            currentChapterName = getIntent().getStringExtra("CHAPTER_NAME");
        }
        if (getIntent().hasExtra("CHAPTER_ID")) {
            currentChapterId = getIntent().getIntExtra("CHAPTER_ID", 1);
        }

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Setup Menu
        setupBottomNavigation();

        // --- XỬ LÝ CLICK PDF (ĐÃ NÂNG CẤP) ---
        cardPdf.setOnClickListener(v -> {
            String pdfFileName = dbHelper.getPdfFileName(currentChapterId);

            if (pdfFileName == null || pdfFileName.isEmpty()) {
                Toast.makeText(this, "Chưa có file PDF cho chương này", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ghi nhận trạng thái đã xem PDF
            dbHelper.saveDocProgress(getUserId(), currentChapterId, "PDF", 1);

            // 1. Kiểm tra file trong bộ nhớ máy (File mới upload)
            File fileInStorage = new File(getFilesDir(), pdfFileName);

            if (fileInStorage.exists() && fileInStorage.length() > 0) {
                // File do giảng viên chọn nằm trong internal storage của app.
                // Truyền absolute path để thư viện đọc đúng file local đã copy.
                startActivity(
                        PdfViewerActivity.Companion.launchPdfFromPath(
                                LessonActivity.this,
                                fileInStorage.getAbsolutePath(),
                                "Tài liệu " + currentChapterName,
                                saveTo.DOWNLOADS,
                                false
                        )
                );
                return;
            }

            if (isPdfInAssets(pdfFileName)) {
                // File PDF mẫu trong assets: thư viện hỗ trợ đọc theo tên asset.
                startActivity(
                        PdfViewerActivity.Companion.launchPdfFromPath(
                                LessonActivity.this,
                                pdfFileName,
                                "Tài liệu " + currentChapterName,
                                saveTo.ASK_EVERYTIME,
                                true
                        )
                );
                return;
            }

            Toast.makeText(this, "Không tìm thấy file PDF: " + pdfFileName + ". Vui lòng yêu cầu giảng viên chọn lại file PDF.", Toast.LENGTH_LONG).show();
        });

        // Video
        cardVideo.setOnClickListener(v -> {
            // 1. Kiểm tra trước xem có tên file video không
            String videoName = dbHelper.getVideoFileName(currentChapterId);

            if (videoName == null || videoName.isEmpty()) {
                Toast.makeText(LessonActivity.this, "Chương này chưa có Video bài giảng!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại, không mở màn hình kia
            }

            // Ghi nhận trạng thái đã xem Video
            dbHelper.saveDocProgress(getUserId(), currentChapterId, "Video", 1);

            // 2. Nếu có thì mới mở
            Intent intent = new Intent(LessonActivity.this, VideoPlayerActivity.class);
            intent.putExtra("CHAPTER_NAME", currentChapterName);
            intent.putExtra("CHAPTER_ID", currentChapterId);
            startActivity(intent);
        });

        // Quiz
        cardQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(LessonActivity.this, QuizActivity.class);
            intent.putExtra("CHAPTER_NAME", currentChapterName);
            startActivity(intent);
        });

        // Assignment
        cardAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(LessonActivity.this, com.example.a9_btl.ui.assignment.AssignmentListActivity.class);
            startActivity(intent);
        });
    }

    private boolean isPdfInAssets(String pdfFileName) {
        try {
            String[] assetFiles = getAssets().list("");
            if (assetFiles == null) return false;
            for (String assetFile : assetFiles) {
                if (assetFile.equals(pdfFileName)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderProgress();
    }

    private int getUserId() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("KEY_USER_ID", 1);
    }

    private void updateHeaderProgress() {
        if (tvCurrentChapter == null) return;
        int myId = getUserId();
        Chapter current = dbHelper.getCurrentChapter(myId);
        if (current != null) {
            tvCurrentChapter.setText(current.getTenChuong());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(LessonActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                Intent intent = new Intent(LessonActivity.this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "CHAT");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(LessonActivity.this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "PROFILE");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}