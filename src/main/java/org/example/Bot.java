package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Телеграм бот для проведения викторин.
 * Обрабатывает входящие сообщения и callback-запросы, управляет состоянием викторины.
 *
 * @author org.example
 * @version 1.0
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private Producer producer;
    private final TelegramClient telegramClient;
    private final String botToken;
    private TelegramBotsLongPollingApplication botsApplication;

    /**
     * Конструктор бота.
     *
     * @param botToken токен бота полученный от BotFather
     */
    public Bot(String botToken) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        System.out.println("[BOT] Бот инициализирован с токеном: " + botToken.substring(0, 10) + "...");
    }

    /**
     * Устанавливает продюсера для обработки контента.
     *
     * @param producer объект продюсера
     */
    public void setProduser(Producer producer) {
        this.producer = producer;
        System.out.println("[BOT] Установлен Producer");
    }

    /**
     * Регистрирует команды бота в меню.
     */
    private void registerBotCommands() {
        try {
            System.out.println("[BOT] Регистрация команд бота...");
            boolean success = telegramClient.execute(
                    SetMyCommands.builder()
                            .commands(Arrays.asList(
                                    new BotCommand("start", "Запустить бота"),
                                    new BotCommand("help", "Помощь"),
                                    new BotCommand("leaderboard", "Топ 5 игроков")
                            ))
                            .scope(new BotCommandScopeDefault())
                            .build()
            );

            System.out.println(success
                    ? "[BOT] Команды бота успешно зарегистрированы"
                    : "[BOT] Ошибка регистрации команд бота!"
            );

        } catch (Exception e) {
            System.err.println("[BOT] Ошибка регистрации команд");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Парсит входящее обновление от Telegram API.
     *
     * @param update входящее обновление
     * @return объект Content с извлеченными данными
     */
    private Content parse(Update update) {
        System.out.println("[BOT] Парсинг входящего обновления");

        if (update.hasMessage()) {
            Message msg = update.getMessage();
            String chatId = String.valueOf(msg.getChatId());
            String text = msg.hasText() ? msg.getText() : "";

            System.out.println("[BOT] Получено сообщение от " + chatId + ": " + text);
            return new Content(false, chatId, text);

        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            String chatId = String.valueOf(query.getMessage().getChatId());
            String clickData = query.getData();

            System.out.println("[BOT] Получен callback от " + chatId + ": " + clickData);
            return new Content(false, chatId, null, new String[]{clickData});
        }

        // Если тип обновления не поддерживается, возвращаем пустой контент
        return new Content(false);
    }

    /**
     * Преобразует объект Content в сообщение для отправки.
     *
     * @param content объект с данными для отправки
     * @return объект SendMessage для отправки через Telegram API или null если отправка не требуется
     */
    private SendMessage toMessage(Content content) {
        if (!content.isOut()) {
            return null;
        }

        System.out.println("[BOT] Преобразование Content в SendMessage ");
        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                .chatId(content.getChatId());

        if (content.getText() != null && !content.getText().isEmpty()) {
            builder.text(content.getText());
        } else {
            System.out.println("[BOT] Нет текста для отправки");
            return null;
        }

        if (content.getOptions() != null && content.getOptions().length > 0) {
            System.out.println("[BOT] Добавление клавиатуры с " + content.getOptions().length + " кнопками");
            List<KeyboardRow> rows = new ArrayList<>();
            for (String option : content.getOptions()) {
                KeyboardRow row = new KeyboardRow();
                row.add(option);
                rows.add(row);
            }

            ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                    .keyboard(rows)
                    .resizeKeyboard(true)
                    .oneTimeKeyboard(false)
                    .build();

            builder.replyMarkup(keyboard);
        }

        return builder.build();
    }

    /**
     * Отправляет сообщение через Telegram API
     * @param message сообщение для отправки
     */
    public void sendMessage(SendMessage message) {
        if (message == null) {
            System.out.println("[BOT] Попытка отправить пустое сообщение");
            return;
        }

        try {
            System.out.println("[BOT] Отправка сообщения для " + message.getChatId());
            telegramClient.execute(message);
            System.out.println("[BOT] Сообщение успешно отправлено");
        } catch (TelegramApiException e) {
            System.err.println("[BOT] Ошибка отправки сообщения");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Обрабатывает входящее обновление от Telegram API.
     *
     * @param update входящее обновление
     */
    public void consume(Update update) {
        try {
            System.out.println("[BOT] НОВОЕ ОБНОВЛЕНИЕ ");
            Content content = parse(update);
            Content[] messages = producer.produce(content);

            System.out.println("[BOT] Producer вернул " + messages.length + " сообщений для отправки");
            for (int i = 0; i < messages.length; i++) {
                Content message = messages[i];
                System.out.println("[BOT] Обработка сообщения " + (i + 1) + "/" + messages.length);
                SendMessage telegramMessage = toMessage(message);
                if (telegramMessage != null) {
                    sendMessage(telegramMessage);
                }
            }
            System.out.println("[BOT] ОБРАБОТКА ЗАВЕРШЕНА \n");
        } catch (Exception e) {
            System.err.println("[BOT] Критическая ошибка обработки обновления");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Остановка бота
     */
    public void stop() {
        System.out.println("[BOT] Остановка бота...");
        if (botsApplication != null) {
            try {
                botsApplication.close();
                System.out.println("[BOT] LongPollingApplication закрыт");
            } catch (Exception e) {
                System.err.println("[BOT] Ошибка при закрытии LongPollingApplication");
                e.printStackTrace(System.err);
            }
        }
        System.out.println("[BOT] Бот остановлен");
    }

    /**
     * Запускает бота в режиме long polling
     */
    public void start() {
        try {
            System.out.println("[BOT] Запуск бота...");

            // Создаем экземпляр приложения
            botsApplication = new TelegramBotsLongPollingApplication();

            // Регистрируем бота
            botsApplication.registerBot(botToken, this);
            System.out.println("[BOT] Бот зарегистрирован в LongPollingApplication");

            // Регистрируем команды
            registerBotCommands();
            System.out.println("[BOT] Бот успешно запущен и готов к работе");

            // Добавляем хук
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[BOT] Получен сигнал завершения работы...");
                stop();
            }));

            System.out.println("[BOT] Бот работает в фоновом режиме");

        } catch (Exception e) {
            System.err.println("[BOT] Фатальная ошибка запуска бота");
            e.printStackTrace(System.err);
        }
    }
}