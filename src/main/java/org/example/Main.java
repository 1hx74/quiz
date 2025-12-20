package org.example;

import org.example.ModeGame.Duel.DuelMatchmaker;
import org.example.ModeGame.Duel.Timer.DuelTimeoutManager;
import org.example.Quiz.Users;
import org.example.Tokens.TokenInterface;
import org.example.Tokens.Tokens;

/**
 * Главный класс приложения для запуска телеграм бота викторины.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск телеграм бота...");

        Users users = new Users();

        TokenInterface token = new Tokens();

        if (token.getTelegramToken().isEmpty()) {
            System.err.println("Ошибка: Токен бота не найден!");
            System.err.println("Убедитесь, что файл bot_token.txt существует в ресурсах");
            return;
        }

        Producer producer = new Producer(users, token.getOpenRouterToken());

        DuelMatchmaker matchmaker = new DuelMatchmaker();
        DuelTimeoutManager timeoutManager = new DuelTimeoutManager(matchmaker);
        producer.setDuelMatchmaker(matchmaker);
        producer.setDuelTimeoutManager(timeoutManager);

        producer.startInitTimeoutNotifier();

        String botToken = token.getTelegramToken();
        System.out.println(botToken);
        Bot bot = new Bot(botToken);
        bot.setProducer(producer);

        System.out.println("Бот инициализирован, запуск...");
        bot.start();
    }
}