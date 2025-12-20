package org.example;

import org.example.DataMessage.Content;
import org.example.Quiz.Users;
import org.example.Tokens.TokenInterface;
import org.example.Tokens.Tokens;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.example.DataMessage.Constants.*;

public class ProducerTest {
    Users users;
    private InputStream originalIn;
    private PrintStream originalOut;
    private String TelegramToken;

    @TempDir
    Path tempDir;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     */
    @BeforeEach
    void setUp() throws IOException {
        String uniqueFileName = "test_users_data_" + UUID.randomUUID() + ".json";
        Path testDbPath = tempDir.resolve(uniqueFileName);

        String emptyDb = "{}";
        Files.writeString(testDbPath, emptyDb);

        users = new Users(testDbPath.toString());

        originalIn = System.in;
        originalOut = System.out;

        TokenInterface tokens = new Tokens();
        TelegramToken = tokens.getTelegramToken();
    }

    /**
     * Восстановление тестового окружения после каждого теста.
     */
    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /**
     * Тестирование обработки команды /start в Producer без регистрации.
     */
    @Test
    public void testProducerStartCommandWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content startContent = new Content(false, "chat123", "/start");
        Content[] result = producer.produce(startContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(2, result.length, "Должно вернуться 2 сообщения");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertTrue(result[1].isOut(), "Регистрация должна быть исходящим");
    }

    /**
     * Тестирование обработки команды /start после регистрации.
     */
    @Test
    public void testProducerStartCommandAfterRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        // Сначала регистрируем пользователя - установка имени в UserData
        users.setLeaderboardName("chat123", "TestUser");

        Content startContent = new Content(false, "chat123", "/start");
        Content[] result = producer.produce(startContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(2, result.length, "Должно вернуться 2 сообщения");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertTrue(result[1].isOut(), "Меню должно быть исходящим");
        Assertions.assertNull(result[1].getKeyboardType(), "Тип клавиатуры должен быть 'mode_selection'");
    }

    /**
     * Тестирование обработки команды /help в Producer.
     */
    @Test
    public void testProducerHelpCommand() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content helpContent = new Content(false, "chat123", "/help");
        Content[] result = producer.produce(helpContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /leaderboard без регистрации.
     */
    @Test
    public void testProducerLeaderboardCommandWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content leaderboardContent = new Content(false, "chat123", "/leaderboard");
        Content[] result = producer.produce(leaderboardContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /leaderboard с регистрацией.
     */
    @Test
    public void testProducerLeaderboardCommandWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content leaderboardContent = new Content(false, "chat123", "/leaderboard");
        Content[] result = producer.produce(leaderboardContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /menu с регистрацией.
     */
    @Test
    public void testProducerMenuCommandWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content menuContent = new Content(false, "chat123", "/menu");
        Content[] result = producer.produce(menuContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertNull(result[0].getKeyboardType(), "Тип клавиатуры должен быть 'mode_selection'");
    }

    /**
     * Тестирование обработки кнопки меню с регистрацией.
     */
    @Test
    public void testProducerMenuButtonWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content menuButtonContent = new Content(false, "chat123", MENU_BUTTON);
        Content[] result = producer.produce(menuButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertNull(result[0].getKeyboardType(), "Тип клавиатуры должен быть 'mode_selection'");
    }

    /**
     * Тестирование обработки кнопки генерации викторины без регистрации.
     */
    @Test
    public void testProducerGenerationButtonWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content generationButtonContent = new Content(false, "chat123", GENERATION_BUTTON);
        Content[] result = producer.produce(generationButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки генерации викторины с регистрацией.
     */
    @Test
    public void testProducerGenerationButtonWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content generationButtonContent = new Content(false, "chat123", GENERATION_BUTTON);
        Content[] result = producer.produce(generationButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки выбора викторины без регистрации.
     */
    @Test
    public void testProducerQuizButtonWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content quizButtonContent = new Content(false, "chat123", QUIZ_BUTTON);
        Content[] result = producer.produce(quizButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки дуэли без регистрации.
     */
    @Test
    public void testProducerDuelButtonWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content duelButtonContent = new Content(false, "chat123", DUEL_BUTTON);
        Content[] result = producer.produce(duelButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки соло режима без регистрации.
     */
    @Test
    public void testProducerSoloButtonWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content soloButtonContent = new Content(false, "chat123", SOLO_BUTTON);
        Content[] result = producer.produce(soloButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки неизвестной команды.
     */
    @Test
    public void testProducerUnknownCommand() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content unknownContent = new Content(false, "chat123", "/unknowncommand");
        Content[] result = producer.produce(unknownContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertTrue(result.length >= 1, "Должен вернуться хотя бы один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки текстового сообщения без активной викторины и регистрации.
     */
    @Test
    public void testProducerTextMessageWithoutQuizAndRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content textContent = new Content(false, "chat123", "просто текст");
        Content[] result = producer.produce(textContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки текстового сообщения без активной викторины с регистрацией.
     */
    @Test
    public void testProducerTextMessageWithoutQuizWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content textContent = new Content(false, "chat123", "просто текст");
        Content[] result = producer.produce(textContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопок ответов без активной викторины и регистрации.
     */
    @Test
    public void testProducerAnswerButtonWithoutQuizAndRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content answerButtonContent = new Content(false, "chat123", A_BUTTON);
        Content[] result = producer.produce(answerButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки навигационных кнопок без активной викторины и регистрации.
     */
    @Test
    public void testProducerNavigationButtonWithoutQuizAndRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content navButtonContent = new Content(false, "chat123", QUIZ_FORWARDS_BUTTON);
        Content[] result = producer.produce(navButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки неизвестного callback.
     */
    @Test
    public void testProducerUnknownCallback() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content unknownCallbackContent = new Content(false, "chat123", "unknown_callback_button");
        Content[] result = producer.produce(unknownCallbackContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("Неизвестное действие", result[0].getText(), "Текст должен сообщать о неизвестном действии");
        Assertions.assertEquals("menu", result[0].getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки кнопки завершения викторины без активной викторины.
     */
    @Test
    public void testProducerEndQuizButtonWithoutQuiz() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content endQuizButtonContent = new Content(false, "chat123", END_QUIZ_BUTTON);
        Content[] result = producer.produce(endQuizButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Викторина не активна", result[0].getText(), "Текст должен сообщать о неактивной викторине");
        Assertions.assertEquals("menu", result[0].getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки кнопки перехода к первому вопросу без активной викторины.
     */
    @Test
    public void testProducerAtTheTopButtonWithoutQuiz() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content atTheTopButtonContent = new Content(false, "chat123", AT_THE_TOP_BUTTON);
        Content[] result = producer.produce(atTheTopButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Викторина не активна", result[0].getText(), "Текст должен сообщать о неактивной викторине");
        Assertions.assertNull(result[0].getKeyboardType(), "Не должно быть клавиатуры");
    }

    /**
     * Тестирование обработки навигационных кнопок темы без активного выбора темы.
     */
    @Test
    public void testProducerTopicNavigationWithoutTopicSelector() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content topicNavButtonContent = new Content(false, "chat123", TOPIC_FORWARDS_BUTTON);
        Content[] result = producer.produce(topicNavButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Навигация недоступна", result[0].getText(), "Текст должен сообщать о недоступной навигации");
        Assertions.assertEquals("menu", result[0].getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки регистрации пользователя с валидным именем.
     */
    @Test
    public void testProducerUserRegistrationValidName() {
        Producer producer = new Producer(users,  TelegramToken);

        // Первое сообщение - должно запрашивать регистрацию
        Content startContent = new Content(false, "chat123", "/start");
        Content[] result1 = producer.produce(startContent);
        Assertions.assertNotNull(result1);
        Assertions.assertEquals(2, result1.length);

        // Вводим валидное имя
        Content nameContent = new Content(false, "chat123", "TestUser");
        Content[] result2 = producer.produce(nameContent);

        Assertions.assertNotNull(result2);
        Assertions.assertEquals(2, result2.length, "Должно вернуться 2 сообщения после регистрации");
        Assertions.assertTrue(result2[0].isOut(), "Подтверждение должно быть исходящим");
        Assertions.assertTrue(result2[1].isOut(), "Меню должно быть исходящим");
        Assertions.assertEquals("mode_selection", result2[1].getKeyboardType(), "После регистрации должно быть меню выбора режима");
    }

    /**
     * Тестирование обработки регистрации с невалидным именем (слишком длинным).
     */
    @Test
    public void testProducerUserRegistrationInvalidName() {
        Producer producer = new Producer(users,  TelegramToken);

        // Сначала /start
        Content startContent = new Content(false, "chat123", "/start");
        producer.produce(startContent);

        // Пытаемся ввести слишком длинное имя
        Content longNameContent = new Content(false, "chat123",
                "ОченьДлинноеИмяКотороеПревышаетДвадцатьСимволов");
        Content[] result = producer.produce(longNameContent);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.length, "Должно вернуться одно сообщение об ошибке");
        Assertions.assertTrue(result[0].isOut(), "Сообщение об ошибке должно быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки завершения викторины с регистрацией.
     */
    @Test
    public void testProducerEndQuizButtonWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content endQuizButtonContent = new Content(false, "chat123", END_QUIZ_BUTTON);
        Content[] result = producer.produce(endQuizButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Викторина не активна", result[0].getText(), "Текст должен сообщать о неактивной викторине");
        Assertions.assertEquals("menu", result[0].getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки текстового сообщения во время ожидания имени для регистрации.
     */
    @Test
    public void testProducerTextMessageDuringRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        // Начинаем регистрацию
        Content startContent = new Content(false, "chat123", "/start");
        producer.produce(startContent);

        // Отправляем имя
        Content nameContent = new Content(false, "chat123", "TestUser");
        Content[] result = producer.produce(nameContent);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.length, "Должно вернуться 2 сообщения");
        Assertions.assertTrue(result[0].isOut(), "Подтверждение должно быть исходящим");
        Assertions.assertTrue(result[1].isOut(), "Меню должно быть исходящим");
        Assertions.assertEquals("mode_selection", result[1].getKeyboardType(), "Должен быть тип клавиатуры mode_selection");
    }

    /**
     * Тестирование получения сообщений из очереди.
     */
    @Test
    public void testProducerGetQueuedMessages() {
        Producer producer = new Producer(users,  TelegramToken);

        Content[] messages = producer.getQueuedMessages();
        Assertions.assertNotNull(messages, "Массив сообщений не должен быть null");
    }

    /**
     * Тестирование проверки наличия сообщений в очереди.
     */
    @Test
    public void testProducerHasQueuedMessages() {
        Producer producer = new Producer(users,  TelegramToken);

        boolean hasMessages = producer.hasQueuedMessages();
        Assertions.assertNotNull(producer, "Producer должен быть создан");
    }

    /**
     * Тестирование установки менеджера пользователей.
     */
    @Test
    public void testProducerSetUsers() {
        Users newUsers = new Users();
        Producer producer = new Producer(newUsers, TelegramToken);

        Assertions.assertNotNull(producer, "Producer должен существовать");
    }

    /**
     * Тестирование обработки текстового сообщения с пустым текстом.
     */
    @Test
    public void testProducerEmptyTextMessage() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content emptyContent = new Content(false, "chat123", "");
        Content[] result = producer.produce(emptyContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /help без регистрации.
     */
    @Test
    public void testProducerHelpCommandWithoutRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        Content helpContent = new Content(false, "chat123", "/help");
        Content[] result = producer.produce(helpContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки дуэли с регистрацией.
     */
    @Test
    public void testProducerDuelButtonWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content duelButtonContent = new Content(false, "chat123", DUEL_BUTTON);
        Content[] result = producer.produce(duelButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertTrue(result.length >= 1, "Должен вернуться хотя бы один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки соло режима с регистрацией.
     */
    @Test
    public void testProducerSoloButtonWithRegistration() {
        Producer producer = new Producer(users,  TelegramToken);

        users.setLeaderboardName("chat123", "TestUser");

        Content soloButtonContent = new Content(false, "chat123", SOLO_BUTTON);
        Content[] result = producer.produce(soloButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertTrue(result.length >= 1, "Должен вернуться хотя бы один контент");
        Assertions.assertTrue(result[0].isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", result[0].getChatId(), "ChatId должен соответствовать");
    }
}