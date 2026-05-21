package com.example.a9_btl.ui.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class TeacherCourseListActivity extends AppCompatActivity {

    private RecyclerView rcv;
    private TextView tvCourseSummary;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_list);

        db = new DatabaseHelper(this);
        rcv = findViewById(R.id.rcvChapters);
        tvCourseSummary = findViewById(R.id.tvCourseSummary);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút Thêm Mới
        Button btnNew = findViewById(R.id.btnNewChapter);
        btnNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherAddEditChapterActivity.class);
            startActivity(intent);
        });

        rcv.setLayoutManager(new LinearLayoutManager(this));
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Load lại khi quay về (để cập nhật dữ liệu mới thêm/sửa)
    }

    private void loadData() {
        List<Chapter> chapters = db.getAllChapters();
        tvCourseSummary.setText("Đang có " + chapters.size() + " chương học trong khóa Kiến trúc máy tính");
        ChapterAdapter adapter = new ChapterAdapter(chapters);
        rcv.setAdapter(adapter);
    }

    private void openEditChapter(Chapter chapter) {
        Intent intent = new Intent(TeacherCourseListActivity.this, TeacherAddEditChapterActivity.class);
        intent.putExtra("CHAPTER_ID", chapter.getMaChuong());
        startActivity(intent);
    }

    private void confirmDeleteChapter(Chapter chapter) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chương học")
                .setMessage("Bạn chắc chắn muốn xóa \"" + chapter.getTenChuong() + "\"? Dữ liệu tài liệu, câu hỏi, điểm và bài nộp liên quan cũng sẽ bị xóa.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = db.deleteChapterFull(chapter.getMaChuong());
                    Toast.makeText(this, deleted ? "Đã xóa chương học" : "Không thể xóa chương học", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .show();
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

    // --- ADAPTER ---
    class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {
        List<Chapter> list;
        public ChapterAdapter(List<Chapter> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_chapter, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Chapter c = list.get(position);
            holder.tvOrder.setText(String.valueOf(c.getThuTuBaiHoc()));
            holder.tvName.setText(c.getTenChuong());
            String pdf = db.getPdfFileName(c.getMaChuong()).isEmpty() ? "Chưa có PDF" : "Có PDF";
            String video = db.getVideoFileName(c.getMaChuong()).isEmpty() ? "Chưa có video" : "Có video";
            String assignment = db.hasAssignmentInChapter(c.getMaChuong()) ? "Có bài tập" : "Chưa có bài tập";
            holder.tvDesc.setText(pdf + " • " + video + " • " + assignment);

            holder.itemView.setOnClickListener(v -> openEditChapter(c));
            holder.btnEdit.setOnClickListener(v -> openEditChapter(c));
            holder.btnDelete.setOnClickListener(v -> confirmDeleteChapter(c));
        }

        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrder, tvName, tvDesc;
            com.google.android.material.button.MaterialButton btnEdit, btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvOrder = v.findViewById(R.id.tvChapterOrder);
                tvName = v.findViewById(R.id.tvChapterName);
                tvDesc = v.findViewById(R.id.tvChapterDesc);
                btnEdit = v.findViewById(R.id.btnEditChapter);
                btnDelete = v.findViewById(R.id.btnDeleteChapter);
            }
        }
    }
}