package com.example.a9_btl.model;

/** Model flashcard: mặt trước (câu hỏi) và mặt sau (đáp án) */
public class FlashCard {
    private final String front;
    private final String back;

    public FlashCard(String front, String back) {
        this.front = front;
        this.back  = back;
    }

    public String getFront() { return front; }
    public String getBack()  { return back;  }
}
