package com.example.androidlearn.ui.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.utils.SimpleChartView;

import java.util.ArrayList;
import java.util.List;

public class TeacherAnalyticsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Spinner spinnerClass;
    private TextView tvSummary, tvCompletion;
    private ProgressBar progressCompletion;
    private SimpleChartView chartScoreDistribution;
    private LinearLayout layoutWarnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_analytics);

        dbHelper = new DatabaseHelper(this);
        spinnerClass = findViewById(R.id.spinnerClass);
        tvSummary = findViewById(R.id.tvSummary);
        tvCompletion = findViewById(R.id.tvCompletion);
        progressCompletion = findViewById(R.id.progressCompletion);
        chartScoreDistribution = findViewById(R.id.chartScoreDistribution);
        layoutWarnings = findViewById(R.id.layoutWarnings);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        List<String> classes = dbHelper.getTeachingClasses(getSharedPreferences("UserSession", MODE_PRIVATE).getInt("KEY_USER_ID", -1));
        if (classes.isEmpty()) classes = dbHelper.getAllClasses();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(adapter);
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderAnalytics((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void renderAnalytics(String className) {
        DatabaseHelper.TeacherAnalytics data = dbHelper.getTeacherAnalytics(className);
        int completionPercent = data.totalItems == 0 ? 0 : Math.round((data.completedItems * 100f) / data.totalItems);
        tvSummary.setText("Lớp " + className + " • " + data.totalStudents + " sinh viên • " + data.totalChapters + " chương");
        progressCompletion.setProgress(completionPercent);
        tvCompletion.setText(completionPercent + "% hoàn thành (" + data.completedItems + "/" + data.totalItems + " lượt học)");

        int max = Math.max(1, data.excellentCount + data.goodCount + data.averageCount + data.weakCount);
        List<SimpleChartView.Entry> chartEntries = new ArrayList<>();
        chartEntries.add(new SimpleChartView.Entry("Giỏi", data.excellentCount * 100f / max));
        chartEntries.add(new SimpleChartView.Entry("Khá", data.goodCount * 100f / max));
        chartEntries.add(new SimpleChartView.Entry("TB", data.averageCount * 100f / max));
        chartEntries.add(new SimpleChartView.Entry("Yếu", data.weakCount * 100f / max));
        chartScoreDistribution.setEntries(chartEntries, false);

        layoutWarnings.removeAllViews();
        for (String warning : data.warnings) {
            TextView item = new TextView(this);
            item.setText("• " + warning);
            item.setTextColor(warning.contains("Chưa có") ? 0xFF2E7D32 : 0xFFD32F2F);
            item.setTextSize(15f);
            item.setPadding(0, 8, 0, 8);
            layoutWarnings.addView(item);
        }
    }
}
