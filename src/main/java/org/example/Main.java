package org.example;

/**
 * Главный класс приложения для запуска телеграм бота викторины.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск телеграм бота...");

        Users users = new Users();
        Producer producer = new Producer();
        producer.setUsers(users);

        Token token = new Token(); // теперь токен загружается в конструкторе

        if (!token.isValid()) {
            System.err.println("Ошибка: Токен бота не найден!");
            System.err.println("Убедитесь, что файл bot_token.txt существует в ресурсах");
            return;
        }

        String botToken = token.get();
        Bot bot = new Bot(botToken);
        bot.setProduser(producer);

        System.out.println("Бот инициализирован, запуск...");
        bot.start();
    }
}