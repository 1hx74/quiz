package org.example;

import org.example.DataMessage.KeyboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

import static org.example.DataMessage.Constants.*;

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
        Assertions.assertEquals(QUIZ_BUTTON, mainButtons.get("Викторины"));
        Assertions.assertEquals(GENERATION_BUTTON, mainButtons.get("Сгенерировать"));
    }

    /**
     * Тестирование получения кнопок ответов на тесты.
     */
    @Test
    public void testGetTestAnswerButtons() {
        Map<String, String> testButtons = keyboardService.getTestAnswerButtons();

        Assertions.assertEquals(4, testButtons.size(), "Должно быть 4 кнопки ответов");
        Assertions.assertEquals(A_BUTTON, testButtons.get("A"));
        Assertions.assertEquals(B_BUTTON, testButtons.get("B"));
        Assertions.assertEquals(C_BUTTON, testButtons.get("C"));
        Assertions.assertEquals(D_BUTTON, testButtons.get("D"));
    }

    /**
     * Тестирование получения кнопок навигации по темам.
     */
    @Test
    public void testGetForwardsAndBackwardsTopicButtons() {
        Map<String, String> topicNavButtons = keyboardService.getForwardsAndBackwardsTopicButtons();

        Assertions.assertEquals(2, topicNavButtons.size(), "Должно быть 2 кнопки навигации по темам");
        Assertions.assertEquals(TOPIC_FORWARDS_BUTTON, topicNavButtons.get("Вперед"));
        Assertions.assertEquals(TOPIC_BACKWARDS_BUTTON, topicNavButtons.get("Назад"));
    }

    /**
     * Тестирование получения кнопок навигации по викторине.
     */
    @Test
    public void testGetForwardsAndBackwardsQuizButtons() {
        Map<String, String> quizNavButtons = keyboardService.getForwardsAndBackwardsQuizButtons();

        Assertions.assertEquals(2, quizNavButtons.size(), "Должно быть 2 кнопки навигации по викторине");
        Assertions.assertEquals(QUIZ_FORWARDS_BUTTON, quizNavButtons.get("Вперед"));
        Assertions.assertEquals(QUIZ_BACKWARDS_BUTTON, quizNavButtons.get("Назад"));
    }

    /**
     * Тестирование получения кнопок выбора викторины.
     */
    @Test
    public void testGetChoiceQuiz() {
        Map<String, String> choiceButtons = keyboardService.getChoiceQuiz();

        Assertions.assertEquals(2, choiceButtons.size(), "Должно быть 2 кнопки выбора викторины");
        Assertions.assertEquals(PLAY_BUTTON, choiceButtons.get("Играть"));
        Assertions.assertEquals(MENU_BUTTON, choiceButtons.get("В меню"));
    }

    /**
     * Тестирование получения кнопок финального сообщения викторины.
     */
    @Test
    public void testGetFinalQuizButton() {
        Map<String, String> finalButtons = keyboardService.getFinalQuizButton();

        Assertions.assertEquals(2, finalButtons.size(), "Должно быть 2 кнопки в финальном сообщении");
        Assertions.assertEquals(AT_THE_TOP_BUTTON, finalButtons.get("В начало"));
        Assertions.assertEquals(END_QUIZ_BUTTON, finalButtons.get("Сдать"));
    }

    /**
     * Тестирование получения кнопки возврата в меню.
     */
    @Test
    public void testGetGoMenu() {
        Map<String, String> goMenuButtons = keyboardService.getGoMenu();

        Assertions.assertEquals(1, goMenuButtons.size(), "Должна быть 1 кнопка возврата в меню");
        Assertions.assertEquals(MENU_BUTTON, goMenuButtons.get("В меню"));
    }

}