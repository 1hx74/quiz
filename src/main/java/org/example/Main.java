package org.example;

import org.example.Quiz.Memory;
import org.example.Quiz.Users;
import org.example.Tokens.Tokens;
import org.example.Tokens.Tokens;
import org.example.TopicSelector.TopicSelector;

/**
 * Главный класс приложения для запуска телеграм бота викторины.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск телеграм бота...");

        Users users = new Users();
        Producer producer = new Producer();
        producer.setUsers(users);
        Tokens token = new Tokens();

        if (!token.isValidForTelegramToken()) {
            System.err.println("Ошибка: Токен бота не найден!");
            System.err.println("Убедитесь, что файл bot_token.txt существует в ресурсах");
            return;
        }

       String botToken = token.getTelegramToken();
        System.out.println(botToken);
        Bot bot = new Bot(botToken);
        bot.setProducer(producer);

        System.out.println("Бот инициализирован, запуск...");
        bot.start();
    }
}