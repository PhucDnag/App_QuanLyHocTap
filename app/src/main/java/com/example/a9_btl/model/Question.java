package com.example.a9_btl.model;

public class Question {
    // Thêm trường ID (để phục vụ việc Xóa câu hỏi)
    private int id;

    // Các trường cũ giữ nguyên
    private String content;
    private String answerA;
    private String answerB;
    private String answerC;
    private String answerD;
    private String correctAnswer;
    private String userAnswer = "";

    // 1. Constructor CŨ (Giữ nguyên để code Sinh viên không bị lỗi)
    public Question(String content, String a, String b, String c, String d, String correct) {
        this.content = content;
        this.answerA = a;
        this.answerB = b;
        this.answerC = c;
        this.answerD = d;
        this.correctAnswer = correct;
    }

    // 2. Constructor MỚI (Có ID - Dùng cho Giáo viên quản lý)
    public Question(int id, String content, String a, String b, String c, String d, String correct) {
        this.id = id;
        this.content = content;
        this.answerA = a;
        this.answerB = b;
        this.answerC = c;
        this.answerD = d;
        this.correctAnswer = correct;
    }

    // --- NHÓM 1: GETTER CŨ (Code cũ của bạn đang dùng cái này) ---
    public String getContent() { return content; }
    public String getAnswerA() { return answerA; }
    public String getAnswerB() { return answerB; }
    public String getAnswerC() { return answerC; }
    public String getAnswerD() { return answerD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    // --- NHÓM 2: GETTER MỚI (Cầu nối - Mapping sang biến cũ) ---
    // Các hàm này giúp file TeacherQuizManagerActivity hết báo đỏ

    public int getMaCauHoi() { return id; } // Trả về ID

    public String getNoiDung() { return content; } // Trả về content

    // (Nếu cần dùng thêm các hàm tiếng Việt ở code mới thì mở comment này ra)
    /*
    public String getDapAnA() { return answerA; }
    public String getDapAnB() { return answerB; }
    public String getDapAnC() { return answerC; }
    public String getDapAnD() { return answerD; }
    public String getDapAnDung() { return correctAnswer; }
    */
}