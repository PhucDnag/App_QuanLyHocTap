package com.example.a9_btl.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper; // Import DB
import com.example.a9_btl.model.User;         // Import Model
import com.example.a9_btl.ui.main.MainActivity;
import com.example.a9_btl.ui.teacher.TeacherMainActivity; // <--- NHỚ IMPORT MÀN HÌNH GIÁO VIÊN
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // ── Chuẩn hóa key — dùng nhất quán trong toàn app ─────────────────
    public static final String PREF_NAME    = "UserSession";
    public static final String KEY_USER_ID  = "KEY_USER_ID";
    public static final String KEY_USER_ROLE = "KEY_USER_ROLE";

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── AUTO-LOGIN: Nếu đã có session hợp lệ → bỏ qua Login ──
        android.content.SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int savedUserId = prefs.getInt(KEY_USER_ID, -1);
        int savedRole   = prefs.getInt(KEY_USER_ROLE, -1);

        if (savedUserId != -1) {
            // Đã đăng nhập trước → chuyển thẳng vào app
            navigateToMain(savedRole);
            return; // Không inflate layout Login
        }

        setContentView(R.layout.activity_login);
        databaseHelper = new DatabaseHelper(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvRegister  = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String username = edtUsername.getText() == null ? "" : edtUsername.getText().toString().trim();
        String password = edtPassword.getText() == null ? "" : edtPassword.getText().toString().trim();

        // Validate cơ bản
        if (username.isEmpty()) {
            edtUsername.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        setLoginEnabled(false);

        // GỌI DATABASE ĐỂ KIỂM TRA ở background thread.
        // Lần đầu mở DB có thể tạo bảng + import dữ liệu mẫu, nếu chạy trên UI thread dễ gây ANR.
        new Thread(() -> {
            User user = databaseHelper.checkLogin(username, password);
            runOnUiThread(() -> {
                setLoginEnabled(true);

                if (user != null) {
                    loginSuccess(user);
                } else {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void setLoginEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnLogin.setText(enabled ? "ĐĂNG NHẬP" : "ĐANG ĐĂNG NHẬP...");
    }

    private void loginSuccess(User user) {
        // 1. Lưu session vào SharedPreferences (dùng đúng key chuẩn)
        android.content.SharedPreferences.Editor editor =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_USER_ID,   user.getMaNguoiDung());
        editor.putInt(KEY_USER_ROLE, user.getQuyenHan());
        editor.apply();

        // 2. Phân quyền chuyển màn hình
        if (user.getQuyenHan() == 2) {
            Toast.makeText(this, "Xin chào Giảng viên: " + user.getHoTen(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        }
        navigateToMain(user.getQuyenHan());
        finish();
    }

    private void navigateToMain(int role) {
        Intent intent = (role == 2)
                ? new Intent(this, TeacherMainActivity.class)
                : new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}