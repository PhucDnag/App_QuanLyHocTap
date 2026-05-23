package com.example.a9_btl.model;

/**
 * Represents a single message in the AI chatbot conversation.
 * Supports three states: USER message, BOT message (may be streaming), and TYPING indicator.
 */
public class AiMessage {

    public enum Role {
        USER,
        BOT,
        TYPING   // Placeholder bubble shown while waiting for first token
    }

    private Role role;
    private StringBuilder content;  // StringBuilder for efficient token appending
    private boolean isStreaming;     // true = bot is currently streaming tokens into this message
    private long timestamp;

    // ── Constructors ────────────────────────────────────────────────

    /** Create a USER message */
    public static AiMessage userMessage(String text) {
        AiMessage msg = new AiMessage();
        msg.role = Role.USER;
        msg.content = new StringBuilder(text);
        msg.isStreaming = false;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

    /** Create a BOT message that is ready to receive streaming tokens */
    public static AiMessage streamingBotMessage() {
        AiMessage msg = new AiMessage();
        msg.role = Role.BOT;
        msg.content = new StringBuilder();
        msg.isStreaming = true;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

    /** Create a TYPING indicator placeholder */
    public static AiMessage typingIndicator() {
        AiMessage msg = new AiMessage();
        msg.role = Role.TYPING;
        msg.content = new StringBuilder();
        msg.isStreaming = true;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

    /** Create a completed BOT message (not streaming, instant display) */
    public static AiMessage completedBotMessage(String text) {
        AiMessage msg = new AiMessage();
        msg.role = Role.BOT;
        msg.content = new StringBuilder(text);
        msg.isStreaming = false;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

    // ── Streaming helpers ────────────────────────────────────────────

    /** Append a token chunk from SSE stream */
    public synchronized void appendToken(String token) {
        content.append(token);
    }

    /** Mark streaming as finished */
    public synchronized void finishStreaming() {
        isStreaming = false;
    }

    // ── Getters ──────────────────────────────────────────────────────

    public Role getRole() { return role; }

    public synchronized String getContent() { return content.toString(); }

    public synchronized boolean isStreaming() { return isStreaming; }

    public long getTimestamp() { return timestamp; }

    /**
     * Convert message to Gemini API "parts" format for conversation history.
     * Used when building the request body for the next API call.
     */
    public String getApiRole() {
        return (role == Role.USER) ? "user" : "model";
    }
}
