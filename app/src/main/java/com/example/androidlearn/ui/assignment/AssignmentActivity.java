package com.example.androidlearn.ui.assignment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
                        try {
                            final int flags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {
                        }
                        String copiedPath = copyPickedFileToInternalStorage(uri);
                        if (copiedPath == null || copiedPath.isEmpty()) {
                            selectedFilePath = uri.toString();
                            tvFileName.setText("Đã chọn: " + getDisplayFileName(uri));
                            Toast.makeText(this, "Không copy được file, app sẽ thử lưu quyền đọc URI.", Toast.LENGTH_SHORT).show();
                        } else {
                            selectedFilePath = copiedPath;
                            tvFileName.setText("Đã chọn: " + new File(copiedPath).getName());
                        }
                        tvFileName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                }
            }
    );

    private String copyPickedFileToInternalStorage(Uri uri) {
        String displayName = getDisplayFileName(uri);
        File dir = new File(getFilesDir(), "submissions");
        if (!dir.exists() && !dir.mkdirs()) {
            return "";
        }

        String safeName = displayName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.trim().isEmpty()) {
            safeName = "submission_file";
        }
        File target = new File(dir, getUserId() + "_" + currentChapterId + "_" + System.currentTimeMillis() + "_" + safeName);

        try (InputStream input = getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(target)) {
            if (input == null) return "";
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return target.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    private String getDisplayFileName(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
        }
        if (name == null || name.trim().isEmpty()) {
            name = uri.getLastPathSegment();
        }
        if (name == null || name.trim().isEmpty()) {
            name = "submission_file";
        }
        return name;
    }

    private int getUserId() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("KEY_USER_ID", 1);
    }

    // --- LOGIC NỘP BÀI ---
    private void submitAssignment() {
        String answerText = edtAnswer.getText().toString().trim();

        // Kiểm tra xem đã nhập gì chưa
        if (answerText.isEmpty() && selectedFilePath.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập câu trả lời hoặc đính kèm file!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào DB theo đúng ID của user đang đăng nhập
        dbHelper.saveSubmission(getUserId(), currentChapterId, answerText, selectedFilePath);

        Toast.makeText(this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
        finish(); // Đóng màn hình
    }

    // Hàm kiểm tra bài cũ
    private void checkExistingSubmission() {
        // Đọc bài cũ theo đúng ID của user đang đăng nhập
        String[] oldData = dbHelper.getSubmissionDetail(getUserId(), currentChapterId);

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
        // 1. Lấy ID user hiện tại
        int myId = getUserId();

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