package com.example.a9_btl.ui.course;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.adapter.ChapterAdapter; // Hoặc CourseAdapter nếu bạn đã đổi tên
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Chapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CourseFragment extends Fragment {

    private RecyclerView rcvChapters;
    private ChapterAdapter adapter; // Nếu bạn dùng CourseAdapter thì sửa tên ở đây
    private List<Chapter> mListChapter;
    private MaterialButton btnBack;
    private DatabaseHelper dbHelper;
    private TextView tvCurrentChapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo Database
        dbHelper = new DatabaseHelper(getContext());

        // 2. Ánh xạ View
        rcvChapters = view.findViewById(R.id.rcvChapters);
        btnBack = view.findViewById(R.id.btnBack);
        tvCurrentChapter = view.findViewById(R.id.tvCurrentChapter); // Ánh xạ Header Tiến độ

        // 3. Xử lý nút Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().finish();
            });
        }

        // 4. Cài đặt RecyclerView
        rcvChapters.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dữ liệu sẽ được load trong onResume để luôn cập nhật mới nhất
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // <--- QUAN TRỌNG: Load lại dữ liệu mỗi khi màn hình hiện lên
        updateHeaderProgress(); // <--- QUAN TRỌNG: Cập nhật thẻ Tiến độ
    }

    private void loadData() {
        // 1. Lấy ID User hiện tại
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int myId = prefs.getInt("KEY_USER_ID", 1);

        // 2. Lấy danh sách chương từ Database (Thay vì viết cứng)
        mListChapter = dbHelper.getAllChapters();

        if (mListChapter == null) mListChapter = new ArrayList<>();

        // 3. DUYỆT QUA TỪNG CHƯƠNG ĐỂ TÍNH TOÁN TRẠNG THÁI (MỞ/KHOÁ)
        for (Chapter chapter : mListChapter) {
            // Hỏi Database xem chương này có được mở không?
            boolean isUnlocked = dbHelper.isChapterUnlocked(myId, chapter.getMaChuong());

            // Cập nhật trạng thái vào object Chapter
            // (Giả sử model Chapter của bạn có hàm setIsLocked hoặc constructor hỗ trợ)
            // Nếu bạn dùng constructor cũ: new Chapter(id, ten, mota, id, locked)
            // Bạn có thể set lại thuộc tính locked ở đây. Ví dụ:
            chapter.setLocked(!isUnlocked); // Lưu ý: Unlocked ngược với Locked
        }

        // 4. Đưa vào Adapter
        if (adapter == null) {
            adapter = new ChapterAdapter(getContext(), mListChapter, new ChapterAdapter.IClickItemListener() {
                @Override
                public void onClickItem(Chapter chapter) {
                    // Kiểm tra lại lần nữa cho chắc
                    if (!chapter.isLocked()) {
                        goToLesson(chapter);
                    } else {
                        Toast.makeText(getContext(), "Bạn phải hoàn thành chương trước!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            rcvChapters.setAdapter(adapter);
        } else {
            // Nếu adapter đã có, chỉ cần cập nhật dữ liệu mới
            // (Bạn cần thêm hàm setData hoặc tạo mới adapter cũng được)
            adapter = new ChapterAdapter(getContext(), mListChapter, new ChapterAdapter.IClickItemListener() {
                @Override
                public void onClickItem(Chapter chapter) {
                    if (!chapter.isLocked()) {
                        goToLesson(chapter);
                    } else {
                        Toast.makeText(getContext(), "Bạn phải hoàn thành chương trước!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            rcvChapters.setAdapter(adapter);
        }
    }

    // Hàm cập nhật Header "Tiến độ: Chương..."
    private void updateHeaderProgress() {
        if (tvCurrentChapter == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int myId = prefs.getInt("KEY_USER_ID", 1);

        // Tìm chương cao nhất đang mở
        Chapter current = dbHelper.getCurrentChapter(myId);

        if (current != null) {
            tvCurrentChapter.setText(current.getTenChuong());
        }
    }

    private void goToLesson(Chapter chapter) {
        Intent intent = new Intent(getActivity(), LessonActivity.class); // Hoặc Activity chứa 4 nút PDF/Video
        intent.putExtra("CHAPTER_ID", chapter.getMaChuong()); // Nhớ gửi ID
        intent.putExtra("CHAPTER_NAME", chapter.getTenChuong());
        startActivity(intent);
    }
}