package com.example.androidlearn.model;

public class Message {
    private String content;
    private String senderName;
    private String time;
    private boolean isMe; // Kiểm tra xem tin nhắn này có phải của mình không

    public Message(String content, String senderName, String time, boolean isMe) {
        this.content = content;
        this.senderName = senderName;
        this.time = time;
        this.isMe = isMe;
    }

    public String getContent() { return content; }
    public String getSenderName() { return senderName; }
    public String getTime() { return time; }
    public boolean isMe() { return isMe; }
}