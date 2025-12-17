package org.example;

import org.example.DataMessage.Content;
import org.example.DataMessage.KeyboardService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

/**
 * Телеграм бот для проведения викторин.
 * Обрабатывает входящие сообщения и callback-запросы, управляет состоянием викторины.
 * Реализует интерфейс LongPollingSingleThreadUpdateConsumer для получения обновлений от Telegram.
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private Producer producer;
    private final TelegramClient telegramClient;
    private final String botToken;
    private TelegramBotsLongPollingApplication botsApplication;
    private final Map<String, InlineKeyboardMarkup> keyboardCache = new HashMap<>();

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
     * @param producer объект продюсера для обработки бизнес-логики
     */
    public void setProducer(Producer producer) {
        this.producer = producer;
        System.out.println("[BOT] Установлен Producer");
        initializeKeyboards();
    }

    /**
     * Регистрирует команды бота в меню Telegram.
     */
    private void registerBotCommands() {
        try {
            System.out.println("[BOT] Регистрация команд бота...");
            boolean success = telegramClient.execute(
                    SetMyCommands.builder()
                            .commands(Arrays.asList(
                                    new BotCommand("start", "Запустить бота"),
                                    new BotCommand("menu", "Меню"),
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
     * Отправляет сообщение через Telegram API.
     *
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
     * Получает обновления из телеграма и передает в Producer через Content.
     * Также проверяет очередь сообщений от таймаутов.
     *
     * @param update объект обновления от Telegram API
     */
    @Override
    public void consume(Update update) {
        try {
            Content[] responseContents = null;
            String chatId;

            System.out.println("[BOT] НОВОЕ ОБНОВЛЕНИЕ");

            // СНАЧАЛА проверяем очередь сообщений от таймаутов
            checkAndSendTimeoutMessages();

            if (update.hasCallbackQuery()) {
                // Обработка callback от кнопок
                String callbackData = update.getCallbackQuery().getData();
                chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());

                System.out.println("[BOT] Callback от " + chatId + ": " + callbackData);

                Content content = new Content(false, chatId, callbackData, callbackData, null);
                responseContents = producer.produce(content);

            } else if (update.hasMessage() && update.getMessage().hasText()) {
                // Обработка текстового сообщения
                String messageText = update.getMessage().getText();
                chatId = String.valueOf(update.getMessage().getChatId());

                System.out.println("[BOT] Сообщение от " + chatId + ": " + messageText);

                Content content = new Content(false, chatId, messageText);
                responseContents = producer.produce(content);
            }

            if (responseContents != null && responseContents.length > 0) {
                System.out.println("[BOT] Producer вернул " + responseContents.length + " сообщений для отправки");

                SendMessage[] messages = convertToSendMessages(responseContents);
                for (SendMessage message : messages) {
                    if (message != null) {
                        sendMessage(message);
                    }
                }
            } else {
                System.out.println("[BOT] Нет ответных сообщений для отправки");
            }

            System.out.println("[BOT] ОБРАБОТКА ЗАВЕРШЕНА\n");

        } catch (Exception e) {
            System.err.println("[BOT] Критическая ошибка обработки обновления");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Проверяет и отправляет сообщения от таймаутов из очереди.
     * Этот метод вызывается при каждом обновлении для обеспечения
     * своевременной отправки сообщений о таймаутах.
     */
    private void checkAndSendTimeoutMessages() {
        if (producer == null) {
            System.out.println("[BOT] Producer не установлен, пропускаем проверку очереди");
            return;
        }

        try {
            if (producer.hasQueuedMessages()) {
                System.out.println("[BOT] Проверка очереди сообщений от таймаутов...");

                Content[] timeoutMessages = producer.getQueuedMessages();

                if (timeoutMessages != null && timeoutMessages.length > 0) {
                    System.out.println("[BOT] Найдено " + timeoutMessages.length + " сообщений от таймаутов в очереди");

                    SendMessage[] timeoutSendMessages = convertToSendMessages(timeoutMessages);
                    int sentCount = 0;

                    for (SendMessage message : timeoutSendMessages) {
                        if (message != null) {
                            sendMessage(message);
                            sentCount++;
                        }
                    }

                    System.out.println("[BOT] Отправлено " + sentCount + " сообщений от таймаутов");
                } else {
                    System.out.println("[BOT] Очередь сообщений пуста");
                }
            }
        } catch (Exception e) {
            System.err.println("[BOT] Ошибка при проверке очереди сообщений от таймаутов");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Конвертирует объект Content в SendMessage для отправки через Telegram API.
     *
     * @param content объект Content для конвертации
     * @return объект SendMessage или null если конвертация невозможна
     */
    private SendMessage convertToSendMessage(Content content) {
        if (!content.isOut()) {
            return null;
        }

        System.out.println("[BOT] Конвертация Content в SendMessage");
        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                .chatId(content.getChatId());

        if (content.getText() != null && !content.getText().isEmpty()) {
            builder.text(content.getText());
        } else {
            System.out.println("[BOT] Нет текста для отправки");
            return null;
        }

        // Добавляем InlineKeyboard если указан тип клавиатуры
        if (content.hasKeyboard()) {
            String keyboardType = content.getKeyboardType();
            System.out.println("[BOT] Добавление Inline клавиатуры типа: " + keyboardType);

            InlineKeyboardMarkup keyboard = keyboardCache.get(keyboardType);
            if (keyboard != null) {
                builder.replyMarkup(keyboard);
            } else {
                System.out.println("[BOT] Клавиатура типа '" + keyboardType + "' не найдена в кэше");
            }
        }

        return builder.build();
    }

    /**
     * Создание клавиатуры из одного или двух уровней кнопок.
     * Кнопки располагаются в зависимости от их количества на каждом уровне.
     *
     * @param firstLevelButtons карта кнопок первого уровня (текст -> callbackData)
     * @param secondLevelButtons карта кнопок второго уровня (текст -> callbackData)
     * @return объект InlineKeyboardMarkup с настроенной клавиатурой
     */
    private InlineKeyboardMarkup createUniversalKeyboard(Map<String, String> firstLevelButtons,
                                                         Map<String, String> secondLevelButtons) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        // Добавляем кнопки первого уровня - все в одном ряду
        InlineKeyboardRow firstLevelRow = new InlineKeyboardRow();
        for (Map.Entry<String, String> entry : firstLevelButtons.entrySet()) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(entry.getKey())
                    .callbackData(entry.getValue())
                    .build();
            firstLevelRow.add(button);
        }
        keyboard.add(firstLevelRow);

        // Добавляем разделитель и кнопки второго уровня (если они есть)
        if (!secondLevelButtons.isEmpty()) {
            keyboard.add(new InlineKeyboardRow()); // пустая строка как разделитель

            // Добавляем кнопки второго уровня - все в одном ряду
            InlineKeyboardRow secondLevelRow = new InlineKeyboardRow();
            for (Map.Entry<String, String> entry : secondLevelButtons.entrySet()) {
                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(entry.getKey())
                        .callbackData(entry.getValue())
                        .build();
                secondLevelRow.add(button);
            }
            keyboard.add(secondLevelRow);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    /**
     * Конвертирует массив Content в массив SendMessage.
     *
     * @param contents массив объектов Content для конвертации
     * @return массив объектов SendMessage
     */
    private SendMessage[] convertToSendMessages(Content[] contents) {
        if (contents == null) {
            return new SendMessage[0];
        }

        List<SendMessage> messages = new ArrayList<>();
        for (Content content : contents) {
            SendMessage message = convertToSendMessage(content);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages.toArray(new SendMessage[0]);
    }

    /**
     * Инициализирует кэш клавиатур для быстрого доступа.
     */
    private void initializeKeyboards() {
        if (producer == null) {
            System.err.println("[BOT] Producer не установлен, невозможно инициализировать клавиатуры");
            return;
        }

        KeyboardService keyboardService = producer.getKeyboardService();
        if (keyboardService == null) {
            System.err.println("[BOT] KeyboardService не доступен");
            return;
        }

        try {
            // Основные кнопки меню
            keyboardCache.put("menu", createUniversalKeyboard(
                    keyboardService.getMainButtons(),
                    new HashMap<>()));

            // Кнопки ответов на вопросы
            keyboardCache.put("test_answer", createUniversalKeyboard(
                    keyboardService.getTestAnswerButtons(),
                    keyboardService.getForwardsAndBackwardsQuizButtons()));

            // Кнопки выбора викторины
            keyboardCache.put("choice_quiz", createUniversalKeyboard(
                    keyboardService.getChoiceQuiz(),
                    keyboardService.getForwardsAndBackwardsTopicButtons()));

            // Пред финальные кнопки викторины
            keyboardCache.put("final_quiz", createUniversalKeyboard(
                    keyboardService.getFinalQuizButton(),
                    new HashMap<>()));

            // Кнопка на лидерборде висит после теста
            keyboardCache.put("go_menu", createUniversalKeyboard(
                    keyboardService.getGoMenu(),
                    new HashMap<>()));

            // Набор кнопок для выбора режима
            keyboardCache.put("mode_selection", createUniversalKeyboard(
                    keyboardService.getModeSelection(),
                    new HashMap<>()));

            System.out.println("[BOT] Кэш клавиатур инициализирован, создано " + keyboardCache.size() + " клавиатур");

            for (String key : keyboardCache.keySet()) {
                InlineKeyboardMarkup keyboard = keyboardCache.get(key);
                int rowCount = keyboard.getKeyboard().size();
                System.out.println("[BOT] Создана клавиатура: " + key + " (рядов: " + rowCount + ")");
            }

        } catch (Exception e) {
            System.err.println("[BOT] Ошибка инициализации клавиатур");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Периодически проверяет очередь сообщений от таймаутов.
     * Этот метод может быть запущен в отдельном потоке для гарантированной
     * отправки сообщений о таймаутах, даже если пользователь ничего не делает.
     */
    public void startTimeoutChecker() {
        System.out.println("[BOT] Запуск периодической проверки очереди таймаутов...");

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (producer != null && producer.hasQueuedMessages()) {
                        System.out.println("[BOT] [TIMER] Периодическая проверка очереди...");

                        Content[] timeoutMessages = producer.getQueuedMessages();
                        if (timeoutMessages != null && timeoutMessages.length > 0) {
                            System.out.println("[BOT] [TIMER] Найдено " + timeoutMessages.length + " сообщений в очереди");

                            // Отправляем сообщения
                            SendMessage[] messages = convertToSendMessages(timeoutMessages);
                            for (SendMessage message : messages) {
                                if (message != null) {
                                    sendMessage(message);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[BOT] [TIMER] Ошибка в периодической проверке очереди");
                    e.printStackTrace(System.err);
                }
            }
        }, 30000, 30000); // Проверяем каждые 30 секунд

        System.out.println("[BOT] Периодическая проверка очереди запущена (интервал: 30 сек)");
    }

    /**
     * Останавливает бота и освобождает ресурсы.
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
     * Запускает бота в режиме long polling.
     */
    public void start() {
        try {
            System.out.println("[BOT] Запуск бота...");
            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, this);
            System.out.println("[BOT] Бот зарегистрирован в LongPollingApplication");

            registerBotCommands();

            startTimeoutChecker();

            System.out.println("[BOT] Бот успешно запущен и готов к работе");

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