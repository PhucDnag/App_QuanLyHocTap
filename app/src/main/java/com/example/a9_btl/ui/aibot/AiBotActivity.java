package com.example.a9_btl.ui.aibot;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.AiMessage;
import com.example.a9_btl.model.FlashCard;
import com.example.a9_btl.ui.flashcard.FlashCardActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class AiBotActivity extends AppCompatActivity
        implements AiBotViewModel.TokenUpdateListener {

    private RecyclerView         recyclerView;
    private EditText             edtInput;
    private ImageButton          btnSend;
    private ImageButton          btnStop;
    private View                 layoutRoot;
    private ChipGroup            chipGroup;
    private View                 btnReset; // Nút xóa hết tin nhắn

    private AiBotViewModel       viewModel;
    private AiBotAdapter         adapter;
    private LinearLayoutManager  layoutManager;

    private static final String[] SUGGESTIONS = {
            "📊 Quét tiến độ học",
            "🃏 Tạo Flashcard",
            "🔍 Kiểm tra bài tập",
            "📝 Tóm tắt chương học"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_bot);

        bindViews();
        setupRecyclerView();
        setupViewModel();
        setupInputBar();
        setupSuggestionChips();
    }

    // ── Setup ──────────────────────────────────────────────────────────

    private void bindViews() {
        layoutRoot   = findViewById(R.id.layoutRoot);
        recyclerView = findViewById(R.id.rcvAiChat);
        edtInput     = findViewById(R.id.edtAiInput);
        btnSend      = findViewById(R.id.btnAiSend);
        btnStop      = findViewById(R.id.btnAiStop);
        btnReset     = findViewById(R.id.btnAiReset);
        chipGroup    = findViewById(R.id.chipGroupSuggestions);

        View btnBack = findViewById(R.id.btnAiBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnStop.setOnClickListener(v -> viewModel.cancelStream());
        
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> showResetConfirmationDialog());
        }
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null); // Tắt animation tránh flicker

        adapter = new AiBotAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AiBotViewModel.class);
        viewModel.setTokenUpdateListener(this); // Đăng ký callback

        // Full list update — thêm/xoá message
        viewModel.getMessages().observe(this, messages -> {
            if (messages == null) return;
            adapter.setMessages(messages, viewModel.getStreamingMessageIndex());
            chipGroup.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
            scrollToBottom();
        });

        // Enable/disable input khi streaming
        viewModel.getIsStreaming().observe(this, isStreaming -> {
            if (isStreaming == null) return;
            // Toggle Send ↔ Stop
            btnSend.setVisibility(isStreaming ? View.GONE  : View.VISIBLE);
            btnStop.setVisibility(isStreaming ? View.VISIBLE : View.GONE);
            edtInput.setEnabled(!isStreaming);
        });

        // Flashcard event — mở FlashCardActivity khi AI tạo xong
        viewModel.getFlashCards().observe(this, cards -> {
            if (cards == null || cards.isEmpty()) return;
            ArrayList<String> fronts = new ArrayList<>();
            ArrayList<String> backs  = new ArrayList<>();
            for (FlashCard c : cards) {
                fronts.add(c.getFront());
                backs.add(c.getBack());
            }
            Intent intent = new Intent(this, FlashCardActivity.class);
            intent.putStringArrayListExtra(FlashCardActivity.EXTRA_FRONTS, fronts);
            intent.putStringArrayListExtra(FlashCardActivity.EXTRA_BACKS, backs);
            startActivity(intent);
        });

        // Hiển thị lỗi
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Snackbar.make(layoutRoot, errorMsg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // ── TokenUpdateListener ────────────────────────────────────────────

    /**
     * Gọi từ ViewModel (đã trên main thread) mỗi khi có token mới.
     * Chỉ redraw đúng 1 item — không đụng các item khác.
     */
    @Override
    public void onTokenUpdate(int streamingIndex) {
        if (streamingIndex >= 0 && streamingIndex < adapter.getItemCount()) {
            adapter.notifyStreamingToken(streamingIndex);
        }
        // Scroll chỉ khi bot bubble đang ở cuối
        scrollToBottom();
    }

    /**
     * Gọi khi stream hoàn thành hoặc lỗi.
     */
    @Override
    public void onStreamingFinished() {
        adapter.stopCursorBlink();
    }

    // ── Input ──────────────────────────────────────────────────────────

    private void setupInputBar() {
        btnSend.setOnClickListener(v -> sendCurrentMessage());
        edtInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentMessage();
                return true;
            }
            return false;
        });
    }

    private void sendCurrentMessage() {
        String text = edtInput.getText().toString().trim();
        if (text.isEmpty()) return;
        edtInput.setText("");

        adapter.startCursorBlink(-1); // Reset cursor trước khi gửi
        viewModel.sendMessage(text);
    }

    // ── Chips ──────────────────────────────────────────────────────────

    private void setupSuggestionChips() {
        for (String suggestion : SUGGESTIONS) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.colorPrimary);
            chip.setTextColor(getResources().getColor(R.color.white, getTheme()));
            chip.setOnClickListener(v -> {
                // Chip đặc biệt: quét tiến độ
                if (suggestion.startsWith("📊")) {
                    int userId = getSharedPreferences("UserSession", MODE_PRIVATE)
                            .getInt("KEY_USER_ID", 1);
                    viewModel.scanAndShowProgress(this, userId);
                    chipGroup.setVisibility(View.GONE);
                    return;
                }
                // Chip Flashcard
                if (suggestion.startsWith("🃏")) {
                    showChapterPickerForFlashCard();
                    chipGroup.setVisibility(View.GONE);
                    return;
                }
                // Chip Kiểm tra bài tập
                if (suggestion.startsWith("🔍")) {
                    showSubmissionPickerDialog();
                    chipGroup.setVisibility(View.GONE);
                    return;
                }
                // Các chip thường
                String text = suggestion.length() > 3
                        ? suggestion.substring(2).trim()
                        : suggestion;
                edtInput.setText(text);
                sendCurrentMessage();
            });
            chipGroup.addView(chip);
        }
    }

    /** Hiện dialog chọn chương để tạo flashcard */
    private void showChapterPickerForFlashCard() {
        DatabaseHelper db = new DatabaseHelper(this);
        java.util.List<com.example.a9_btl.model.Chapter> chapters = db.getAllChapters();

        String[] chapterNames = new String[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            chapterNames[i] = chapters.get(i).getTenChuong();
        }

        new AlertDialog.Builder(this)
                .setTitle("🃏 Chọn chương để tạo Flashcard")
                .setItems(chapterNames, (dialog, which) -> {
                    String selectedChapter = chapterNames[which];
                    viewModel.generateFlashCards(selectedChapter);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Hiện dialog danh sách bài đã nộp để AI chấm */
    private void showSubmissionPickerDialog() {
        int userId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getInt("KEY_USER_ID", 1);

        DatabaseHelper db = new DatabaseHelper(this);
        java.util.List<DatabaseHelper.SubmissionForReview> subs =
                db.getSubmittedAssignments(userId);

        if (subs.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🔍 Kiểm tra bài tập")
                    .setMessage("⚠️ Bạn chưa nộp bài tập nào.\n\nHãy hoàn thành và nộp bài trong mục Khóa học trước.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Hiện danh sách các bài đã nộp
        String[] labels = new String[subs.size()];
        for (int i = 0; i < subs.size(); i++) {
            labels[i] = "📝 " + subs.get(i).chapterName;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔍 Chọn bài để AI chấm")
                .setItems(labels, (dialog, which) -> {
                    viewModel.reviewSubmission(subs.get(which));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showResetConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🧹 Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử tin nhắn chat này không?")
                .setPositiveButton("Xóa hết", (dialog, which) -> {
                    viewModel.clearAllMessages();
                    Toast.makeText(this, "Đã reset cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── Scroll ─────────────────────────────────────────────────────────

    private void scrollToBottom() {
        int count = adapter.getItemCount();
        if (count > 0) {
            recyclerView.post(() -> layoutManager.scrollToPosition(count - 1));
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.setTokenUpdateListener(null); // Tránh memory leak
    }
}
