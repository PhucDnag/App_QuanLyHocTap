package com.example.a9_btl.model;

/**
 * Represents a single choice for a multiple-choice question.
 */
public class Answer {
    private final String label;
    private final String text;
    private final boolean isCorrect;

    public Answer(String label, String text, boolean isCorrect) {
        this.label = label;       // e.g. "A"
        this.text = text;         // e.g. "Cấu trúc lệnh IF...THEN...ELSE...ENDIF"
        this.isCorrect = isCorrect;
    }

    public String getLabel() { return label; }
    public String getText() { return text; }
    public boolean isCorrect() { return isCorrect; }
}
