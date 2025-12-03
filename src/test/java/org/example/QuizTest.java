package org.example;

import org.example.Quiz.DataQuestion;
import org.example.Quiz.Memory;
import org.example.Quiz.Quiz;
import org.example.Quiz.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Тестовый класс для проверки функциональности класса Quiz.
 * Содержит тесты для всех основных методов и сценариев использования.
 */
class QuizTest {
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
     * Тестирование создания викторины с памятью.
     */
    @Test
    public void testQuizConstructorWithMemory() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        Assertions.assertNotNull(quiz, "Объект Quiz не должен быть null");
        Assertions.assertEquals(memory, quiz.getMemory(), "Память должна соответствовать переданной");
        Assertions.assertEquals(0, quiz.getScore(), "Начальный счет должен быть 0");
        Assertions.assertEquals(0, quiz.getCurrentQuestionIndex(), "Начальный индекс вопроса должен быть 0");
    }

    /**
     * Тестирование создания пустой викторины.
     */
    @Test
    public void testQuizDefaultConstructor() {
        Quiz quiz = new Quiz();

        Assertions.assertNotNull(quiz, "Объект Quiz не должен быть null");
        Assertions.assertNotNull(quiz.getMemory(), "Память не должна быть null");
        Assertions.assertEquals(0, quiz.getScore(), "Начальный счет должен быть 0");
        Assertions.assertEquals(0, quiz.getCurrentQuestionIndex(), "Начальный индекс вопроса должен быть 0");
    }

    /**
     * Тестирование обработки правильного ответа.
     */
    @Test
    public void testQuizProcessCorrectAnswer() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        String result = quiz.processAnswer("B");

        Assertions.assertEquals("✅ Ваш ответ \"B\" успешно сохранен!", result,
                "Должно возвращаться сообщение об успешном сохранении");
        Assertions.assertEquals(1, quiz.getScore(), "Счет должен увеличиться на 1 после правильного ответа");
    }

    /**
     * Тестирование обработки неправильного ответа.
     */
    @Test
    public void testQuizProcessIncorrectAnswer() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        String result = quiz.processAnswer("A");

        Assertions.assertEquals("✅ Ваш ответ \"A\" успешно сохранен!", result,
                "Должно возвращаться сообщение об успешном сохранении");
        Assertions.assertEquals(0, quiz.getScore(), "Счет не должен измениться после неправильного ответа");
    }

    /**
     * Тестирование обработки повторного выбора того же ответа.
     */
    @Test
    public void testQuizProcessSameAnswer() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        quiz.processAnswer("A");
        String result = quiz.processAnswer("A");

        Assertions.assertEquals("ℹ️ Вы уже выбрали этот ответ", result,
                "Должно возвращаться сообщение о повторном выборе");
    }

    /**
     * Тестирование навигации между вопросами.
     */
    @Test
    public void testQuizNavigation() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1),
                new DataQuestion("Вопрос 3", new String[]{"А", "Б", "В", "Г"}, 2)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        // Переход к следующему вопросу
        quiz.nextQuestion();
        Assertions.assertEquals(1, quiz.getCurrentQuestionIndex(), "Индекс должен увеличиться до 1");

        // Переход к предыдущему вопросу
        quiz.previousQuestion();
        Assertions.assertEquals(0, quiz.getCurrentQuestionIndex(), "Индекс должен уменьшиться до 0");

        // Переход к первому вопросу
        quiz.nextQuestion();
        quiz.nextQuestion();
        quiz.goToFirstQuestion();
        Assertions.assertEquals(0, quiz.getCurrentQuestionIndex(), "Индекс должен быть сброшен до 0");
    }

    /**
     * Тестирование перехода к финальному сообщению.
     */
    @Test
    public void testQuizGoToFinalMessage() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        quiz.goToFinalMessage();
        Assertions.assertEquals(2, quiz.getCurrentQuestionIndex(), "Индекс должен быть равен количеству вопросов");
        Assertions.assertTrue(quiz.isFinished(), "Викторина должна быть завершена");
        Assertions.assertTrue(quiz.isOnFinalMessage(), "Викторина должна быть на финальном сообщении");
    }

    /**
     * Тестирование получения текста текущего вопроса.
     */
    @Test
    public void testQuizGetCurrentQuestionText() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос?", new String[]{"Ответ А", "Ответ Б", "Ответ В", "Ответ Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        String questionText = quiz.getCurrentQuestionText();

        String expectedText = """
                🎯 Вопрос 1 из 1
                
                Тестовый вопрос?
                
                A) Ответ А
                B) Ответ Б
                C) Ответ В
                D) Ответ Г
                """;

        Assertions.assertEquals(expectedText, questionText, "Текст вопроса должен полностью совпадать");
    }

    /**
     * Тестирование получения текста текущего вопроса с ответом пользователя.
     */
    @Test
    public void testQuizGetCurrentQuestionTextWithAnswer() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос?", new String[]{"Ответ А", "Ответ Б", "Ответ В", "Ответ Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);
        quiz.processAnswer("B");

        String questionText = quiz.getCurrentQuestionText();

        String expectedText = """
                🎯 Вопрос 1 из 1
                
                Тестовый вопрос?
                
                A) Ответ А
                B) Ответ Б
                C) Ответ В
                D) Ответ Г
                
                📝 Ваш ответ: B""";

        Assertions.assertEquals(expectedText, questionText, "Текст вопроса с ответом должен полностью совпадать");
    }

    /**
     * Тестирование получения финального сообщения.
     */
    @Test
    public void testQuizGetFinalMessage() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        String finalMessage = quiz.getFinalMessage();

        String expectedMessage = """
                🏁 Вопросы закончились!
                
                📊 Вы ответили на 0 из 2 вопросов
                
                Проверьте свои ответы, нажмите кнопку заново или сдайте тест""";

        Assertions.assertEquals(expectedMessage, finalMessage, "Финальное сообщение должно полностью совпадать");
    }

    /**
     * Тестирование получения результатов викторины с 50% результатом.
     */
    @Test
    public void testQuizGetResultsWith50Percent() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        // Отвечаем на вопросы - 1 правильный из 2
        quiz.processAnswer("A"); // правильный
        quiz.nextQuestion();
        quiz.processAnswer("A"); // неправильный

        quiz.goToFinalMessage();

        String results = quiz.getResults();

        String expectedResults = """
                🏆 Викторина завершена!
                
                ✅ Правильных ответов: 1 из 2
                📊 Результат: 50,0%
                🎯 Отвечено вопросов: 2 из 2""";

        Assertions.assertEquals(expectedResults, results, "Результаты викторины с 50% должны полностью совпадать");
    }

    /**
     * Тестирование сброса викторины.
     */
    @Test
    public void testQuizReset() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);
        quiz.processAnswer("A");
        quiz.nextQuestion();
        quiz.processAnswer("B");

        quiz.reset();

        Assertions.assertEquals(0, quiz.getScore(), "Счет должен быть сброшен до 0");
        Assertions.assertEquals(0, quiz.getCurrentQuestionIndex(), "Индекс вопроса должен быть сброшен до 0");

        // Проверяем что ответы пользователя очищены
        for (DataQuestion question : memory.getData()) {
            Assertions.assertNull(question.getUserAnswer(), "Ответы пользователя должны быть очищены");
        }
    }

    /**
     * Тестирование обработки невалидного ответа.
     */
    @Test
    public void testQuizProcessInvalidAnswer() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Тестовый вопрос", new String[]{"Вариант А", "Вариант Б", "Вариант В", "Вариант Г"}, 1)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        String result = quiz.processAnswer("X");

        Assertions.assertEquals("✅ Ваш ответ \"X\" успешно сохранен!", result,
                "Должно возвращаться сообщение об успешном сохранении даже для невалидного ответа");
        Assertions.assertEquals(0, quiz.getScore(), "Счет не должен измениться для невалидного ответа");
    }

    /**
     * Тестирование викторины без вопросов.
     */
    @Test
    public void testQuizWithNoQuestions() {
        Memory memory = new Memory();
        memory.setData(new DataQuestion[0]);

        Quiz quiz = new Quiz(memory);

        String questionText = quiz.getCurrentQuestionText();
        Assertions.assertEquals("❌ Нет доступных вопросов", questionText,
                "Должно возвращаться сообщение об отсутствии вопросов");

        String result = quiz.processAnswer("A");
        Assertions.assertEquals("❌ Викторина завершена!", result,
                "Должно возвращаться сообщение о завершении викторины");
    }

    /**
     * Тестирование конвертации ответов в индексы.
     */
    @Test
    public void testAnswerConversion() {
        Memory memory = new Memory();
        Quiz quiz = new Quiz(memory);

        // Используем рефлексию для тестирования приватных методов
        // В реальном проекте лучше вынести эти методы в отдельный утилитный класс

        // Протестируем через публичные методы, которые их используют
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос", new String[]{"А", "Б", "В", "Г"}, 0)
        };
        memory.setData(data);

        // Ответ A должен быть правильным (индекс 0)
        quiz.processAnswer("A");
        Assertions.assertEquals(1, quiz.getScore(), "Ответ A должен быть правильным");
    }

    /**
     * Тестирование получения общего количества вопросов.
     */
    @Test
    public void testQuizGetTotalQuestions() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б"}, 1),
                new DataQuestion("Вопрос 3", new String[]{"А", "Б"}, 0)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        Assertions.assertEquals(3, quiz.getTotalQuestions(), "Должно возвращать правильное количество вопросов");
    }
    /**
     * Тестирование подсчета баллов для правильных, неправильных и неотвеченных вопросов.
     */
    @Test
    public void testQuizScoreForDifferentAnswerTypes() {
        Memory memory = new Memory();
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Вопрос 1", new String[]{"А", "Б", "В", "Г"}, 0),
                new DataQuestion("Вопрос 2", new String[]{"А", "Б", "В", "Г"}, 1),
                new DataQuestion("Вопрос 3", new String[]{"А", "Б", "В", "Г"}, 2)
        };
        memory.setData(data);

        Quiz quiz = new Quiz(memory);

        // Правильный ответ на первый вопрос
        quiz.processAnswer("A");
        Assertions.assertEquals(1, quiz.getScore(), "Счет должен быть 1 за правильный ответ");

        // Неправильный ответ на второй вопрос
        quiz.nextQuestion();
        quiz.processAnswer("A"); // Правильный был бы B
        Assertions.assertEquals(1, quiz.getScore(), "Счет должен остаться 1 за неправильный ответ");

        // Третий вопрос без ответа - переходим к финалу
        quiz.nextQuestion();
        quiz.goToFinalMessage();

        // Проверяем результаты
        String results = quiz.getResults();
        String expectedResults = """
                🏆 Викторина завершена!
                
                ✅ Правильных ответов: 1 из 3
                📊 Результат: 33,3%
                🎯 Отвечено вопросов: 2 из 3""";

        Assertions.assertEquals(expectedResults, results,
                "Результаты должны показывать 1 правильный ответ из 2 отвеченных вопросов");
    }
}