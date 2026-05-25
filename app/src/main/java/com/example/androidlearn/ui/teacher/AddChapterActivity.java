package com.example.androidlearn.ui.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class AddChapterActivity extends AppCompatActivity {

    private TextInputEditText edtChapterName, edtChapterDesc;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chapter);

        dbHelper = new DatabaseHelper(this);
        edtChapterName = findViewById(R.id.edtChapterName);
        edtChapterDesc = findViewById(R.id.edtChapterDesc);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveChapter());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveChapter() {
        String name = edtChapterName.getText() == null ? "" : edtChapterName.getText().toString().trim();
        String desc = edtChapterDesc.getText() == null ? "" : edtChapterDesc.getText().toString().trim();

        if (name.isEmpty()) {
            edtChapterName.setError("Vui lòng nhập tên chương");
            edtChapterName.requestFocus();
            return;
        }

        dbHelper.addChapter(name, desc);
        Toast.makeText(this, "Đã thêm chương học", Toast.LENGTH_SHORT).show();
        finish();
    }
}
