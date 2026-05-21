package com.example.a9_btl.ui.course;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private DatabaseHelper dbHelper;
    private ImageView btnPlayOverlay;
    private int currentChapterId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);
        btnPlayOverlay = findViewById(R.id.btnPlayOverlay);
        dbHelper = new DatabaseHelper(this);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 1. Nhận ID chương
        if (getIntent().hasExtra("CHAPTER_ID")) {
            currentChapterId = getIntent().getIntExtra("CHAPTER_ID", 1);
        }

        // 2. Lấy tên file từ Database
        String fileName = dbHelper.getVideoFileName(currentChapterId);

        if (fileName != null && !fileName.isEmpty()) {
            playVideoSmart(fileName);
        } else {
            Toast.makeText(this, "Không tìm thấy video cho chương này!", Toast.LENGTH_SHORT).show();
        }
    }

    // --- HÀM THÔNG MINH: TỰ ĐỘNG CHỌN NGUỒN VIDEO ---
    private void playVideoSmart(String fileName) {
        try {
            // CÁCH 1: Tìm trong bộ nhớ máy (Dành cho video Giáo viên mới upload)
            File fileInStorage = new File(getFilesDir(), fileName);
            if (fileInStorage.exists()) {
                // Nếu tìm thấy trong máy -> Chạy luôn
                videoView.setVideoPath(fileInStorage.getAbsolutePath());
                setupVideoController();
                return; // Xong việc, thoát hàm
            }

            // CÁCH 2: Tìm trong thư mục RAW (Dành cho video có sẵn trong code)
            String rawName = fileName.replace(".mp4", "").trim(); // Bỏ đuôi .mp4
            int resId = getResources().getIdentifier(rawName, "raw", getPackageName());

            if (resId != 0) {
                String path = "android.resource://" + getPackageName() + "/" + resId;
                videoView.setVideoURI(Uri.parse(path));
                setupVideoController();
            } else {
                // CÁCH 3: (Dự phòng) Nếu không thấy đâu cả
                Toast.makeText(this, "Lỗi: Không tìm thấy file video " + fileName, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi phát video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupVideoController() {
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            if (btnPlayOverlay != null) {
                btnPlayOverlay.setVisibility(android.view.View.GONE);
            }
        });

        if (btnPlayOverlay != null) {
            btnPlayOverlay.setOnClickListener(v -> {
                videoView.start();
                btnPlayOverlay.setVisibility(android.view.View.GONE);
            });
        }
    }
}