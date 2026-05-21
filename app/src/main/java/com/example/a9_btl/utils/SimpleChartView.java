package com.example.a9_btl.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SimpleChartView extends View {
    public static class Entry {
        public final String label;
        public final float value;

        public Entry(String label, float value) {
            this.label = label;
            this.value = Math.max(0, Math.min(100, value));
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Entry> entries = new ArrayList<>();
    private boolean spiderMode = false;

    public SimpleChartView(Context context) {
        super(context);
    }

    public SimpleChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEntries(List<Entry> entries, boolean spiderMode) {
        this.entries.clear();
        if (entries != null) this.entries.addAll(entries);
        this.spiderMode = spiderMode;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) {
            paint.setColor(Color.GRAY);
            paint.setTextSize(34f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, paint);
            return;
        }
        if (spiderMode) drawSpider(canvas); else drawBars(canvas);
    }

    private void drawBars(Canvas canvas) {
        float padding = 36f;
        float chartHeight = getHeight() - 96f;
        float barWidth = Math.max(28f, (getWidth() - padding * 2) / entries.size() * 0.55f);
        float gap = ((getWidth() - padding * 2) - barWidth * entries.size()) / Math.max(1, entries.size() - 1);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24f);

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            float left = padding + i * (barWidth + gap);
            float top = 24f + chartHeight * (1f - e.value / 100f);
            paint.setColor(getColorForValue(e.value));
            canvas.drawRoundRect(left, top, left + barWidth, 24f + chartHeight, 12f, 12f, paint);
            paint.setColor(Color.DKGRAY);
            canvas.drawText(Math.round(e.value) + "%", left + barWidth / 2f, top - 8f, paint);
            canvas.drawText(shortLabel(e.label), left + barWidth / 2f, getHeight() - 26f, paint);
        }
    }

    private void drawSpider(Canvas canvas) {
        int n = entries.size();
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f + 8f;
        float radius = Math.min(getWidth(), getHeight()) * 0.31f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.LTGRAY);
        for (int level = 1; level <= 4; level++) {
            Path grid = new Path();
            float r = radius * level / 4f;
            for (int i = 0; i < n; i++) {
                double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
                float x = cx + (float) Math.cos(angle) * r;
                float y = cy + (float) Math.sin(angle) * r;
                if (i == 0) grid.moveTo(x, y); else grid.lineTo(x, y);
            }
            grid.close();
            canvas.drawPath(grid, paint);
        }

        Path data = new Path();
        paint.setStrokeWidth(4f);
        for (int i = 0; i < n; i++) {
            Entry e = entries.get(i);
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            float outerX = cx + (float) Math.cos(angle) * radius;
            float outerY = cy + (float) Math.sin(angle) * radius;
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(cx, cy, outerX, outerY, paint);

            float r = radius * e.value / 100f;
            float x = cx + (float) Math.cos(angle) * r;
            float y = cy + (float) Math.sin(angle) * r;
            if (i == 0) data.moveTo(x, y); else data.lineTo(x, y);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(24f);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.DKGRAY);
            canvas.drawText(shortLabel(e.label), cx + (float) Math.cos(angle) * (radius + 44f), cy + (float) Math.sin(angle) * (radius + 44f), paint);
            paint.setStyle(Paint.Style.STROKE);
        }
        data.close();
        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawPath(data, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 25, 118, 210));
        canvas.drawPath(data, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private int getColorForValue(float value) {
        if (value >= 80) return Color.rgb(46, 125, 50);
        if (value >= 50) return Color.rgb(251, 140, 0);
        return Color.rgb(211, 47, 47);
    }

    private String shortLabel(String label) {
        if (label == null) return "";
        return label.length() > 10 ? label.substring(0, 10) : label;
    }
}
