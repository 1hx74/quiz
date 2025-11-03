package org.example;

public class Producer {
    private Users users;

    public Producer() {}

    public void setUsers(Users users) {
        this.users = users;
    }

    public Content produce(Content content) {
        String chatId = content.getChatId();

        if (!users.has(chatId)) {
            UserData newUser = new UserData();
            newUser.setLevel(1);
            newUser.setScore(0);
            newUser.setState("menu");
            users.add(chatId, newUser);
        }

        UserData userData = users.get(chatId);

        Content result = new Content(true);
        result.setChatId(chatId);
        switch (content.getText()) {
            case "/start":
                result.setText("start");
                break;
            case "/help":
                result.setText("help");
                break;
            default:
                result = inState(content);
        }

        return result;
    }

    private Content inState(Content content) {
        String chatId = content.getChatId();

        Content result = new Content(true);
        result.setChatId(chatId);

        result.setText("<UNK>");
        return result;
    }
}
