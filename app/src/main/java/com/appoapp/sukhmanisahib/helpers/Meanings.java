package com.appoapp.sukhmanisahib.helpers;

public class Meanings {
    public int id;
    private int page;
    private String line1 = "";
    private String line2 = "";
    private String line3 = "";
    private String line4 = "";
    private String line5 = "";

    public Meanings(int id, int page, String line1, String line2, String line3, String line4, String line5) {
        this.id = id;
        this.page = page;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
        this.line5 = line5;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public String getline1() {
        return line1;
    }
    public void setline1(String line1) {
        this.line1 = line1;
    }
    public String getline2() {
        return line2;
    }
    public void setline2(String line2) {
        this.line2 = line2;
    }
    public String getline3() {
        return line3;
    }
    public void setline3(String line3) {
        this.line3 = line3;
    }
    public String getline4() {
        return line4;
    }
    public void setline4(String line4) {
        this.line4 = line4;
    }
    public String getline5() {
        return line5;
    }
    public void setline5(String line5) {
        this.line5 = line5;
    }
}
