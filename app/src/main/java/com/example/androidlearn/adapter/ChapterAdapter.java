package com.example.androidlearn.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Thư viện để lấy màu từ resources
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlearn.R;
import com.example.androidlearn.model.Chapter;
import com.google.android.material.card.MaterialCardView; // Import thẻ Material

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private Context context;
    private List<Chapter> mListChapter;
    private IClickItemListener iClickItemListener;

    // Interface để gửi sự kiện click ra bên ngoài (cho Activity xử lý)
    public interface IClickItemListener {
        void onClickItem(Chapter chapter);
    }

    public ChapterAdapter(Context context, List<Chapter> mListChapter, IClickItemListener listener) {
        this.context = context;
        this.mListChapter = mListChapter;
        this.iClickItemListener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = mListChapter.get(position);
        if (chapter == null) return;

        // 1. Gán tên chương (Sử dụng getTenChuong khớp với bảng CSDL)
        holder.tvChapterName.setText(chapter.getTenChuong());

        // 2. Xử lý Logic Giao diện theo trạng thái
        if (chapter.isLocked()) {
            // --- TRƯỜNG HỢP: KHOÁ ---

            // Text trạng thái
            holder.tvStatus.setText("Khoá");
            holder.tvStatus.setTextColor(Color.GRAY);

            // Tên chương: Màu xám nhạt để thể hiện bị vô hiệu hóa
            holder.tvChapterName.setTextColor(Color.parseColor("#9E9E9E"));

            // CardView: Viền xám, bỏ đổ bóng (Elevation = 0)
            holder.cardChapter.setStrokeColor(Color.parseColor("#E0E0E0"));
            holder.cardChapter.setCardElevation(0);

            // Sự kiện Click: Vô hiệu hóa (set null)
            holder.itemView.setOnClickListener(null);

        } else {
            // --- TRƯỜNG HỢP: MỞ ---

            // Lấy màu xanh chủ đạo từ file colors.xml
            int colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary);

            // Text trạng thái
            holder.tvStatus.setText("Mở");
            holder.tvStatus.setTextColor(colorPrimary);

            // Tên chương: Màu đen rõ ràng
            holder.tvChapterName.setTextColor(Color.BLACK);

            // CardView: Viền xanh, có đổ bóng nổi lên
            holder.cardChapter.setStrokeColor(colorPrimary);
            holder.cardChapter.setCardElevation(8); // Đơn vị pixel tương đương 2-4dp

            // Sự kiện Click: Kích hoạt
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iClickItemListener.onClickItem(chapter);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mListChapter != null) {
            return mListChapter.size();
        }
        return 0;
    }

    // Lớp ViewHolder ánh xạ các view trong item_chapter.xml
    public class ChapterViewHolder extends RecyclerView.ViewHolder {

        private TextView tvChapterName;
        private TextView tvStatus;
        private MaterialCardView cardChapter; // Dùng MaterialCardView để chỉnh strokeColor

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ ID theo file item_chapter.xml
            tvChapterName = itemView.findViewById(R.id.tvChapterName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardChapter = itemView.findViewById(R.id.cardChapter);
        }
    }
}