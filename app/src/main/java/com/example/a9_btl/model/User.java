package com.example.a9_btl.model;

public class User {
    private int maNguoiDung;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private int quyenHan; // 1: Sinh viên, 2: Giảng viên
    private String maLop;

    public User(int maNguoiDung, String tenDangNhap, String matKhau, String hoTen, int quyenHan, String maLop) {
        this.maNguoiDung = maNguoiDung;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.quyenHan = quyenHan;
        this.maLop = maLop;
    }

    public User() {
    }

    // Getter và Setter
    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public int getQuyenHan() { return quyenHan; }
    public void setQuyenHan(int quyenHan) { this.quyenHan = quyenHan; }

    public String getMaLop() { return maLop; }
    public void setMaLop(String maLop) { this.maLop = maLop; }
}