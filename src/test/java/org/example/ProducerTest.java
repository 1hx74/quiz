package org.example;

import org.example.DataMessage.Content;
import org.example.Quiz.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.InputStream;
import java.io.PrintStream;

public class ProducerTest {
    Users users;
    private InputStream originalIn;
    private PrintStream originalOut;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        users = new Users();
        originalIn = System.in;
        originalOut = System.out;
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
     * Тестирование обработки команды /start в Producer.
     */
    @Test
    public void testProducerStartCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content startContent = new Content(false, "chat123", "/start");
        Content[] result = producer.produce(startContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(2, result.length, "Должно вернуться 2 сообщения");

        Content welcomeResponse = result[0];
        Content menuResponse = result[1];

        Assertions.assertTrue(welcomeResponse.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", welcomeResponse.getChatId(), "ChatId должен соответствовать");

        Assertions.assertTrue(menuResponse.isOut(), "Меню должно быть исходящим");
        Assertions.assertEquals("menu", menuResponse.getKeyboardType(), "Тип клавиатуры должен быть 'menu'");
    }

    /**
     * Тестирование обработки команды /help в Producer.
     */
    @Test
    public void testProducerHelpCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content helpContent = new Content(false, "chat123", "/help");
        Content[] result = producer.produce(helpContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /leaderboard в Producer.
     */
    @Test
    public void testProducerLeaderboardCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content leaderboardContent = new Content(false, "chat123", "/leaderboard");
        Content[] result = producer.produce(leaderboardContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки команды /menu в Producer.
     */
    @Test
    public void testProducerMenuCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content menuContent = new Content(false, "chat123", "/menu");
        Content[] result = producer.produce(menuContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Тип клавиатуры должен быть 'menu'");
    }

    /**
     * Тестирование обработки кнопки меню в Producer.
     */
    @Test
    public void testProducerMenuButton() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content menuButtonContent = new Content(false, "chat123", "menu_button");
        Content[] result = producer.produce(menuButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Тип клавиатуры должен быть 'menu'");
    }

    /**
     * Тестирование обработки кнопки генерации викторины.
     */
    @Test
    public void testProducerGenerationButton() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content generationButtonContent = new Content(false, "chat123", "generation_button");
        Content[] result = producer.produce(generationButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопки выбора викторины.
     */
    @Test
    public void testProducerQuizButton() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content quizButtonContent = new Content(false, "chat123", "quiz_button");
        Content[] result = producer.produce(quizButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");

        // Может вернуть ошибку если choose.json не доступен в тестовой среде
        if (result.length > 0) {
            Content response = result[0];
            Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
            Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        }
    }

    /**
     * Тестирование обработки неизвестной команды в Producer.
     */
    @Test
    public void testProducerUnknownCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content unknownContent = new Content(false, "chat123", "/unknowncommand");
        Content[] result = producer.produce(unknownContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertTrue(result.length >= 1, "Должен вернуться хотя бы один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки текстового сообщения без активной викторины.
     */
    @Test
    public void testProducerTextMessageWithoutQuiz() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content textContent = new Content(false, "chat123", "просто текст");
        Content[] result = producer.produce(textContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки кнопок ответов без активной викторины.
     */
    @Test
    public void testProducerAnswerButtonWithoutQuiz() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content answerButtonContent = new Content(false, "chat123", "A_button");
        Content[] result = producer.produce(answerButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки навигационных кнопок без активной викторины.
     */
    @Test
    public void testProducerNavigationButtonWithoutQuiz() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content navButtonContent = new Content(false, "chat123", "quiz_forwards_button");
        Content[] result = producer.produce(navButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
    }

    /**
     * Тестирование обработки неизвестного callback.
     */
    @Test
    public void testProducerUnknownCallback() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content unknownCallbackContent = new Content(false, "chat123", "unknown_callback_button");
        Content[] result = producer.produce(unknownCallbackContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("Неизвестное действие", response.getText(), "Текст должен сообщать о неизвестном действии");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки кнопки завершения викторины без активной викторины.
     */
    @Test
    public void testProducerEndQuizButtonWithoutQuiz() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content endQuizButtonContent = new Content(false, "chat123", "end_quiz_button");
        Content[] result = producer.produce(endQuizButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Викторина не активна", response.getText(), "Текст должен сообщать о неактивной викторине");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки кнопки перехода к первому вопросу без активной викторины.
     */
    @Test
    public void testProducerAtTheTopButtonWithoutQuiz() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content atTheTopButtonContent = new Content(false, "chat123", "at_the_top_button");
        Content[] result = producer.produce(atTheTopButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Викторина не активна", response.getText(), "Текст должен сообщать о неактивной викторине");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Должна быть клавиатура меню");
    }

    /**
     * Тестирование обработки навигационных кнопок темы без активного выбора темы.
     */
    @Test
    public void testProducerTopicNavigationWithoutTopicSelector() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content topicNavButtonContent = new Content(false, "chat123", "topic_forwards_button");
        Content[] result = producer.produce(topicNavButtonContent);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        Assertions.assertTrue(response.isOut(), "Ответ должен быть исходящим");
        Assertions.assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        Assertions.assertEquals("❌ Навигация недоступна", response.getText(), "Текст должен сообщать о недоступной навигации");
        Assertions.assertEquals("menu", response.getKeyboardType(), "Должна быть клавиатура меню");
    }
}