package org.example;

import org.example.GenerationQuiz.CreateQuiz;
import org.example.Quiz.Memory.AiMemory;
import org.example.Quiz.DataQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тестовый класс для проверки парсинга JSON-ответов от ИИ.
 * Проверяет корректность преобразования JSON-строк в объекты AiMemory.
 * У тестов есть вспомогательный класс TestCreateQuiz, который наследует CreateQuiz
 * и делает его публичным
 */
public class ParserTest {

    private CreateQuiz testQuiz;

    /**
     * Метод, выполняемый перед каждым тестом.
     * Инициализирует тестовый объект TestCreateQuiz с null-клиентом.
     */
    @BeforeEach
    public void setUp() {
        testQuiz = new CreateQuiz(null);
    }

    /**
     * Тестирует парсинг корректного JSON без код-блоков.
     * Проверяет, что парсинг завершается без ошибок, создаётся объект AiMemory
     * с правильным количеством вопросов, текст вопросов и варианты ответов
     * соответствуют ожидаемым, правильные ответы определяются корректно.
     */
    @Test
    public void testParseCorrectJson() {
        String json = """
        {
          "quiz_topic": "Тест",
          "questions": [
            {
              "question": "Вопрос 1",
              "options": ["A", "B", "C", "D"],
              "correct_answer": 0
            },
            {
              "question": "Вопрос 2",
              "options": ["1", "2", "3", "4"],
              "correct_answer": 2
            }
          ]
        }
        """;

        AiMemory memory = testQuiz.parseQuizResponse(json, "Тест");

        Assertions.assertNotNull(memory);
        Assertions.assertEquals(2, memory.getData().length);

        // Проверяем первый вопрос
        DataQuestion q1 = memory.getData()[0];
        Assertions.assertEquals("Вопрос 1", q1.getQuestion());
        Assertions.assertArrayEquals(new String[]{"A", "B", "C", "D"}, q1.getOptions());
        Assertions.assertTrue(q1.validAnswer(0));

        // Проверяем второй вопрос
        DataQuestion q2 = memory.getData()[1];
        Assertions.assertEquals("Вопрос 2", q2.getQuestion());
        Assertions.assertArrayEquals(new String[]{"1", "2", "3", "4"}, q2.getOptions());
        Assertions.assertTrue(q2.validAnswer(2));
    }

    /**
     * Тестирует парсинг JSON, обёрнутого в код-блоки markdown.
     * Проверяет, что парсер корректно извлекает JSON из блоков ```json и парсит его.
     */
    @Test
    public void testParseJsonWithCodeBlocks() {
        String json = """
        ```json
        {
          "quiz_topic": "Наука",
          "questions": [
            {
              "question": "Сколько планет?",
              "options": ["8", "9", "7", "10"],
              "correct_answer": 0
            }
          ]
        }
        ```
        """;

        AiMemory memory = testQuiz.parseQuizResponse(json, "Наука");

        Assertions.assertNotNull(memory);
        Assertions.assertEquals(1, memory.getData().length);

        DataQuestion question = memory.getData()[0];
        Assertions.assertEquals("Сколько планет?", question.getQuestion());
        Assertions.assertTrue(question.validAnswer(0));
    }

    /**
     * Тестирует обработку некорректного JSON.
     * Ожидается, что метод выбросит RuntimeException при попытке парсинга
     * синтаксически неправильного JSON.
     */
    @Test
    public void testParseIncorrectJson() {
        String invalidJson = "{invalid json}";

        Assertions.assertThrows(RuntimeException.class, () ->
                testQuiz.parseQuizResponse(invalidJson, "Ошибка")
        );
    }

    /**
     * Тестирует обработку пустого JSON объекта.
     * Ожидается, что метод выбросит RuntimeException при попытке парсинга
     * JSON, который не содержит обязательных полей.
     */
    @Test
    public void testParseEmptyJson() {
        String emptyJson = "{}";

        Assertions.assertThrows(RuntimeException.class, () ->
                testQuiz.parseQuizResponse(emptyJson, "Пусто")
        );
    }
}