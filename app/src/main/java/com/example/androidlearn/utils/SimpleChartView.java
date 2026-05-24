package com.example.androidlearn.utils;

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
    private static final float GRID_STROKE_WIDTH = 1.5f;
    private static final float VALUE_STROKE_WIDTH = 4f;
    private static final float LABEL_TEXT_SIZE = 22f;
    private static final float EMPTY_TEXT_SIZE = 34f;
    private static final float LABEL_GAP = 28f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Entry> entries = new ArrayList<>();
    private boolean spiderMode;

    public SimpleChartView(Context context) {
        super(context);
    }

    public SimpleChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEntries(List<Entry> data, boolean spiderMode) {
        entries.clear();
        if (data != null) {
            entries.addAll(data);
        }
        this.spiderMode = spiderMode;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) {
            drawEmpty(canvas);
            return;
        }
        if (spiderMode) {
            drawSpider(canvas);
        } else {
            drawBars(canvas);
        }
    }

    private void drawEmpty(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setTextSize(EMPTY_TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, paint);
    }

    private void drawSpider(Canvas canvas) {
        int count = entries.size();
        if (count == 0) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f + 8f;
        float maxLabelWidth = Math.max(54f, width * 0.24f);
        float labelSafeSpace = LABEL_GAP + LABEL_TEXT_SIZE + 8f;
        float radius = Math.min(width / 2f - maxLabelWidth - LABEL_GAP, height / 2f - labelSafeSpace);
        radius = Math.max(42f, radius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(GRID_STROKE_WIDTH);
        paint.setColor(Color.rgb(210, 210, 210));

        for (int level = 1; level <= 4; level++) {
            Path gridPath = new Path();
            float levelRadius = radius * level / 4f;
            for (int i = 0; i < count; i++) {
                double angle = -Math.PI / 2d + 2d * Math.PI * i / count;
                float x = centerX + (float) Math.cos(angle) * levelRadius;
                float y = centerY + (float) Math.sin(angle) * levelRadius;
                if (i == 0) {
                    gridPath.moveTo(x, y);
                } else {
                    gridPath.lineTo(x, y);
                }
            }
            gridPath.close();
            canvas.drawPath(gridPath, paint);
        }

        Path valuePath = new Path();
        paint.setStrokeWidth(GRID_STROKE_WIDTH);
        for (int i = 0; i < count; i++) {
            Entry entry = entries.get(i);
            double angle = -Math.PI / 2d + 2d * Math.PI * i / count;
            float axisX = centerX + (float) Math.cos(angle) * radius;
            float axisY = centerY + (float) Math.sin(angle) * radius;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.rgb(210, 210, 210));
            canvas.drawLine(centerX, centerY, axisX, axisY, paint);

            float valueRadius = radius * Math.max(0f, Math.min(100f, entry.value)) / 100f;
            float valueX = centerX + (float) Math.cos(angle) * valueRadius;
            float valueY = centerY + (float) Math.sin(angle) * valueRadius;
            if (i == 0) {
                valuePath.moveTo(valueX, valueY);
            } else {
                valuePath.lineTo(valueX, valueY);
            }

            drawSpiderLabel(canvas, entry.label, centerX, centerY, radius, angle, maxLabelWidth);
        }

        valuePath.close();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(VALUE_STROKE_WIDTH);
        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawPath(valuePath, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 25, 118, 210));
        canvas.drawPath(valuePath, paint);
    }

    private void drawSpiderLabel(Canvas canvas, String label, float centerX, float centerY, float radius,
                                 double angle, float maxLabelWidth) {
        String shortText = shortLabel(label);
        float labelRadius = radius + LABEL_GAP;
        float x = centerX + (float) Math.cos(angle) * labelRadius;
        float y = centerY + (float) Math.sin(angle) * labelRadius;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(LABEL_TEXT_SIZE);
        paint.setColor(Color.rgb(69, 69, 69));
        paint.setTextAlign(Paint.Align.CENTER);

        if (Math.cos(angle) > 0.35d) {
            paint.setTextAlign(Paint.Align.LEFT);
            x += 4f;
        } else if (Math.cos(angle) < -0.35d) {
            paint.setTextAlign(Paint.Align.RIGHT);
            x -= 4f;
        }

        Paint.FontMetrics metrics = paint.getFontMetrics();
        if (Math.sin(angle) < -0.35d) {
            y += -metrics.ascent;
        } else if (Math.sin(angle) > 0.35d) {
            y -= metrics.descent + 4f;
        } else {
            y -= (metrics.ascent + metrics.descent) / 2f;
        }

        float minX = getPaddingLeft() + 4f;
        float maxX = getWidth() - getPaddingRight() - 4f;
        if (paint.getTextAlign() == Paint.Align.LEFT) {
            x = Math.min(x, maxX - Math.min(maxLabelWidth, paint.measureText(shortText)));
        } else if (paint.getTextAlign() == Paint.Align.RIGHT) {
            x = Math.max(x, minX + Math.min(maxLabelWidth, paint.measureText(shortText)));
        } else {
            x = Math.max(minX + maxLabelWidth / 2f, Math.min(maxX - maxLabelWidth / 2f, x));
        }

        canvas.drawText(shortText, x, y, paint);
    }

    private void drawBars(Canvas canvas) {
        float horizontalPadding = 36f;
        float chartBottom = getHeight() - 96f;
        float barWidth = Math.max(28f, (getWidth() - horizontalPadding * 2f) / entries.size() * 0.55f);
        float space = (getWidth() - horizontalPadding * 2f - barWidth * entries.size())
                / Math.max(1, entries.size() - 1);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24f);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float left = horizontalPadding + i * (barWidth + space);
            float top = 24f + chartBottom * (1f - Math.max(0f, Math.min(100f, entry.value)) / 100f);
            paint.setColor(getColorForValue(entry.value));
            canvas.drawRoundRect(left, top, left + barWidth, chartBottom + 24f, 12f, 12f, paint);

            paint.setColor(Color.rgb(68, 68, 68));
            canvas.drawText(Math.round(entry.value) + "%", left + barWidth / 2f, top - 8f, paint);
            canvas.drawText(shortLabel(entry.label), left + barWidth / 2f, getHeight() - 26f, paint);
        }
    }

    private int getColorForValue(float value) {
        if (value >= 80f) {
            return Color.rgb(46, 125, 50);
        }
        if (value >= 50f) {
            return Color.rgb(251, 140, 0);
        }
        return Color.rgb(211, 47, 47);
    }

    private String shortLabel(String label) {
        if (label == null) {
            return "";
        }
        String trimmed = label.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            return "";
        }

        String[] words = trimmed.split(" ");
        if (words.length <= 3) {
            return trimmed;
        }

        return words[0] + " " + words[1] + " " + words[2] + "...";
    }

    public static class Entry {
        public final String label;
        public final float value;

        public Entry(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }
}
