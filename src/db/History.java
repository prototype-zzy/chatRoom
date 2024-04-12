package db;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class History {
    private int id;
    private Timestamp time;
    private String sender;
    private String content;

    public History(int id, Timestamp time, String sender, String content) {
        this.id = id;
        this.time = time;
        this.sender = sender;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String senderId) {
        this.sender = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        return "[" + formatter.format(time) +"] " + sender + ": " + content;
    }
}
