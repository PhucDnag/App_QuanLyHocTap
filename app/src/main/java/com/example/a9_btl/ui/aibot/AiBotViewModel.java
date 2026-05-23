package com.example.a9_btl.ui.aibot;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.data.GeminiRepository;
import com.example.a9_btl.model.AiMessage;
import com.example.a9_btl.model.FlashCard;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel AI Bot — dùng Handler(mainLooper) cho mọi UI update
 * để tránh race condition và RecyclerView IllegalStateException.
 */
public class AiBotViewModel extends AndroidViewModel {

    // ── LiveData (chỉ dùng cho full list update + trạng thái) ─────────
    private final MutableLiveData<List<AiMessage>> messagesLiveData  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean>         isStreamingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String>          errorLiveData      = new MutableLiveData<>();
    // Flashcard event — Activity observe để mở FlashCardActivity
    private final MutableLiveData<List<FlashCard>> flashCardsLiveData = new MutableLiveData<>();

    // ── Direct callback tới Activity cho token updates ─────────────────
    /** Activity set callback này để nhận token updates trực tiếp trên main thread */
    public interface TokenUpdateListener {
        void onTokenUpdate(int streamingIndex);
        void onStreamingFinished();
    }
    private TokenUpdateListener tokenUpdateListener;

    // ── Handler đảm bảo mọi UI update đều trên main thread ────────────
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final GeminiRepository repository;
    private int streamingMessageIndex = -1;

    // Danh sách messages được quản lý trong ViewModel
    private final List<AiMessage> messages = new ArrayList<>();

    public AiBotViewModel(@NonNull Application application) {
        super(application);
        repository = new GeminiRepository();
    }

    // ── Getters ────────────────────────────────────────────────────────
    public LiveData<List<AiMessage>> getMessages()       { return messagesLiveData; }
    public LiveData<Boolean>  getIsStreaming()            { return isStreamingLiveData; }
    public LiveData<String>   getError()                 { return errorLiveData; }
    public LiveData<List<FlashCard>> getFlashCards()     { return flashCardsLiveData; }
    public List<AiMessage>    getMessageList()           { return messages; }
    public int getStreamingMessageIndex()                { return streamingMessageIndex; }

    public void setTokenUpdateListener(TokenUpdateListener listener) {
        this.tokenUpdateListener = listener;
    }

    /** Xóa toàn bộ cuộc trò chuyện */
    public void clearAllMessages() {
        cancelStream();
        messages.clear();
        messagesLiveData.setValue(new ArrayList<>(messages));
    }

    /** Dừng stream đang chạy */
    public void cancelStream() {
        repository.cancelCurrentCall();
        if (streamingMessageIndex >= 0 && streamingMessageIndex < messages.size()) {
            messages.get(streamingMessageIndex).finishStreaming();
        }
        streamingMessageIndex = -1;
        isStreamingLiveData.setValue(false);
        messagesLiveData.setValue(new ArrayList<>(messages));
        if (tokenUpdateListener != null) tokenUpdateListener.onStreamingFinished();
    }

    /** Inject tin nhắn bot tức thì (dùng cho scan kết quả, không cần gọi API) */
    public void addUserAndBotMessage(String userText, String botResponse) {
        messages.add(AiMessage.userMessage(userText));
        messages.add(AiMessage.completedBotMessage(botResponse));
        messagesLiveData.setValue(new ArrayList<>(messages));
    }

    /** Quét tiến độ học và hiển thị kết quả trong chat */
    public void scanAndShowProgress(android.content.Context context, int userId) {
        DatabaseHelper db = new DatabaseHelper(context);
        java.util.List<DatabaseHelper.ChapterProgress> list = db.getStudyProgress(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("📊 TIẾN ĐỘ HỌC TẬP\n\n");

        int done = 0, partial = 0, todo = 0;
        for (DatabaseHelper.ChapterProgress p : list) {
            String icon;
            switch (p.getStatus()) {
                case "DONE":    icon = "✅"; done++;    break;
                case "PARTIAL": icon = "🔄"; partial++; break;
                default:        icon = "❌"; todo++;    break;
            }
            sb.append(icon).append(" ").append(p.chapterName).append("\n");

            if (p.totalQuestions > 0) {
                sb.append("  • Quiz: ").append(p.quizDone ? p.quizScore + "/" + p.totalQuestions : "Chưa làm").append("\n");
            }
            if (p.hasAssignment) {
                sb.append("  • Bài tập: ").append(p.assignmentDone ? "Đã nộp" : "Chưa nộp").append("\n");
            }
            if (p.hasPdf) {
                sb.append("  • PDF: ").append(p.pdfDone ? "Đã đọc" : "Chưa đọc").append("\n");
            }
            if (p.hasVideo) {
                sb.append("  • Video: ").append(p.videoDone ? "Đã xem" : "Chưa xem").append("\n");
            }
            sb.append("\n");
        }

        sb.append("────────────\n");
        sb.append("🎯 Tổng kết: ").append(done).append(" hoàn thành | ")
          .append(partial).append(" đang học | ")
          .append(todo).append(" chưa học\n");

        if (todo > 0 || partial > 0) {
            sb.append("\n💡 Hãy hoàn thành các mục chưa đạt để mở khóa tiếp theo!");
        } else {
            sb.append("\n🎉 Xuất sắc! Bạn đã hoàn thành tất cả các chương!");
        }

        addUserAndBotMessage("📊 Quét tiến độ học", sb.toString());
    }

    /**
     * Gọi AI để tạo flashcard về chương đang học.
     * AI trả về định dạng: CARD_FRONT: ... \n CARD_BACK: ...
     * Sau khi nhận đủ, parse và post vào flashCardsLiveData.
     */
    public void generateFlashCards(String chapterName) {
        if (Boolean.TRUE.equals(isStreamingLiveData.getValue())) return;

        String prompt = "Hãy tạo 5 flashcard học tập về chủ đề: " + chapterName + ".\n"
                + "Định dạng BẮT BUỘC cho MỖI thẻ (giữ đúng tiêu đề, không thêm ký tự khác):\n"
                + "CARD_FRONT: [câu hỏi hoặc khái niệm cần nhớ]\n"
                + "CARD_BACK: [câu trả lời hoặc giải thích ngắn gọn]\n"
                + "---\n"
                + "Tạo đúng 5 cặp theo định dạng trên, bằng tiếng Việt.";

        // Hiện thông báo đang tạo
        addUserAndBotMessage(
                "🃏 Tạo flashcard: " + chapterName,
                "⏳ Đang tạo flashcard, vui lòng chờ..."
        );

        // Gọi API — collect toàn bộ response rồi parse
        final StringBuilder fullResponse = new StringBuilder();

        List<AiMessage> history = new ArrayList<>();
        history.add(AiMessage.userMessage(prompt));

        isStreamingLiveData.setValue(true);

        repository.streamMessage(history, new GeminiRepository.StreamCallback() {
            @Override
            public void onToken(String token) {
                fullResponse.append(token);
            }

            @Override
            public void onComplete() {
                mainHandler.post(() -> {
                    isStreamingLiveData.setValue(false);
                    List<FlashCard> cards = parseFlashCards(fullResponse.toString());

                    if (cards.isEmpty()) {
                        // Fallback: hiện raw text
                        addUserAndBotMessage("", "⚠️ Không parse được flashcard. AI trả lời:\n" + fullResponse);
                    } else {
                        // Update message cuối thành thông báo thành công
                        if (!messages.isEmpty()) {
                            messages.get(messages.size() - 1)
                                    .appendToken("\n✅ Đã tạo xong " + cards.size() + " thẻ! Đang mở...");
                            messagesLiveData.setValue(new ArrayList<>(messages));
                        }
                        flashCardsLiveData.setValue(cards);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isStreamingLiveData.setValue(false);
                    errorLiveData.setValue(errorMessage);
                });
            }
        });
    }

    /** Parse response AI thành List<FlashCard> */
    private List<FlashCard> parseFlashCards(String raw) {
        List<FlashCard> result = new ArrayList<>();
        String[] lines = raw.split("\n");
        String currentFront = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("CARD_FRONT:")) {
                currentFront = line.substring("CARD_FRONT:".length()).trim();
            } else if (line.startsWith("CARD_BACK:") && currentFront != null) {
                String back = line.substring("CARD_BACK:".length()).trim();
                if (!currentFront.isEmpty() && !back.isEmpty()) {
                    result.add(new FlashCard(currentFront, back));
                }
                currentFront = null;
            }
        }
        return result;
    }

    /**
     * AI chấm bài tập đã nộp của sinh viên — stream kết quả trực tiếp vào chat.
     * @param sub SubmissionForReview chứa đề bài + bài làm từ DB
     */
    public void reviewSubmission(DatabaseHelper.SubmissionForReview sub) {
        if (Boolean.TRUE.equals(isStreamingLiveData.getValue())) return;

        // Hiện user message tóm tắt
        String userLabel = "🔍 Kiểm tra bài tập: " + sub.chapterName;

        // Prompt chi tiết cho AI
        String prompt =
            "Bạn là giảng viên chấm bài. Hãy đánh giá bài làm của sinh viên dưới đây.\n\n" +
            "📋 **ĐỀ BÀI:**\n" + sub.assignmentQuestion + "\n\n" +
            "✏️ **BÀI LÀM CỦA SINH VIÊN:**\n" + sub.userAnswer + "\n\n" +
            "Hãy nhận xét theo cấu trúc:\n" +
            "1. ✅ Điểm đúng (nêu cụ thể)\n" +
            "2. ❌ Điểm cần cải thiện (nêu cụ thể)\n" +
            "3. 💡 Gợi ý bổ sung\n" +
            "4. 🎯 Điểm số đề xuất (thang 10)\n" +
            "Trả lời bằng tiếng Việt, rõ ràng và có tính khuyến khích.";

        // Thêm user message vào chat
        messages.add(AiMessage.userMessage(userLabel));

        // Thêm TYPING indicator
        AiMessage typingMsg = AiMessage.typingIndicator();
        messages.add(typingMsg);
        final int typingIndex = messages.size() - 1;
        messagesLiveData.setValue(new ArrayList<>(messages));
        isStreamingLiveData.setValue(true);
        streamingMessageIndex = -1;

        // Gọi API streaming với prompt chấm bài
        List<AiMessage> history = new ArrayList<>();
        history.add(AiMessage.userMessage(prompt));

        repository.streamMessage(history, new GeminiRepository.StreamCallback() {
            @Override
            public void onToken(String token) {
                mainHandler.post(() -> {
                    if (streamingMessageIndex == -1) {
                        AiMessage botMsg = AiMessage.streamingBotMessage();
                        botMsg.appendToken(token);
                        if (typingIndex < messages.size()) {
                            messages.set(typingIndex, botMsg);
                            streamingMessageIndex = typingIndex;
                        } else {
                            messages.add(botMsg);
                            streamingMessageIndex = messages.size() - 1;
                        }
                        messagesLiveData.setValue(new ArrayList<>(messages));
                    } else {
                        if (streamingMessageIndex < messages.size()) {
                            messages.get(streamingMessageIndex).appendToken(token);
                        }
                        if (tokenUpdateListener != null) {
                            tokenUpdateListener.onTokenUpdate(streamingMessageIndex);
                        }
                    }
                });
            }

            @Override
            public void onComplete() {
                mainHandler.post(() -> {
                    if (streamingMessageIndex >= 0 && streamingMessageIndex < messages.size()) {
                        messages.get(streamingMessageIndex).finishStreaming();
                    }
                    streamingMessageIndex = -1;
                    messagesLiveData.setValue(new ArrayList<>(messages));
                    isStreamingLiveData.setValue(false);
                    if (tokenUpdateListener != null) tokenUpdateListener.onStreamingFinished();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    if (typingIndex < messages.size()
                            && messages.get(typingIndex).getRole() == AiMessage.Role.TYPING) {
                        messages.remove(typingIndex);
                    }
                    streamingMessageIndex = -1;
                    messagesLiveData.setValue(new ArrayList<>(messages));
                    isStreamingLiveData.setValue(false);
                    errorLiveData.setValue(errorMessage);
                    if (tokenUpdateListener != null) tokenUpdateListener.onStreamingFinished();
                });
            }
        });
    }

    // ── Actions ────────────────────────────────────────────────────────

    public void sendMessage(String userText) {
        if (userText == null || userText.trim().isEmpty()) return;
        if (Boolean.TRUE.equals(isStreamingLiveData.getValue())) return;

        // 1. Thêm USER message
        messages.add(AiMessage.userMessage(userText.trim()));

        // 2. Thêm TYPING indicator
        AiMessage typingMsg = AiMessage.typingIndicator();
        messages.add(typingMsg);
        final int typingIndex = messages.size() - 1;

        // 3. Notify UI — full list (trên main thread vì sendMessage gọi từ UI)
        messagesLiveData.setValue(new ArrayList<>(messages));
        isStreamingLiveData.setValue(true);
        streamingMessageIndex = -1;

        // 4. History snapshot cho API
        List<AiMessage> historySnapshot = buildHistoryForApi();

        // 5. Gọi API streaming
        repository.streamMessage(historySnapshot, new GeminiRepository.StreamCallback() {

            @Override
            public void onToken(String token) {
                // Post lên main thread — tránh mọi threading issue
                mainHandler.post(() -> {
                    if (streamingMessageIndex == -1) {
                        // Token đầu tiên: replace TYPING → BOT bubble
                        AiMessage botMsg = AiMessage.streamingBotMessage();
                        botMsg.appendToken(token);

                        if (typingIndex < messages.size()) {
                            messages.set(typingIndex, botMsg);
                            streamingMessageIndex = typingIndex;
                        } else {
                            messages.add(botMsg);
                            streamingMessageIndex = messages.size() - 1;
                        }
                        // Full list update vì đã thay element
                        messagesLiveData.setValue(new ArrayList<>(messages));
                    } else {
                        // Các token tiếp theo: append và chỉ notify 1 item
                        if (streamingMessageIndex < messages.size()) {
                            messages.get(streamingMessageIndex).appendToken(token);
                        }
                        // Callback trực tiếp tới Activity
                        if (tokenUpdateListener != null) {
                            tokenUpdateListener.onTokenUpdate(streamingMessageIndex);
                        }
                    }
                });
            }

            @Override
            public void onComplete() {
                mainHandler.post(() -> {
                    // Finalize bot message
                    if (streamingMessageIndex >= 0 && streamingMessageIndex < messages.size()) {
                        messages.get(streamingMessageIndex).finishStreaming();
                    } else if (typingIndex < messages.size()
                            && messages.get(typingIndex).getRole() == AiMessage.Role.TYPING) {
                        messages.remove(typingIndex);
                    }

                    int finishedIdx = streamingMessageIndex;
                    streamingMessageIndex = -1;

                    messagesLiveData.setValue(new ArrayList<>(messages));
                    isStreamingLiveData.setValue(false);

                    if (tokenUpdateListener != null) {
                        tokenUpdateListener.onStreamingFinished();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    // Xoá typing indicator nếu còn
                    if (typingIndex < messages.size()
                            && messages.get(typingIndex).getRole() == AiMessage.Role.TYPING) {
                        messages.remove(typingIndex);
                    }
                    streamingMessageIndex = -1;
                    messagesLiveData.setValue(new ArrayList<>(messages));
                    isStreamingLiveData.setValue(false);
                    errorLiveData.setValue(errorMessage);

                    if (tokenUpdateListener != null) {
                        tokenUpdateListener.onStreamingFinished();
                    }
                });
            }
        });
    }

    private List<AiMessage> buildHistoryForApi() {
        List<AiMessage> history = new ArrayList<>();
        for (AiMessage msg : messages) {
            if (msg.getRole() == AiMessage.Role.TYPING) continue;
            if (msg.isStreaming()) continue;
            history.add(msg);
        }
        return history;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        tokenUpdateListener = null;
        repository.cancelCurrentCall();
    }
}
