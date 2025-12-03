package org.example;

import org.example.DataMessage.KeyboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

public class TestKeyboardService {

    private KeyboardService keyboardService;

    @BeforeEach
    public void setUp() {
        keyboardService = new KeyboardService();
    }

    /**
     * Тестирование получения кнопок главного меню.
     */
    @Test
    public void testGetMainButtons() {
        Map<String, String> mainButtons = keyboardService.getMainButtons();

        Assertions.assertEquals(2, mainButtons.size(), "Должно быть 2 кнопки в главном меню");
        Assertions.assertEquals("quiz_button", mainButtons.get("Викторины"));
        Assertions.assertEquals("generation_button", mainButtons.get("Сгенерировать"));
    }

    /**
     * Тестирование получения кнопок ответов на тесты.
     */
    @Test
    public void testGetTestAnswerButtons() {
        Map<String, String> testButtons = keyboardService.getTestAnswerButtons();

        Assertions.assertEquals(4, testButtons.size(), "Должно быть 4 кнопки ответов");
        Assertions.assertEquals("A_button", testButtons.get("A"));
        Assertions.assertEquals("B_button", testButtons.get("B"));
        Assertions.assertEquals("C_button", testButtons.get("C"));
        Assertions.assertEquals("D_button", testButtons.get("D"));
    }

    /**
     * Тестирование получения кнопок навигации по темам.
     */
    @Test
    public void testGetForwardsAndBackwardsTopicButtons() {
        Map<String, String> topicNavButtons = keyboardService.getForwardsAndBackwardsTopicButtons();

        Assertions.assertEquals(2, topicNavButtons.size(), "Должно быть 2 кнопки навигации по темам");
        Assertions.assertEquals("topic_forwards_button", topicNavButtons.get("Вперед"));
        Assertions.assertEquals("topic_backwards_button", topicNavButtons.get("Назад"));
    }

    /**
     * Тестирование получения кнопок навигации по викторине.
     */
    @Test
    public void testGetForwardsAndBackwardsQuizButtons() {
        Map<String, String> quizNavButtons = keyboardService.getForwardsAndBackwardsQuizButtons();

        Assertions.assertEquals(2, quizNavButtons.size(), "Должно быть 2 кнопки навигации по викторине");
        Assertions.assertEquals("quiz_forwards_button", quizNavButtons.get("Вперед"));
        Assertions.assertEquals("quiz_backwards_button", quizNavButtons.get("Назад"));
    }

    /**
     * Тестирование получения кнопок выбора викторины.
     */
    @Test
    public void testGetChoiceQuiz() {
        Map<String, String> choiceButtons = keyboardService.getChoiceQuiz();

        Assertions.assertEquals(2, choiceButtons.size(), "Должно быть 2 кнопки выбора викторины");
        Assertions.assertEquals("play_button", choiceButtons.get("Играть"));
        Assertions.assertEquals("menu_button", choiceButtons.get("В меню"));
    }

    /**
     * Тестирование получения кнопок финального сообщения викторины.
     */
    @Test
    public void testGetFinalQuizButton() {
        Map<String, String> finalButtons = keyboardService.getFinalQuizButton();

        Assertions.assertEquals(2, finalButtons.size(), "Должно быть 2 кнопки в финальном сообщении");
        Assertions.assertEquals("at_the_top_button", finalButtons.get("В начало"));
        Assertions.assertEquals("end_quiz_button", finalButtons.get("Сдать"));
    }

    /**
     * Тестирование получения кнопки возврата в меню.
     */
    @Test
    public void testGetGoMenu() {
        Map<String, String> goMenuButtons = keyboardService.getGoMenu();

        Assertions.assertEquals(1, goMenuButtons.size(), "Должна быть 1 кнопка возврата в меню");
        Assertions.assertEquals("menu_button", goMenuButtons.get("В меню"));
    }

}