package com.example.a9_btl.ui.assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AssignmentActivity extends AppCompatActivity {

    private TextView tvChapterTitle, tvQuestion, tvFileName;
    private EditText edtAnswer;
    private View btnBack, btnAttach;
    private com.google.android.material.button.MaterialButton btnSubmit;

    private DatabaseHelper dbHelper;
    private int currentChapterId = 1;
    private String currentChapterName = "Chương học";

    // Biến lưu đường dẫn file user đã chọn
    private String selectedFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        dbHelper = new DatabaseHelper(this);

        // 1. Nhận dữ liệu
        if (getIntent().hasExtra("CHAPTER_ID")) {
            currentChapterId = getIntent().getIntExtra("CHAPTER_ID", 1);
        }
        if (getIntent().hasExtra("CHAPTER_NAME")) {
            currentChapterName = getIntent().getStringExtra("CHAPTER_NAME");
        }

        // 2. Ánh xạ
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFileName = findViewById(R.id.tvFileName);
        edtAnswer = findViewById(R.id.edtAnswer);
        btnBack = findViewById(R.id.btnBack);
        btnAttach = findViewById(R.id.btnAttach);
        btnSubmit = findViewById(R.id.btnSubmit);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // 3. Hiển thị đề bài
        tvChapterTitle.setText(currentChapterName);
        loadQuestion();

        // 4. Xử lý sự kiện
        btnBack.setOnClickListener(v -> finish());

        // Chọn file
        btnAttach.setOnClickListener(v -> openFilePicker());
        tvFileName.setOnClickListener(v -> openFilePicker()); // Bấm vào chữ cũng mở luôn

        // Nộp bài
        btnSubmit.setOnClickListener(v -> submitAssignment());

        setupBottomNavigation();

        // --- THÊM ĐOẠN NÀY ĐỂ XEM LẠI BÀI CŨ ---
        checkExistingSubmission();
    }

    private void loadQuestion() {
        String question = dbHelper.getAssignmentQuestion(currentChapterId);
        tvQuestion.setText(question);
    }

    // --- LOGIC CHỌN FILE ---
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        // Chỉ định rõ PDF và Word nếu muốn:
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        filePickerLauncher.launch(intent);
    }

    // Hứng kết quả chọn file
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedFilePath = uri.toString();
                        tvFileName.setText("Đã chọn: " + uri.getLastPathSegment());
                        tvFileName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                }
            }
    );

    // --- LOGIC NỘP BÀI ---
    private void submitAssignment() {
        String answerText = edtAnswer.getText().toString().trim();

        // Kiểm tra xem đã nhập gì chưa
        if (answerText.isEmpty() && selectedFilePath.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập câu trả lời hoặc đính kèm file!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào DB (User ID tạm để là 1)
        dbHelper.saveSubmission(1, currentChapterId, answerText, selectedFilePath);

        Toast.makeText(this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
        finish(); // Đóng màn hình
    }

    // Hàm kiểm tra bài cũ
    private void checkExistingSubmission() {
        // User ID = 1
        String[] oldData = dbHelper.getSubmissionDetail(1, currentChapterId);

        if (oldData != null) {
            // Đổ dữ liệu cũ vào ô nhập
            String oldText = oldData[0];
            String oldFile = oldData[1];

            edtAnswer.setText(oldText);

            if (oldFile != null && !oldFile.isEmpty()) {
                selectedFilePath = oldFile;
                tvFileName.setText("File đã nộp: " + Uri.parse(oldFile).getLastPathSegment());
                tvFileName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            // Đổi tên nút Submit thành "Cập nhật"
            btnSubmit.setText("Cập nhật bài làm");
        }
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
                    Intent intent = new Intent(AssignmentActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    // Chuyển sang Tab Chat
                    Intent intent = new Intent(AssignmentActivity.this, MainActivity.class);
                    intent.putExtra("NAVIGATE_TO", "CHAT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Chuyển sang Tab Profile
                    Intent intent = new Intent(AssignmentActivity.this, MainActivity.class);
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