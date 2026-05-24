package com.example.androidlearn.data;

import android.util.Log;

import com.example.androidlearn.model.AiMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Repository giao tiếp với OpenRouter API (OpenAI-compatible).
 * Streaming SSE: data: {"choices":[{"delta":{"content":"token"}}]}
 *
 * Lấy API Key miễn phí tại: https://openrouter.ai/keys
 * Model miễn phí gợi ý: google/gemini-2.0-flash-exp:free
 */
public class GeminiRepository {

    private static final String TAG = "GeminiRepository";

    // ── Cấu hình OpenRouter ───────────────────────────────────────────
    private static final String API_KEY  = "api_key_your";
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

    private static final String[] MODELS = {
            "openai/gpt-oss-120b:free",
            "nvidia/nemotron-3-super-120b-a12b:free",
            "deepseek/deepseek-v4-flash:free"
    };

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ── System Prompt ─────────────────────────────────────────────────
    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý lập trình Android thông minh của ứng dụng QuanLyHocTap. " +
            "Hãy trả lời bằng tiếng Việt cực kỳ ngắn gọn, súc tích, đi thẳng trực diện vào vấn đề, không giải thích khái niệm dài dòng lan man, không đề xuất hay gợi ý phương pháp học tập. " +
            "Nhiệm vụ chính của bạn là giải đáp toàn bộ các câu hỏi về lập trình Android, kiến trúc máy tính, chấm điểm và review bài tập của sinh viên. " +
            "Hãy thân thiện chào hỏi và tương tác cơ bản khi bắt đầu cuộc trò chuyện. " +
            "Nếu là lập trình Android: cung cấp giải pháp hoặc code (Java/Kotlin, XML Layouts) tối ưu, giải thích siêu ngắn (1-2 câu). " +
            "Hạn chế các định dạng danh sách gạch đầu dòng phức tạp, trả lời gọn gàng và tối giản nhất. " +
            "Hãy luôn hỗ trợ nhiệt tình, KHÔNG từ chối các câu hỏi liên quan đến lập trình, máy tính, học tập hoặc tương tác thông thường.";

    // ── Callback Interface ────────────────────────────────────────────
    public interface StreamCallback {
        void onToken(String token);
        void onComplete();
        void onError(String errorMessage);
    }

    // ── OkHttp Client ─────────────────────────────────────────────────
    private final OkHttpClient client;
    private Call currentCall;

    public GeminiRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // ── Public API ────────────────────────────────────────────────────

    public void streamMessage(List<AiMessage> history, StreamCallback callback) {
        cancelCurrentCall();

        String requestBodyJson = buildRequestBody(history);
        Log.d(TAG, "Sending to OpenRouter: " + requestBodyJson);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(RequestBody.create(requestBodyJson, JSON))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://github.com/QuanLyHocTap") // optional
                .addHeader("X-Title", "QuanLyHocTap AI Bot")                  // optional
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {
                    Log.d(TAG, "Request was canceled");
                    return;
                }
                Log.e(TAG, "Network failure / timeout", e);
                callback.onError("Không thể kết nối đến server. Vui lòng kiểm tra mạng.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: HTTP " + response.code() + " " + response.message());
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "API error " + response.code() + ": " + errorBody);
                    callback.onError(parseApiError(response.code(), errorBody));
                    return;
                }
                Log.d(TAG, "Starting to parse stream...");
                parseStreamingResponse(response, callback);
            }
        });
    }

    public void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────

    /**
     * Parse SSE stream theo format OpenAI:
     * data: {"choices":[{"delta":{"content":"token"},"finish_reason":null}]}
     * data: [DONE]
     */
    private void parseStreamingResponse(Response response, StreamCallback callback) {
        ResponseBody body = response.body();
        if (body == null) {
            callback.onError("Phản hồi trống từ server.");
            return;
        }

        try (InputStream inputStream = body.byteStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                Log.d(TAG, "Received line: " + line);

                if (!line.startsWith("data: "))
                    continue;

                String jsonStr = line.substring(6).trim();

                if ("[DONE]".equals(jsonStr)) {
                    callback.onComplete();
                    return;
                }

                String token = extractTokenFromChunk(jsonStr);
                if (token != null && !token.isEmpty()) {
                    callback.onToken(token);
                }
            }

            callback.onComplete();

        } catch (IOException e) {
            if (currentCall != null && currentCall.isCanceled())
                return;
            Log.e(TAG, "Stream read error", e);
            callback.onError("Lỗi đọc phản hồi từ server.");
        }
    }

    /**
     * Trích xuất token từ OpenAI SSE chunk.
     * Cấu trúc: choices[0].delta.content
     */
    private String extractTokenFromChunk(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            JSONArray choices = root.optJSONArray("choices");
            if (choices == null || choices.length() == 0)
                return null;

            JSONObject delta = choices.getJSONObject(0).optJSONObject("delta");
            if (delta == null)
                return null;

            return delta.optString("content", null);

        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse chunk: " + jsonStr);
            return null;
        }
    }

    /**
     * Build request body theo format OpenAI Chat Completions.
     * {
     *   "model": "...",
     *   "stream": true,
     *   "messages": [
     *     {"role": "system", "content": "..."},
     *     {"role": "user",   "content": "..."},
     *     {"role": "assistant", "content": "..."}
     *   ]
     * }
     */
    private String buildRequestBody(List<AiMessage> history) {
        try {
            JSONObject root = new JSONObject();
            
            // Gán model chính mặc định (bắt buộc theo schema của API)
            root.put("model", MODELS[0]);
            
            JSONArray modelsArray = new JSONArray();
            for (String m : MODELS) {
                modelsArray.put(m);
            }
            root.put("models", modelsArray);
            root.put("stream", true);

            JSONArray messages = new JSONArray();

            // 1. System message
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.put(systemMsg);

            // 2. Conversation history
            for (AiMessage msg : history) {
                if (msg.getRole() == AiMessage.Role.TYPING)
                    continue;
                if (msg.getRole() == AiMessage.Role.BOT && msg.isStreaming())
                    continue;

                JSONObject msgObj = new JSONObject();
                // OpenAI dùng "assistant" thay vì "model"
                String role = (msg.getRole() == AiMessage.Role.USER) ? "user" : "assistant";
                msgObj.put("role", role);
                msgObj.put("content", msg.getContent());
                messages.put(msgObj);
            }

            root.put("messages", messages);

            return root.toString();

        } catch (JSONException e) {
            Log.e(TAG, "Failed to build request body", e);
            return "{}";
        }
    }

    private String parseApiError(int code, String body) {
        switch (code) {
            case 400:
                return "Yêu cầu không hợp lệ. Vui lòng thử lại.";
            case 401:
                return "API Key không hợp lệ. Vui lòng kiểm tra key OpenRouter.";
            case 402:
                return "Tài khoản OpenRouter hết credit. Vui lòng nạp thêm.";
            case 403:
                return "API Key không có quyền truy cập model này.";
            case 404:
                return "Model không tồn tại. Vui lòng kiểm tra tên model.";
            case 429:
                return "Đã vượt giới hạn yêu cầu. Vui lòng thử lại sau vài giây.";
            case 500:
            case 503:
                return "Server AI đang bận. Vui lòng thử lại.";
            default:
                return "Lỗi kết nối (code " + code + "). Vui lòng thử lại.";
        }
    }
}
