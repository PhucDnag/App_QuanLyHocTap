package com.example.androidlearn.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.androidlearn.model.AiMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưu và khôi phục lịch sử chat AI bot vào SharedPreferences (JSON).
 * Mỗi user có key riêng → không bị lẫn dữ liệu giữa các tài khoản.
 */
public class ChatHistoryManager {

    private static final String TAG = "ChatHistoryManager";
    private static final String PREF_NAME = "AiChatHistory";
    private static final String KEY_PREFIX = "chat_history_user_";
    private static final int MAX_MESSAGES = 100; // Giới hạn tối đa để tránh quá nặng

    private final SharedPreferences prefs;
    private final int userId;

    public ChatHistoryManager(Context context, int userId) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userId = userId;
    }

    /**
     * Lưu danh sách tin nhắn. Chỉ lưu USER và BOT đã hoàn thành (skip TYPING & streaming).
     */
    public void saveMessages(List<AiMessage> messages) {
        try {
            JSONArray array = new JSONArray();
            int startIndex = Math.max(0, messages.size() - MAX_MESSAGES);

            for (int i = startIndex; i < messages.size(); i++) {
                AiMessage msg = messages.get(i);
                // Bỏ qua TYPING indicator và message đang streaming
                if (msg.getRole() == AiMessage.Role.TYPING) continue;
                if (msg.isStreaming()) continue;

                JSONObject obj = new JSONObject();
                obj.put("role", msg.getRole().name());
                obj.put("content", msg.getContent());
                obj.put("timestamp", msg.getTimestamp());
                array.put(obj);
            }

            prefs.edit()
                    .putString(KEY_PREFIX + userId, array.toString())
                    .apply();

            Log.d(TAG, "Saved " + array.length() + " messages for user " + userId);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save chat history", e);
        }
    }

    /**
     * Khôi phục danh sách tin nhắn từ SharedPreferences.
     */
    public List<AiMessage> loadMessages() {
        List<AiMessage> messages = new ArrayList<>();
        String json = prefs.getString(KEY_PREFIX + userId, null);

        if (json == null || json.isEmpty()) {
            return messages;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String roleName = obj.getString("role");
                String content = obj.getString("content");
                long timestamp = obj.optLong("timestamp", System.currentTimeMillis());

                AiMessage.Role role;
                try {
                    role = AiMessage.Role.valueOf(roleName);
                } catch (IllegalArgumentException e) {
                    continue; // Skip invalid role
                }

                // Chỉ khôi phục USER và BOT
                if (role == AiMessage.Role.TYPING) continue;

                messages.add(AiMessage.fromSaved(role, content, timestamp));
            }
            Log.d(TAG, "Loaded " + messages.size() + " messages for user " + userId);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to load chat history", e);
        }

        return messages;
    }

    /**
     * Xóa toàn bộ lịch sử chat của user hiện tại.
     */
    public void clearHistory() {
        prefs.edit()
                .remove(KEY_PREFIX + userId)
                .apply();
        Log.d(TAG, "Cleared chat history for user " + userId);
    }
}
