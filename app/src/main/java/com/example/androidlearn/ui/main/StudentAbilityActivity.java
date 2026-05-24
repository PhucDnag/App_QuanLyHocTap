package com.example.androidlearn.ui.main;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.utils.SimpleChartView;

import java.util.ArrayList;
import java.util.List;

public class StudentAbilityActivity extends AppCompatActivity {
    private SimpleChartView chartAbility;
    private LinearLayout layoutAbilityList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_ability);

        dbHelper = new DatabaseHelper(this);
        chartAbility = findViewById(R.id.chartAbility);
        layoutAbilityList = findViewById(R.id.layoutAbilityList);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        renderAbility();
    }

    private void renderAbility() {
        int userId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("KEY_USER_ID", 1);
        List<DatabaseHelper.AbilityItem> abilities = dbHelper.getStudentAbilityAnalysis(userId);
        List<SimpleChartView.Entry> entries = new ArrayList<>();
        layoutAbilityList.removeAllViews();

        TextView title = new TextView(this);
        title.setText("Chi tiết từng mảng kiến thức");
        title.setTextColor(0xFF1976D2);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layoutAbilityList.addView(title);

        for (DatabaseHelper.AbilityItem item : abilities) {
            entries.add(new SimpleChartView.Entry(item.label, item.scorePercent));
            TextView label = new TextView(this);
            label.setText(item.label + " - " + item.scorePercent + "% • " + item.status);
            label.setTextColor(getStatusColor(item.scorePercent));
            label.setTextSize(15f);
            label.setPadding(0, 14, 0, 6);
            layoutAbilityList.addView(label);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress(item.scorePercent);
            layoutAbilityList.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 14));
        }
        chartAbility.setEntries(entries, true);
    }

    private int getStatusColor(int percent) {
        if (percent >= 80) return 0xFF2E7D32;
        if (percent >= 50) return 0xFFF57C00;
        return 0xFFD32F2F;
    }
}
