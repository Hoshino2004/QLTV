package com.example.qltv.model;

public class Student {
    private String maSV;
    private String tenSV;

    private String sdtSV;

    public Student() {
    }

    public Student(String maSV, String tenSV, String sdtSV) {
        this.maSV = maSV;
        this.tenSV = tenSV;
        this.sdtSV = sdtSV;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getTenSV() {
        return tenSV;
    }

    public void setTenSV(String tenSV) {
        this.tenSV = tenSV;
    }

    public String getSdtSV() {
        return sdtSV;
    }

    public void setSdtSV(String sdtSV) {
        this.sdtSV = sdtSV;
    }
}
