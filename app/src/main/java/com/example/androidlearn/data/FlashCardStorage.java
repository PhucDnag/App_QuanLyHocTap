package com.example.androidlearn.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.androidlearn.model.FlashCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưu và khôi phục flashcard đã tạo vào SharedPreferences.
 * Mỗi chương lưu riêng → user có thể mở lại bất cứ lúc nào.
 */
public class FlashCardStorage {

    private static final String TAG = "FlashCardStorage";
    private static final String PREF_NAME = "FlashCardData";
    private static final String KEY_PREFIX = "flashcards_";
    private static final String KEY_LAST_CHAPTER = "last_chapter_";

    private final SharedPreferences prefs;
    private final int userId;

    public FlashCardStorage(Context context, int userId) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userId = userId;
    }

    /**
     * Lưu danh sách flashcard theo tên chương.
     */
    public void saveFlashCards(String chapterName, List<FlashCard> cards) {
        try {
            JSONArray array = new JSONArray();
            for (FlashCard card : cards) {
                JSONObject obj = new JSONObject();
                obj.put("front", card.getFront());
                obj.put("back", card.getBack());
                array.put(obj);
            }
            String key = KEY_PREFIX + userId + "_" + chapterName;
            prefs.edit()
                    .putString(key, array.toString())
                    .putString(KEY_LAST_CHAPTER + userId, chapterName)
                    .apply();

            Log.d(TAG, "Saved " + cards.size() + " flashcards for: " + chapterName);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save flashcards", e);
        }
    }

    /**
     * Load flashcard theo tên chương.
     */
    public List<FlashCard> loadFlashCards(String chapterName) {
        List<FlashCard> cards = new ArrayList<>();
        String key = KEY_PREFIX + userId + "_" + chapterName;
        String json = prefs.getString(key, null);

        if (json == null || json.isEmpty()) return cards;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                cards.add(new FlashCard(
                        obj.getString("front"),
                        obj.getString("back")
                ));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to load flashcards", e);
        }
        return cards;
    }

    /**
     * Lấy tên chương được tạo flashcard gần nhất.
     */
    public String getLastChapterName() {
        return prefs.getString(KEY_LAST_CHAPTER + userId, null);
    }

    /**
     * Kiểm tra chương này đã có flashcard chưa.
     */
    public boolean hasFlashCards(String chapterName) {
        String key = KEY_PREFIX + userId + "_" + chapterName;
        return prefs.contains(key);
    }

    /**
     * Lấy danh sách tất cả tên chương đã có flashcard.
     */
    public List<String> getSavedChapterNames() {
        List<String> chapters = new ArrayList<>();
        String prefix = KEY_PREFIX + userId + "_";
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(prefix)) {
                chapters.add(key.substring(prefix.length()));
            }
        }
        return chapters;
    }
}
