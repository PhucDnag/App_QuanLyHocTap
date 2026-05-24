package com.example.androidlearn.ui.aibot;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlearn.R;
import com.example.androidlearn.model.AiMessage;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị hội thoại AI Bot.
 * 3 ViewType: TYPE_USER, TYPE_BOT, TYPE_TYPING.
 *
 * Tính năng đặc biệt:
 * - Cursor nhấp nháy "▌" khi bot đang streaming
 * - Typing indicator animation (3 chấm)
 * - notifyItemChanged(index) để cập nhật real-time mà không scroll mất vị trí
 */
public class AiBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ── ViewType constants ─────────────────────────────────────────────
    private static final int TYPE_USER   = 0;
    private static final int TYPE_BOT    = 1;
    private static final int TYPE_TYPING = 2;

    // ── Cursor blink ───────────────────────────────────────────────────
    private static final String CURSOR = " ▌";
    private static final long CURSOR_BLINK_INTERVAL_MS = 500;

    // ── Callback để mở flashcard đã lưu ─────────────────────────────
    public interface OnFlashCardClickListener {
        void onOpenFlashCard(String chapterName);
    }
    private OnFlashCardClickListener flashCardClickListener;

    private List<AiMessage> messages;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Index đang được stream (để animate cursor) */
    private int streamingIndex = -1;
    private boolean cursorVisible = true;
    private Runnable cursorBlinkRunnable;

    public AiBotAdapter(List<AiMessage> messages) {
        this.messages = messages;
    }

    public void setFlashCardClickListener(OnFlashCardClickListener listener) {
        this.flashCardClickListener = listener;
    }

    // ── Adapter Lifecycle ──────────────────────────────────────────────

    public void setMessages(List<AiMessage> newMessages, int streamingIdx) {
        this.messages = newMessages;
        this.streamingIndex = streamingIdx;
        notifyDataSetChanged();
    }

    /** Gọi khi chỉ cần redraw 1 item đang stream — không đụng các item khác */
    public void notifyStreamingToken(int idx) {
        this.streamingIndex = idx;
        if (idx >= 0 && idx < getItemCount()) {
            notifyItemChanged(idx);
        }
    }

    // ── Streaming Cursor Animation ─────────────────────────────────────

    public void startCursorBlink(int streamingIdx) {
        stopCursorBlink();
        this.streamingIndex = streamingIdx;
        cursorVisible = true;
        cursorBlinkRunnable = new Runnable() {
            @Override
            public void run() {
                cursorVisible = !cursorVisible;
                if (streamingIndex >= 0 && streamingIndex < getItemCount()) {
                    notifyItemChanged(streamingIndex);
                }
                mainHandler.postDelayed(this, CURSOR_BLINK_INTERVAL_MS);
            }
        };
        mainHandler.postDelayed(cursorBlinkRunnable, CURSOR_BLINK_INTERVAL_MS);
    }

    public void stopCursorBlink() {
        if (cursorBlinkRunnable != null) {
            mainHandler.removeCallbacks(cursorBlinkRunnable);
            cursorBlinkRunnable = null;
        }
        cursorVisible = false;
        streamingIndex = -1;
    }

    // ── RecyclerView.Adapter ───────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        AiMessage msg = messages.get(position);
        switch (msg.getRole()) {
            case USER:   return TYPE_USER;
            case TYPING: return TYPE_TYPING;
            default:     return TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_USER:
                return new UserViewHolder(inflater.inflate(R.layout.item_ai_user_message, parent, false));
            case TYPE_TYPING:
                return new TypingViewHolder(inflater.inflate(R.layout.item_ai_typing, parent, false));
            default:
                return new BotViewHolder(inflater.inflate(R.layout.item_ai_bot_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiMessage msg = messages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(msg);
        } else if (holder instanceof BotViewHolder) {
            boolean isThisStreaming = (position == streamingIndex) && msg.isStreaming();
            ((BotViewHolder) holder).bind(msg, isThisStreaming, cursorVisible, flashCardClickListener);
        }
        // TypingViewHolder — animation tự chạy qua XML animator, không cần bind
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // ── ViewHolders ────────────────────────────────────────────────────

    /** ViewHolder cho tin nhắn của người dùng (căn phải) */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvContent;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvUserContent);
        }

        void bind(AiMessage msg) {
            tvContent.setText(msg.getContent());
        }
    }

    /** ViewHolder cho tin nhắn của bot (căn trái, hỗ trợ streaming cursor) */
    static class BotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvContent;
        private final MaterialButton btnOpenFlashcard;

        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvBotContent);
            btnOpenFlashcard = itemView.findViewById(R.id.btnOpenFlashcard);
        }

        void bind(AiMessage msg, boolean isStreaming, boolean cursorVisible,
                  OnFlashCardClickListener listener) {
            String text = msg.getContent();
            if (isStreaming && cursorVisible) {
                tvContent.setText(text + CURSOR);
            } else {
                tvContent.setText(text);
            }

            // Hiện nút "Mở lại Flashcard" nếu message chứa kết quả tạo flashcard thành công
            if (btnOpenFlashcard != null) {
                if (!isStreaming && text.contains("✅ Đã tạo xong") && text.contains("thẻ")) {
                    btnOpenFlashcard.setVisibility(View.VISIBLE);

                    // Trích xuất tên chương từ message USER phía trước
                    btnOpenFlashcard.setOnClickListener(v -> {
                        if (listener != null) {
                            // Trích chapterName từ nội dung bot message
                            // Bot message trước đó có dạng: "⏳ Đang tạo flashcard, vui lòng chờ...\n✅ Đã tạo xong 5 thẻ!"
                            listener.onOpenFlashCard(null); // Activity sẽ xử lý logic tìm chapter
                        }
                    });
                } else {
                    btnOpenFlashcard.setVisibility(View.GONE);
                }
            }
        }
    }

    /** ViewHolder cho typing indicator (3 chấm animation pulse) */
    static class TypingViewHolder extends RecyclerView.ViewHolder {
        private final View dot1, dot2, dot3;
        private AnimatorSet animatorSet;

        TypingViewHolder(@NonNull View itemView) {
            super(itemView);
            dot1 = itemView.findViewById(R.id.dot1);
            dot2 = itemView.findViewById(R.id.dot2);
            dot3 = itemView.findViewById(R.id.dot3);
            startDotAnimation();
        }

        private void startDotAnimation() {
            if (dot1 == null || dot2 == null || dot3 == null) return;

            animatorSet = new AnimatorSet();

            ObjectAnimator anim1 = createDotAnimator(dot1, 0);
            ObjectAnimator anim2 = createDotAnimator(dot2, 200);
            ObjectAnimator anim3 = createDotAnimator(dot3, 400);

            animatorSet.playTogether(anim1, anim2, anim3);
            animatorSet.start();
        }

        private ObjectAnimator createDotAnimator(View dot, long startDelay) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "translationY", 0f, -12f, 0f);
            animator.setDuration(600);
            animator.setStartDelay(startDelay);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            return animator;
        }
    }
}
