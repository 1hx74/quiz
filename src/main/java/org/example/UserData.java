package org.example;

import java.util.Map;

public class UserData {
    private String chatId;
    private String state;
    private int level;
    private int score;
    private Map<String, Integer> allScore;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<String, Integer> getAllScore() {
        return allScore;
    }

    public void setAllScore(Map<String, Integer> allScore) {
        this.allScore = allScore;
    }
}
