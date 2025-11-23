package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;    // аннотации для тествого окружения, этио типо перед каждым тестом
import org.junit.jupiter.api.AfterEach;     // после каждого
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки функциональности класса Quiz.
 * Содержит тесты для всех основных методов и сценариев использования.
 */
class QuizTest {
    Users users;
    private InputStream originalIn;     // для изоляции теста
    private PrintStream originalOut;    // работает как отладка

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Сохраняет оригинальные System.in и System.out для последующего восстановления.
     */
    @BeforeEach
    void setUp() {
        users = new Users();    // Добавлена инициализация
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
     * Тестирование метода getIdxAnswer с различными вариантами ввода.
     * Проверяет корректность преобразования буквенных и числовых ответов в индексы.
     */
    @Test
    public void testMethodGetIdxAnswer() {
        String input = "A\nB\nhelp\n3\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Quiz quiz = new Quiz();

        assertEquals(0, quiz.getIdxAnswer(), "Буква A должна преобразовываться в индекс 0");
        assertEquals(1, quiz.getIdxAnswer(), "Буква B должна преобразовываться в индекс 1");
        assertEquals(-1, quiz.getIdxAnswer(), "Слово help должно возвращать -1");
        assertEquals(2, quiz.getIdxAnswer(), "Цифра 3 должна преобразовываться в индекс 2");
    }

    /**
     * Тестирование запуска викторины в обычном режиме.
     * Проверяет корректность подсчета очков и форматирования результатов.
     */
    @Test
    public void testPrivateMethodQuizRun() {
        Memory memory = new Memory();

        // Создаем вопросы с известными правильными ответами
        // Правильные ответы: C(2), A(0), B(1) - ожидаем счет 2 из 3
        Data q1 = new Data("question1", new String[] { "answer1", "answer2", "answer3", "answer4" }, 2); // Правильный: C
        Data q2 = new Data("question2", new String[] { "answer1", "answer2", "answer3", "answer4" }, 0); // Правильный: A
        Data q3 = new Data("question3", new String[] { "answer1", "answer2", "answer3", "answer4" }, 1); // Правильный: B

        Data[] data = new Data[] { q1, q2, q3 };
        memory.setData(data);

        // Вводим ответы: C(правильно), B(неправильно), B(правильно) = 2 правильных из 3
        String answer = "C\nB\nB\n";    // Ответы: C(2), B(1), B(1)
        InputStream in = new ByteArrayInputStream(answer.getBytes());
        System.setIn(in);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        Quiz quiz = new Quiz(memory, false);
        String result = quiz.run(memory, false);

        // Проверяем что счет 2 из 3
        assertTrue(result.contains("Ваш счёт: 2 из 3"), "Результат должен содержать счет 2 из 3. Фактический результат: " + result);
    }

    /**
     * Тестирование конструктора Quiz с параметрами.
     * Проверяет корректность инициализации полей объекта.
     */
    @Test
    public void testQuizConstructorWithParameters() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, true);

        assertNotNull(quiz, "Объект Quiz не должен быть null");
        assertTrue(quiz.isChooseMode(), "Режим должен быть 'выбор темы'");
        assertEquals(0, quiz.getScore(), "Начальный счет должен быть 0");
    }

    /**
     * Тестирование обработки правильных и неправильных ответов.
     * Проверяет корректность увеличения счета и форматирования сообщений.
     */
    @Test
    public void testQuizProcessAnswer() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, false);

        // Тест правильного ответа
        String result = quiz.processAnswer(1);
        assertEquals("Верно!", result, "Правильный ответ должен возвращать 'Верно!'");
        assertEquals(1, quiz.getScore(), "Счет должен увеличиться на 1 после правильного ответа");

        // Тест неправильного ответа
        result = quiz.processAnswer(0);

        // Проверяем что это не сообщение о правильном ответе
        assertNotEquals("Верно!", result, "Неправильный ответ не должен возвращать 'Верно!'");

        // Проверяем что счет не изменился
        assertEquals(1, quiz.getScore(), "Счет не должен изменяться после неправильного ответа");

        // Проверяем что возвращается какое-то сообщение (не пустая строка)
        assertFalse(result.isEmpty(), "Должно возвращаться сообщение об ошибке");
    }

    /**
     * Тестирование определения завершения викторины.
     * Проверяет корректность работы метода isFinished.
     */
    @Test
    public void testQuizIsFinished() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Вопрос 1", new String[]{"Ответ 1", "Ответ 2", "Ответ 3", "Ответ 4"}, 0),
                new Data("Вопрос 2", new String[]{"Ответ 1", "Ответ 2", "Ответ 3", "Ответ 4"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, false);

        assertFalse(quiz.isFinished(), "Викторина не должна быть завершена в начале");
        quiz.processAnswer(0);
        assertFalse(quiz.isFinished(), "Викторина не должна быть завершена после первого ответа");
        quiz.processAnswer(1);
        assertTrue(quiz.isFinished(), "Викторина должна быть завершена после всех ответов");
    }

    /**
     * Тестирование форматирования текущего вопроса.
     * Проверяет корректность отображения вопроса и вариантов ответов.
     */
    @Test
    public void testQuizGetCurrentQuestion() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Тестовый вопрос", new String[]{"Ответ А", "Ответ Б", "Ответ В", "Ответ Г"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, false);

        String question = quiz.getCurrentQuestion();

        // Проверяем ключевые элементы через разбиение на строки
        String[] lines = question.split("\n");

        assertEquals("Вопрос №1) Тестовый вопрос", lines[0],
                "Первая строка должна содержать номер и текст вопроса");

        assertEquals("A) Ответ А", lines[1],
                "Вторая строка должна содержать вариант A");

        assertEquals("B) Ответ Б", lines[2],
                "Третья строка должна содержать вариант B");

        assertEquals("C) Ответ В", lines[3],
                "Четвертая строка должна содержать вариант C");

        assertEquals("D) Ответ Г", lines[4],
                "Пятая строка должна содержать вариант D");

        assertTrue(question.endsWith("Ваш ответ: "), "Вопрос должен заканчиваться приглашением к ответу");
    }

    /**
     * Тестирование форматирования вопроса в режиме выбора темы.
     */
    @Test
    public void testQuizGetCurrentQuestionInChooseMode() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Выберите тему", new String[]{"История", "География", "Наука"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, true); // Режим выбора темы

        String question = quiz.getCurrentQuestion();
        assertTrue(question.contains("Выберите тему"), "Вопрос должен содержать текст выбора темы");
        assertTrue(question.contains("История"), "Должна быть тема История");
        assertTrue(question.contains("География"), "Должна быть тема География");
        assertTrue(question.contains("Наука"), "Должна быть тема Наука");
        assertTrue(question.contains("Ваш выбор:"), "Должно быть приглашение к выбору");
    }

    /**
     * Тестирование сброса состояния викторины.
     * Проверяет корректность обнуления счета и текущего вопроса.
     */
    @Test
    public void testQuizReset() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Вопрос", new String[]{"А", "Б", "В", "Г"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, false);

        quiz.processAnswer(0);
        assertEquals(1, quiz.getScore(), "Счет должен быть 1 после правильного ответа");

        quiz.reset();
        assertEquals(0, quiz.getScore(), "Счет должен быть 0 после сброса");
    }

    /**
     * Тестирование режима выбора темы.
     * Проверяет корректность работы в режиме выбора темы.
     */
    @Test
    public void testChooseMode() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Выберите тему", new String[]{"История", "География", "Наука"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, true);

        assertTrue(quiz.isChooseMode(), "Режим должен быть 'выбор темы'");
        String question = quiz.getCurrentQuestion();
        assertTrue(question.contains("Выберите тему"), "Вопрос должен содержать текст выбора темы");
        assertTrue(question.contains("Ваш выбор:"), "Вопрос должен содержать приглашение к выбору");
    }

    /**
     * Тестирование обработки пустого ввода.
     * Проверяет корректность обработки пустых строк.
     */
    @Test
    public void testEmptyInput() {
        String input = "\n\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Quiz quiz = new Quiz();

        assertEquals(-1, quiz.getIdxAnswer(), "Пустая строка должна возвращать -1");
        assertEquals(-1, quiz.getIdxAnswer(), "Пустая строка должна возвращать -1");
    }

    /**
     * Тестирование обработки невалидного ввода.
     * Проверяет корректность обработки некорректных данных.
     */
    @Test
    public void testInvalidInput() {
        String input = "X\n5\nZ\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Quiz quiz = new Quiz();

        assertEquals(-1, quiz.getIdxAnswer(), "Невалидный символ X должен возвращать -1");
        assertEquals(-1, quiz.getIdxAnswer(), "Цифра 5 (вне диапазона) должна возвращать -1");
        assertEquals(-1, quiz.getIdxAnswer(), "Невалидный символ Z должен возвращать -1");
    }

    /**
     * Тестирование получения хранилища данных.
     * Проверяет корректность работы метода getMemory.
     */
    @Test
    public void testQuizGetMemory() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Вопрос", new String[]{"А", "Б"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory, false);

        Memory retrievedMemory = quiz.getMemory();
        assertNotNull(retrievedMemory, "Полученное хранилище не должно быть null");
        assertEquals(1, retrievedMemory.getData().length, "Количество вопросов должно быть 1");
        assertEquals("Вопрос", retrievedMemory.getData()[0].getQuestion(),
                "Текст вопроса должен соответствовать исходному");
    }

    /**
     * Тестирование запуска викторины в режиме выбора темы.
     * Проверяет корректность работы метода run в режиме выбора темы.
     */
    @Test
    public void testQuizRunChooseMode() {
        Memory memory = new Memory();
        Data[] data = new Data[] {
                new Data("Выберите тему", new String[]{"Тема 1", "Тема 2"}, 0)
        };
        memory.setData(data);

        String input = "A\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Quiz quiz = new Quiz(memory, true);
        String result = quiz.run(memory, true);

        assertTrue(result.contains("Выбрана тема: Тема 1"), "Результат должен содержать выбранную тему");
        assertTrue(result.contains("Для начала викторины используйте команду /start"), "Результат должен содержать инструкции для продолжения");
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
        assertEquals(true, response.getText().contains("Лидерборд"), "Текст должен содержать лидерборд");
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