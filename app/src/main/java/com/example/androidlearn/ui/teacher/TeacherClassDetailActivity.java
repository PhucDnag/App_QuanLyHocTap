package com.example.androidlearn.ui.teacher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Chapter;
import com.example.androidlearn.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class TeacherClassDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_class_detail);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        RecyclerView rcvStudents = findViewById(R.id.rcvStudents);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 1. Nhận tên lớp
        String className = "Lớp học";
        if (getIntent().hasExtra("CLASS_NAME")) {
            className = getIntent().getStringExtra("CLASS_NAME");
            tvHeaderTitle.setText("Lớp " + className);
        }

        // 2. Lấy danh sách SV
        List<User> studentList = dbHelper.getStudentsByClass(className);
        if (studentList.isEmpty()) {
            Toast.makeText(this, "Lớp này trống!", Toast.LENGTH_SHORT).show();
        }

        // 3. Hiển thị lên RecyclerView
        rcvStudents.setLayoutManager(new LinearLayoutManager(this));
        rcvStudents.setAdapter(new StudentListAdapter(this, studentList, dbHelper));
    }

    // --- ADAPTER DANH SÁCH SINH VIÊN (Inner Class) ---
    public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder> {
        private Context context;
        private List<User> list;
        private DatabaseHelper db;

        public StudentListAdapter(Context context, List<User> list, DatabaseHelper db) {
            this.context = context;
            this.list = list;
            this.db = db;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_student_progress, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User s = list.get(position);
            holder.tvName.setText(s.getHoTen());

            // Hiển thị chương hiện tại
            Chapter c = db.getCurrentChapter(s.getMaNguoiDung());
            if (c != null) {
                holder.tvProgress.setText("Đang học: " + c.getTenChuong());
                holder.tvProgress.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            } else {
                holder.tvProgress.setText("Chưa học");
                holder.tvProgress.setTextColor(android.graphics.Color.GRAY);
            }

            // --- SỰ KIỆN CLICK: CHUYỂN MÀN HÌNH ---
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TeacherStudentDetailActivity.class);
                intent.putExtra("STUDENT_ID", s.getMaNguoiDung());   // Gửi ID
                intent.putExtra("STUDENT_NAME", s.getHoTen());       // Gửi Tên
                context.startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvProgress;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStudentName);
                tvProgress = v.findViewById(R.id.tvProgress);
            }
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