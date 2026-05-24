package com.example.androidlearn.model;

public class Chapter {
    // 1. Các trường ánh xạ đúng với bảng ChuongHoc trong CSDL
    private int maChuong;       // Map với cột: MaChuong
    private String tenChuong;   // Map với cột: TenChuong
    private String moTa;        // Map với cột: MoTa
    private int thuTuBaiHoc;    // Map với cột: ThuTuBaiHoc

    // 2. Trường logic hỗ trợ giao diện (Không lưu trong bảng ChuongHoc)
    // Giá trị này sẽ được tính dựa trên kết quả thi của bài trước
    private boolean isLocked;

    // Constructor đầy đủ
    public Chapter(int maChuong, String tenChuong, String moTa, int thuTuBaiHoc, boolean isLocked) {
        this.maChuong = maChuong;
        this.tenChuong = tenChuong;
        this.moTa = moTa;
        this.thuTuBaiHoc = thuTuBaiHoc;
        this.isLocked = isLocked;
    }

    // Constructor rút gọn (dùng khi mới tạo, mặc định khoá)
    public Chapter(int maChuong, String tenChuong, String moTa, int thuTuBaiHoc) {
        this.maChuong = maChuong;
        this.tenChuong = tenChuong;
        this.moTa = moTa;
        this.thuTuBaiHoc = thuTuBaiHoc;
        this.isLocked = true; // Mặc định là khoá
    }

    // --- Getter và Setter ---

    public int getMaChuong() {
        return maChuong;
    }

    public void setMaChuong(int maChuong) {
        this.maChuong = maChuong;
    }

    public String getTenChuong() {
        return tenChuong;
    }

    public void setTenChuong(String tenChuong) {
        this.tenChuong = tenChuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getThuTuBaiHoc() {
        return thuTuBaiHoc;
    }

    public void setThuTuBaiHoc(int thuTuBaiHoc) {
        this.thuTuBaiHoc = thuTuBaiHoc;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}