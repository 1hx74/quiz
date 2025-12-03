package org.example;

import org.example.GenerationQuiz.CreateQuiz;
import org.example.Quiz.Memory;
import org.example.Quiz.DataQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;

public class testParser {

    private CreateQuiz createQuiz;
    private Method parseMethod;

    @BeforeEach
    public void setUp() throws Exception {
        createQuiz = new CreateQuiz(null);
        parseMethod = CreateQuiz.class.getDeclaredMethod("parseQuizResponse", String.class, String.class);
        parseMethod.setAccessible(true);
    }

    /**
     * Тест правильного парсинга JSON.
     */
    @Test
    public void testParseCorrectJson() throws Exception {
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

        Memory memory = (Memory) parseMethod.invoke(createQuiz, json, "Тест");

        Assertions.assertNotNull(memory);
        Assertions.assertEquals(2, memory.getData().length);

        // Проверяем первый вопрос
        DataQuestion q1 = memory.getData()[0];
        Assertions.assertEquals("Вопрос 1", q1.getQuestion());
        Assertions.assertArrayEquals(new String[]{"A", "B", "C", "D"}, q1.getOptions());
        Assertions.assertTrue(q1.validAnswer(0)); // Правильный ответ A (индекс 0)

        // Проверяем второй вопрос
        DataQuestion q2 = memory.getData()[1];
        Assertions.assertEquals("Вопрос 2", q2.getQuestion());
        Assertions.assertArrayEquals(new String[]{"1", "2", "3", "4"}, q2.getOptions());
        Assertions.assertTrue(q2.validAnswer(2)); // Правильный ответ 3 (индекс 2)
    }

    /**
     * Тест парсинга JSON с код блоками.
     */
    @Test
    public void testParseJsonWithCodeBlocks() throws Exception {
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

        Memory memory = (Memory) parseMethod.invoke(createQuiz, json, "Наука");

        Assertions.assertNotNull(memory);
        Assertions.assertEquals(1, memory.getData().length);

        DataQuestion question = memory.getData()[0];
        Assertions.assertEquals("Сколько планет?", question.getQuestion());
        Assertions.assertTrue(question.validAnswer(0)); // Правильный ответ 8 (индекс 0)
    }

    /**
     * Тест неправильного JSON.
     */
    @Test
    public void testParseIncorrectJson() {
        String invalidJson = "{invalid json}";

        Assertions.assertThrows(Exception.class, () ->
                parseMethod.invoke(createQuiz, invalidJson, "Ошибка")
        );
    }

    /**
     * Тест пустого JSON.
     */
    @Test
    public void testParseEmptyJson() {
        String emptyJson = "{}";

        Assertions.assertThrows(Exception.class, () ->
                parseMethod.invoke(createQuiz, emptyJson, "Пусто")
        );
    }
}