package db;

import java.sql.Timestamp;

public class History {
    private int id;
    private Timestamp time;
    private int senderId;
    private String content;

    public History(int id, Timestamp time, int senderId, String content) {
        this.id = id;
        this.time = time;
        this.senderId = senderId;
        this.content = content;
    }

    public History() {
    }

    public int getId() {
        return id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
