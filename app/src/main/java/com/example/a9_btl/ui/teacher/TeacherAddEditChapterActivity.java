package com.example.a9_btl.ui.teacher;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TeacherAddEditChapterActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int chapterId = -1;

    private EditText edtName, edtPdf, edtVideo, edtAssignment;
    private TextView tvTitle;

    // --- 1. KHAI BÁO BIẾN NÚT QUIZ ---
    private Button btnManageQuiz;

    // Mã request để phân biệt đang chọn PDF hay Video
    private static final int PICK_PDF_REQUEST = 101;
    private static final int PICK_VIDEO_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_add_edit_chapter);

        db = new DatabaseHelper(this);

        // Ánh xạ View
        edtName = findViewById(R.id.edtChapterName);
        edtPdf = findViewById(R.id.edtPdf);
        edtVideo = findViewById(R.id.edtVideo);
        edtAssignment = findViewById(R.id.edtAssignment);
        tvTitle = findViewById(R.id.tvTitle);

        edtPdf.setInputType(InputType.TYPE_NULL);
        edtPdf.setFocusable(false);
        edtPdf.setOnClickListener(v -> openFileChooser("application/pdf", PICK_PDF_REQUEST));
        edtVideo.setInputType(InputType.TYPE_NULL);
        edtVideo.setFocusable(false);
        edtVideo.setOnClickListener(v -> openFileChooser("video/*", PICK_VIDEO_REQUEST));

        Button btnSubmit = findViewById(R.id.btnSubmit);
        ImageButton btnPickPdf = findViewById(R.id.btnPickPdf);
        ImageButton btnPickVideo = findViewById(R.id.btnPickVideo);

        // --- 2. ÁNH XẠ NÚT QUIZ ---
        btnManageQuiz = findViewById(R.id.btnManageQuiz);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Kiểm tra Thêm/Sửa
        if (getIntent().hasExtra("CHAPTER_ID")) {
            chapterId = getIntent().getIntExtra("CHAPTER_ID", -1);
            tvTitle.setText("Chỉnh sửa khoá học");
            loadData();
        }

        // Sự kiện chọn file
        btnPickPdf.setOnClickListener(v -> openFileChooser("application/pdf", PICK_PDF_REQUEST));
        btnPickVideo.setOnClickListener(v -> openFileChooser("video/*", PICK_VIDEO_REQUEST));

        // Sự kiện lưu chương
        btnSubmit.setOnClickListener(v -> saveChapter());

        // --- 3. SỰ KIỆN BẤM NÚT SOẠN CÂU HỎI (MỚI THÊM) ---
        btnManageQuiz.setOnClickListener(v -> {
            if (chapterId == -1) {
                // Nếu đang thêm mới (chưa có ID) -> Bắt buộc lưu trước
                Toast.makeText(this, "Vui lòng bấm Submit để lưu chương trước khi soạn câu hỏi!", Toast.LENGTH_LONG).show();
            } else {
                // Nếu đã có ID -> Mở màn hình quản lý câu hỏi
                Intent intent = new Intent(TeacherAddEditChapterActivity.this, TeacherQuizManagerActivity.class);
                intent.putExtra("CHAPTER_ID", chapterId);
                startActivity(intent);
            }
        });

        setupBottomNavigation();
    }

    // 1. Mở trình chọn file
    private void openFileChooser(String mimeType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn file"), requestCode);
    }

    // 2. Nhận kết quả chọn file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String savedFileName = copyFileToInternalStorage(uri);

            if (savedFileName != null) {
                if (requestCode == PICK_PDF_REQUEST) {
                    edtPdf.setText(savedFileName);
                } else if (requestCode == PICK_VIDEO_REQUEST) {
                    edtVideo.setText(savedFileName);
                }
            } else {
                Toast.makeText(this, "Lỗi khi tải file!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 3. Copy file vào bộ nhớ trong
    private String copyFileToInternalStorage(Uri uri) {
        try {
            String fileName = getFileName(uri);
            if (fileName == null) fileName = "file_" + System.currentTimeMillis();

            File desFile = new File(getFilesDir(), fileName);
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(desFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void loadData() {
        Chapter c = db.getChapterById(chapterId);
        if (c != null) {
            edtName.setText(c.getTenChuong());
            edtPdf.setText(db.getPdfFileName(chapterId));
            edtVideo.setText(db.getVideoFileName(chapterId));
            edtAssignment.setText(db.getAssignmentQuestion(chapterId));
        }
    }

    private void saveChapter() {
        String name = edtName.getText().toString().trim();
        String pdf = edtPdf.getText().toString().trim();
        String video = edtVideo.getText().toString().trim();
        String assignment = edtAssignment.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên chương không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (chapterId == -1) {
            db.addChapterFull(name, pdf, video, assignment);
            Toast.makeText(this, "Đã thêm chương mới!", Toast.LENGTH_SHORT).show();
            // Không finish() ngay nếu muốn cho người dùng soạn câu hỏi luôn
            // Nhưng để đơn giản, cứ finish về danh sách rồi bảo họ bấm vào lại
        } else {
            db.updateChapterFull(chapterId, name, pdf, video, assignment);
            Toast.makeText(this, "Đã cập nhật chương!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, TeacherMainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "PROFILE");
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}