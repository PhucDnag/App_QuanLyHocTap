package com.example.androidlearn.ui.aibot;

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

import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.data.FlashCardStorage;
import com.example.androidlearn.model.AiMessage;
import com.example.androidlearn.model.FlashCard;
import com.example.androidlearn.ui.flashcard.FlashCardActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

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
        // Đăng ký callback mở lại flashcard từ message bot
        adapter.setFlashCardClickListener(chapterName -> openSavedFlashCards());
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AiBotViewModel.class);
        viewModel.setTokenUpdateListener(this); // Đăng ký callback

        // Load lịch sử chat từ SharedPreferences
        int userId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getInt("KEY_USER_ID", 1);
        viewModel.initChatHistory(userId);

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
        java.util.List<com.example.androidlearn.model.Chapter> chapters = db.getAllChapters();
        FlashCardStorage storage = viewModel.getFlashCardStorage();

        String[] labels = new String[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            String name = chapters.get(i).getTenChuong();
            boolean hasSaved = storage != null && storage.hasFlashCards(name);
            labels[i] = hasSaved ? name + " ✅" : name;
        }

        new AlertDialog.Builder(this)
                .setTitle("🃏 Chọn chương")
                .setItems(labels, (dialog, which) -> {
                    String selectedChapter = chapters.get(which).getTenChuong();
                    boolean hasSaved = storage != null && storage.hasFlashCards(selectedChapter);

                    if (hasSaved) {
                        // Chương đã có flashcard → hỏi tạo mới hay mở lại
                        new AlertDialog.Builder(this)
                                .setTitle("🃏 " + selectedChapter)
                                .setMessage("Chương này đã có flashcard được lưu. Bạn muốn mở lại hay tạo mới?")
                                .setPositiveButton("📂 Mở lại", (d, w) -> {
                                    openSavedFlashCards(selectedChapter);
                                })
                                .setNegativeButton("🔄 Tạo mới", (d, w) -> {
                                    viewModel.generateFlashCards(selectedChapter);
                                })
                                .setNeutralButton("Hủy", null)
                                .show();
                    } else {
                        viewModel.generateFlashCards(selectedChapter);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Mở flashcard đã lưu của chương cuối cùng được tạo */
    private void openSavedFlashCards() {
        FlashCardStorage storage = viewModel.getFlashCardStorage();
        if (storage == null) return;

        List<String> savedChapters = storage.getSavedChapterNames();
        if (savedChapters.isEmpty()) {
            Toast.makeText(this, "Chưa có flashcard nào được lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (savedChapters.size() == 1) {
            openSavedFlashCards(savedChapters.get(0));
        } else {
            // Nhiều chương → hiện dialog chọn
            String[] labels = savedChapters.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("🃏 Chọn flashcard để mở")
                    .setItems(labels, (dialog, which) -> openSavedFlashCards(savedChapters.get(which)))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    /** Mở flashcard đã lưu của chương cụ thể */
    private void openSavedFlashCards(String chapterName) {
        FlashCardStorage storage = viewModel.getFlashCardStorage();
        if (storage == null) return;

        List<FlashCard> cards = storage.loadFlashCards(chapterName);
        if (cards.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy flashcard đã lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> fronts = new ArrayList<>();
        ArrayList<String> backs = new ArrayList<>();
        for (FlashCard c : cards) {
            fronts.add(c.getFront());
            backs.add(c.getBack());
        }
        Intent intent = new Intent(this, FlashCardActivity.class);
        intent.putStringArrayListExtra(FlashCardActivity.EXTRA_FRONTS, fronts);
        intent.putStringArrayListExtra(FlashCardActivity.EXTRA_BACKS, backs);
        intent.putExtra(FlashCardActivity.EXTRA_CHAPTER_NAME, chapterName);
        startActivity(intent);
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
