package com.appoapp.sukhmanisahib.helpers;

public class Path {
    public int id;
    public String title = "";
    public String data = "";

    public Path(int id, String title, String data) {
        this.id = id;
        this.title = title;
        this.data = data;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String gettitle() {
        return title;
    }
    public void settitle(String title) {
        this.title = title;
    }
    public String getdata() {
        return data;
    }
    public void setdata(String data) {
        this.data = data;
    }
}
