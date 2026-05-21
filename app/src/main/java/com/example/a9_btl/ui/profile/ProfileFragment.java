package com.example.a9_btl.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.User;
import com.example.a9_btl.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private static final String PREF_SESSION = "UserSession";
    private static final String PREF_SETTINGS = "AppSettings";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_AVATAR_URI_PREFIX = "KEY_AVATAR_URI_";
    private static final String KEY_DARK_MODE = "KEY_DARK_MODE";

    private TextView tvFullName, tvClass;
    private com.google.android.material.textfield.TextInputLayout tilFullName;
    private TextInputEditText edtFullName;
    private ImageView imgAvatar;
    private MaterialButton btnLogout, btnSaveProfile, btnChangeAvatar, btnToggleDarkMode;
    private DatabaseHelper dbHelper;
    private SharedPreferences sessionPrefs;
    private SharedPreferences settingsPrefs;
    private int currentUserId = -1;

    private final ActivityResultLauncher<String> avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveAvatarUri(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionPrefs = requireActivity().getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE);
        settingsPrefs = requireActivity().getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvClass = view.findViewById(R.id.tvClass);
        tilFullName = view.findViewById(R.id.tilFullName);
        edtFullName = view.findViewById(R.id.edtFullName);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnToggleDarkMode = view.findViewById(R.id.btnToggleDarkMode);
        btnLogout = view.findViewById(R.id.btnLogout);

        currentUserId = sessionPrefs.getInt(KEY_USER_ID, -1);
        if (currentUserId != -1) {
            loadUserProfile(currentUserId);
            loadAvatar(currentUserId);
        }
        updateDarkModeButtonText();

        btnChangeAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnToggleDarkMode.setOnClickListener(v -> toggleDarkMode());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserProfile(int userId) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            boolean isTeacher = user.getQuyenHan() == 2;
            tvFullName.setText(user.getHoTen());
            tvClass.setText(isTeacher ? "Giảng viên • Lớp phụ trách: " + user.getMaLop() : "Sinh viên • Lớp: " + user.getMaLop());
            tilFullName.setHint(isTeacher ? "Tên giảng viên" : "Tên sinh viên");
            edtFullName.setText(user.getHoTen());
        } else {
            tvFullName.setText("Khách");
            tvClass.setText("Chưa đăng nhập");
            tilFullName.setHint("Họ tên");
            edtFullName.setText("");
        }
    }

    private void saveProfile() {
        String newName = edtFullName.getText() == null ? "" : edtFullName.getText().toString().trim();
        if (newName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = dbHelper.updateUserFullName(currentUserId, newName);
        if (updated) {
            tvFullName.setText(newName);
            Toast.makeText(requireContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAvatarUri(Uri uri) {
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Một số trình chọn ảnh không cấp persistable permission, vẫn lưu URI để dùng trong phiên hiện tại.
        }

        sessionPrefs.edit()
                .putString(KEY_AVATAR_URI_PREFIX + currentUserId, uri.toString())
                .apply();
        imgAvatar.setImageURI(uri);
        Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
    }

    private void loadAvatar(int userId) {
        String avatarUri = sessionPrefs.getString(KEY_AVATAR_URI_PREFIX + userId, null);
        if (avatarUri != null && !avatarUri.isEmpty()) {
            imgAvatar.setImageURI(Uri.parse(avatarUri));
        } else {
            imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void toggleDarkMode() {
        boolean enableDark = !settingsPrefs.getBoolean(KEY_DARK_MODE, false);
        settingsPrefs.edit().putBoolean(KEY_DARK_MODE, enableDark).apply();
        AppCompatDelegate.setDefaultNightMode(enableDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        updateDarkModeButtonText();
    }

    private void updateDarkModeButtonText() {
        boolean darkMode = settingsPrefs.getBoolean(KEY_DARK_MODE, false);
        btnToggleDarkMode.setText(darkMode ? "Tắt Dark Mode" : "Bật Dark Mode");
    }

    private void logout() {
        sessionPrefs.edit().clear().apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}