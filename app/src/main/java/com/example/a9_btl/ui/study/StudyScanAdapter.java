package com.example.a9_btl.ui.study;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;

import java.util.List;

public class StudyScanAdapter extends RecyclerView.Adapter<StudyScanAdapter.ScanViewHolder> {

    private final List<DatabaseHelper.ChapterProgress> list;

    public StudyScanAdapter(List<DatabaseHelper.ChapterProgress> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_scan, parent, false);
        return new ScanViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder h, int position) {
        DatabaseHelper.ChapterProgress p = list.get(position);

        h.tvName.setText(p.chapterName);
        h.pb.setProgress(p.getProgressPercent());

        // Quiz status
        if (p.totalQuestions == 0) {
            h.tvQuiz.setText("📝 Quiz: Không có");
        } else if (p.quizDone) {
            h.tvQuiz.setText("✅ Quiz: " + p.quizScore + "/" + p.totalQuestions);
        } else {
            h.tvQuiz.setText("❌ Quiz: Chưa làm");
        }

        // Assignment status
        if (!p.hasAssignment) {
            h.tvAss.setText("📋 Bài tập: Không có");
        } else if (p.assignmentDone) {
            h.tvAss.setText("✅ Bài tập: Đã nộp");
        } else {
            h.tvAss.setText("❌ Bài tập: Chưa nộp");
        }

        // PDF status
        if (!p.hasPdf) {
            h.tvPdf.setText("📄 PDF: Không có");
        } else if (p.pdfDone) {
            h.tvPdf.setText("✅ PDF: Đã đọc");
        } else {
            h.tvPdf.setText("❌ PDF: Chưa đọc");
        }

        // Video status
        if (!p.hasVideo) {
            h.tvVideo.setText("🎥 Video: Không có");
        } else if (p.videoDone) {
            h.tvVideo.setText("✅ Video: Đã xem");
        } else {
            h.tvVideo.setText("❌ Video: Chưa xem");
        }

        // Badge
        switch (p.getStatus()) {
            case "DONE":
                h.tvBadge.setText("✅ Hoàn thành");
                h.tvBadge.setBackgroundColor(Color.parseColor("#2E7D32"));
                h.pb.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                break;
            case "PARTIAL":
                h.tvBadge.setText("🔄 Đang học");
                h.tvBadge.setBackgroundColor(Color.parseColor("#F57C00"));
                h.pb.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00")));
                break;
            default:
                h.tvBadge.setText("❌ Chưa học");
                h.tvBadge.setBackgroundColor(Color.parseColor("#C62828"));
                h.pb.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828")));
                break;
        }
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName, tvQuiz, tvAss, tvPdf, tvVideo, tvBadge;
        ProgressBar pb;

        ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName  = itemView.findViewById(R.id.tvScanChapterName);
            pb      = itemView.findViewById(R.id.pbScanProgress);
            tvQuiz  = itemView.findViewById(R.id.tvScanQuizStatus);
            tvAss   = itemView.findViewById(R.id.tvScanAssStatus);
            tvPdf   = itemView.findViewById(R.id.tvScanPdfStatus);
            tvVideo = itemView.findViewById(R.id.tvScanVideoStatus);
            tvBadge = itemView.findViewById(R.id.tvScanBadge);
        }
    }
}
