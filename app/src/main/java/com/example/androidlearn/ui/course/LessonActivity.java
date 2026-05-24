package com.example.androidlearn.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.main.MainActivity;
import com.example.androidlearn.ui.quiz.QuizActivity;
// import com.example.androidlearn.ui.course.VideoPlayerActivity; // Chung package không cần import
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

        // Nhận dữ liệu chương đang mở
        syncChapterFromIntentOrCurrent();
        updateChapterTitle();

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
            intent.putExtra("CHAPTER_ID", currentChapterId);
            intent.putExtra("CHAPTER_NAME", currentChapterName);
            startActivity(intent);
        });

        // Assignment
        cardAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(LessonActivity.this, com.example.androidlearn.ui.assignment.AssignmentListActivity.class);
            intent.putExtra("CHAPTER_ID", currentChapterId);
            intent.putExtra("CHAPTER_NAME", currentChapterName);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        syncChapterFromIntentOrCurrent();
        updateChapterTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncChapterFromIntentOrCurrent();
        updateChapterTitle();
    }

    private int getUserId() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("KEY_USER_ID", 1);
    }

    private void syncChapterFromIntentOrCurrent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CHAPTER_ID")) {
            currentChapterId = intent.getIntExtra("CHAPTER_ID", currentChapterId);
        } else {
            Chapter current = dbHelper.getCurrentLearningChapter(getUserId());
            if (current != null) {
                currentChapterId = current.getMaChuong();
            }
        }

        if (intent != null && intent.hasExtra("CHAPTER_NAME")) {
            String name = intent.getStringExtra("CHAPTER_NAME");
            if (name != null && !name.trim().isEmpty()) {
                currentChapterName = name;
            }
        } else {
            Chapter chapter = dbHelper.getChapterById(currentChapterId);
            if (chapter != null) {
                currentChapterName = chapter.getTenChuong();
            }
        }
    }

    private void updateChapterTitle() {
        if (tvCurrentChapter != null) {
            tvCurrentChapter.setText(currentChapterName);
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