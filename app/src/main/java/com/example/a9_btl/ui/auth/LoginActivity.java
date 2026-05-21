package com.example.a9_btl.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper; // Import DB
import com.example.a9_btl.model.User;         // Import Model
import com.example.a9_btl.ui.main.MainActivity;
import com.example.a9_btl.ui.teacher.TeacherMainActivity; // <--- NHỚ IMPORT MÀN HÌNH GIÁO VIÊN
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private Button btnLogin;

    // Khai báo DatabaseHelper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DB
        databaseHelper = new DatabaseHelper(this);

        // Ánh xạ
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate cơ bản
        if (username.isEmpty()) {
            edtUsername.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // GỌI DATABASE ĐỂ KIỂM TRA
        User user = databaseHelper.checkLogin(username, password);

        if (user != null) {
            // 1. Đăng nhập thành công -> LƯU ID VÀO BỘ NHỚ
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("KEY_USER_ID", user.getMaNguoiDung()); // Lưu ID
            editor.putInt("KEY_USER_ROLE", user.getQuyenHan());  // Lưu thêm Quyền (để dùng sau này nếu cần)
            editor.apply(); // Xác nhận lưu

            // 2. PHÂN QUYỀN CHUYỂN MÀN HÌNH (LOGIC MỚI Ở ĐÂY)
            if (user.getQuyenHan() == 2) {
                // --- LÀ GIÁO VIÊN ---
                Toast.makeText(this, "Xin chào Giảng viên: " + user.getHoTen(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
                startActivity(intent);
            } else {
                // --- LÀ SINH VIÊN ---
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            // Đóng màn hình Login để không quay lại được bằng nút Back
            finish();

        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
}