package com.example.a9_btl.ui.study;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.data.SessionManager;

import java.util.List;

/**
 * Màn hình quét tiến độ học của sinh viên.
 * Hiển thị trạng thái từng chương: DONE / PARTIAL / TODO
 */
public class StudyScanActivity extends AppCompatActivity {

    private TextView tvScanSubtitle;
    private TextView tvCountDone, tvCountPartial, tvCountTodo;
    private RecyclerView rcvStudyScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_scan);

        bindViews();
        loadAndDisplay();
    }

    private void bindViews() {
        tvScanSubtitle = findViewById(R.id.tvScanSubtitle);
        tvCountDone    = findViewById(R.id.tvCountDone);
        tvCountPartial = findViewById(R.id.tvCountPartial);
        tvCountTodo    = findViewById(R.id.tvCountTodo);
        rcvStudyScan   = findViewById(R.id.rcvStudyScan);

        View btnBack = findViewById(R.id.btnScanBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rcvStudyScan.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAndDisplay() {
        // Lấy userId từ SessionManager (hoặc SharedPreferences)
        int userId = getUserId();

        DatabaseHelper db = new DatabaseHelper(this);
        List<DatabaseHelper.ChapterProgress> progressList = db.getStudyProgress(userId);

        // Đếm thống kê
        int done = 0, partial = 0, todo = 0;
        for (DatabaseHelper.ChapterProgress p : progressList) {
            switch (p.getStatus()) {
                case "DONE":    done++;    break;
                case "PARTIAL": partial++; break;
                default:        todo++;    break;
            }
        }

        // Cập nhật summary
        tvCountDone.setText(String.valueOf(done));
        tvCountPartial.setText(String.valueOf(partial));
        tvCountTodo.setText(String.valueOf(todo));

        String subtitle = progressList.size() + " chương • " + done + " hoàn thành";
        if (todo > 0) subtitle += " • " + todo + " chưa học";
        tvScanSubtitle.setText(subtitle);

        // Hiển thị list
        StudyScanAdapter adapter = new StudyScanAdapter(progressList);
        rcvStudyScan.setAdapter(adapter);
    }

    private int getUserId() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("KEY_USER_ID", 1);
    }
}
