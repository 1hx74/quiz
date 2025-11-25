package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProducerTest {
    Users users;
    private InputStream originalIn;     // для изоляции теста
    private PrintStream originalOut;    // работает как отладка

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Сохраняет оригинальные System.in и System.out для последующего восстановления.
     */
    @BeforeEach
    void setUp() {
        users = new Users();
        originalIn = System.in;
        originalOut = System.out;
    }

    /**
     * Восстановление тестового окружения после каждого теста.
     * Восстанавливает оригинальные System.in и System.out.
     */
    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /**
     * Тестирование создания главного меню в Producer.
     * Проверяет корректность кнопок и текста.
     */
    @Test
    public void testProducerCreateMenuContent() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content menuContent = producer.createMenuContent("chat123", "Добро пожаловать в викторину!");

        assertTrue(menuContent.isOut(), "Меню должно быть исходящим сообщением");
        assertEquals("chat123", menuContent.getChatId(), "ChatId должен соответствовать");
        assertEquals("Добро пожаловать в викторину!", menuContent.getText(), "Текст должен соответствовать");

        String[] options = menuContent.getOptions();
        assertNotNull(options, "Опции не должны быть null");
        assertEquals(3, options.length, "Должно быть 3 кнопки в меню");
        assertEquals("Начать викторину", options[0], "Первая кнопка должна быть 'Начать викторину'");
        assertEquals("Выбрать тему", options[1], "Вторая кнопка должна быть 'Выбрать тему'");
        assertEquals("Помощь", options[2], "Третья кнопка должна быть 'Помощь'");
    }

    /**
     * Тестирование создания текстового контента без кнопок.
     */
    @Test
    public void testProducerCreateTextContent() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content textContent = producer.createTextContent("chat456", "Простое текстовое сообщение");

        assertTrue(textContent.isOut(), "Контент должен быть исходящим");
        assertEquals("chat456", textContent.getChatId(), "ChatId должен соответствовать");
        assertEquals("Простое текстовое сообщение", textContent.getText(), "Текст должен соответствовать");
        assertNull(textContent.getOptions(), "Текстовый контент не должен иметь кнопок");
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

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        assertTrue(response.isOut(), "Ответ должен быть исходящим");
        assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        assertTrue(response.getText().contains("Добро пожаловать в викторину"), "Текст должен содержать приветствие");

        String[] options = response.getOptions();
        assertNotNull(options, "Опции не должны быть null");
        assertEquals(3, options.length, "Должно быть 3 кнопки");
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

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        assertTrue(response.isOut(), "Ответ должен быть исходящим");
        assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        assertTrue(response.getText().contains("Помощь по викторине"), "Текст должен содержать справку");
        assertTrue(response.getText().contains("Как играть"), "Текст должен содержать инструкции");
        assertNull(response.getOptions(), "Справка не должна иметь кнопок");
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

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        assertTrue(response.isOut(), "Ответ должен быть исходящим");
        assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        assertTrue(response.getText().contains("Лидерборд"), "Текст должен содержать лидерборд");
        assertNull(response.getOptions(), "Лидерборд не должен иметь кнопок");
    }

    /**
     * Тестирование обработки кнопки "Помощь" в Producer.
     */
    @Test
    public void testProducerHelpButton() {
        Producer producer = new Producer();
        producer.setUsers(users);

        Content helpButtonContent = new Content(false, "chat123", "Помощь");
        Content[] result = producer.produce(helpButtonContent);

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.length, "Должен вернуться один контент");

        Content response = result[0];
        assertTrue(response.isOut(), "Ответ должен быть исходящим");
        assertEquals("chat123", response.getChatId(), "ChatId должен соответствовать");
        assertTrue(response.getText().contains("Помощь по викторине"), "Текст должен содержать справку");
        assertNull(response.getOptions(), "Справка не должна иметь кнопок");
    }

    /**
     * Тестирование обработки неизвестной команды в Producer.
     */
    @Test
    public void testProducerUnknownCommand() {
        Producer producer = new Producer();
        producer.setUsers(users);

        // Сначала создаем пользователя с активной викториной
        Content startContent = new Content(false, "chat123", "Начать викторину");
        producer.produce(startContent);

        // Тестируем неизвестную команду
        Content unknownContent = new Content(false, "chat123", "неизвестная команда");
        Content[] result = producer.produce(unknownContent);

        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.length >= 1, "Должен вернуться хотя бы один контент");
    }
}
