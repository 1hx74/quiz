package org.example;

public class Content {
    private final boolean out;
    private String chatId;
    private String text;
    private String userClick;
    private String[] options;

    public Content(boolean out) {
        this.out = out;
    }

    public Content(boolean out, String chatId, String text, String userClick) {
        this.out = out;
        this.chatId = chatId;
        this.text = text;
        this.userClick = userClick;
    }

    public boolean isOut() {
        return out;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClick() {
        return userClick;
    }

    public void setClick(String userClick) {
        this.userClick = userClick;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }
}
