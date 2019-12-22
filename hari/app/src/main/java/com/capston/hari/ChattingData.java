package com.capston.hari;

public class ChattingData {
    private String title;
    private String content;
    private String change;
    private String room_number;
    private String name;
    private String date;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChange() { return change; }
    public void setChange(String change) { this.change = change; }

    public String getRoom_number() { return room_number; }
    public void setRoom_number(String room_number) { this.room_number = room_number; }
}