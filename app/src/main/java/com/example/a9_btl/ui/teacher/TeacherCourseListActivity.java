package com.example.a9_btl.ui.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_list);

        db = new DatabaseHelper(this);
        rcv = findViewById(R.id.rcvChapters);
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
        ChapterAdapter adapter = new ChapterAdapter(chapters);
        rcv.setAdapter(adapter);
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
            // Tận dụng lại layout item_student_chapter_score.xml cho nhanh, hoặc tạo mới
            // Ở đây mình dùng layout đơn giản tự tạo trong code (để bạn đỡ phải tạo file xml mới)
            // Hoặc dùng tạm android.R.layout.simple_list_item_1 nếu muốn nhanh nhất
            // Nhưng tốt nhất là dùng lại item_assignment_card hoặc tương tự
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_chapter_score, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Chapter c = list.get(position);
            holder.tvName.setText(c.getTenChuong());
            holder.tvStatus.setText("Chỉnh sửa >"); // Dùng lại ID tvScore để hiện chữ này

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherCourseListActivity.this, TeacherAddEditChapterActivity.class);
                intent.putExtra("CHAPTER_ID", c.getMaChuong()); // Truyền ID để Sửa
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvChapterName);
                tvStatus = v.findViewById(R.id.tvScore);
            }
        }
    }
}