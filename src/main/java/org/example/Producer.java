package org.example;

public class Producer {
    public Content produce(Content content) {
        Content result = new Content(true);
        result.setChatId(content.getChatId());
        result.setText(content.getText());
        return result;
    }
}
