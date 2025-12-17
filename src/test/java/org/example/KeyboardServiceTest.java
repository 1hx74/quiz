package org.example;

import org.example.DataMessage.KeyboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

import static org.example.DataMessage.Constants.*;

public class KeyboardServiceTest {

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
        Assertions.assertEquals(QUIZ_BUTTON, mainButtons.get("Викторины"), "Кнопка 'Викторины' должна иметь правильный callback");
        Assertions.assertEquals(GENERATION_BUTTON, mainButtons.get("Сгенерировать"), "Кнопка 'Сгенерировать' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок ответов на тесты.
     */
    @Test
    public void testGetTestAnswerButtons() {
        Map<String, String> testButtons = keyboardService.getTestAnswerButtons();

        Assertions.assertEquals(4, testButtons.size(), "Должно быть 4 кнопки ответов");
        Assertions.assertEquals(A_BUTTON, testButtons.get("A"), "Кнопка 'A' должна иметь правильный callback");
        Assertions.assertEquals(B_BUTTON, testButtons.get("B"), "Кнопка 'B' должна иметь правильный callback");
        Assertions.assertEquals(C_BUTTON, testButtons.get("C"), "Кнопка 'C' должна иметь правильный callback");
        Assertions.assertEquals(D_BUTTON, testButtons.get("D"), "Кнопка 'D' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок навигации по темам.
     */
    @Test
    public void testGetForwardsAndBackwardsTopicButtons() {
        Map<String, String> topicNavButtons = keyboardService.getForwardsAndBackwardsTopicButtons();

        Assertions.assertEquals(2, topicNavButtons.size(), "Должно быть 2 кнопки навигации по темам");
        Assertions.assertEquals(TOPIC_FORWARDS_BUTTON, topicNavButtons.get("Вперед"), "Кнопка 'Вперед' должна иметь правильный callback");
        Assertions.assertEquals(TOPIC_BACKWARDS_BUTTON, topicNavButtons.get("Назад"), "Кнопка 'Назад' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок навигации по викторине.
     */
    @Test
    public void testGetForwardsAndBackwardsQuizButtons() {
        Map<String, String> quizNavButtons = keyboardService.getForwardsAndBackwardsQuizButtons();

        Assertions.assertEquals(2, quizNavButtons.size(), "Должно быть 2 кнопки навигации по викторине");
        Assertions.assertEquals(QUIZ_FORWARDS_BUTTON, quizNavButtons.get("Вперед"), "Кнопка 'Вперед' должна иметь правильный callback");
        Assertions.assertEquals(QUIZ_BACKWARDS_BUTTON, quizNavButtons.get("Назад"), "Кнопка 'Назад' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок выбора викторины.
     */
    @Test
    public void testGetChoiceQuiz() {
        Map<String, String> choiceButtons = keyboardService.getChoiceQuiz();

        Assertions.assertEquals(2, choiceButtons.size(), "Должно быть 2 кнопки выбора викторины");
        Assertions.assertEquals(PLAY_BUTTON, choiceButtons.get("Играть"), "Кнопка 'Играть' должна иметь правильный callback");
        Assertions.assertEquals(MENU_BUTTON, choiceButtons.get("В меню"), "Кнопка 'В меню' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок финального сообщения викторины.
     */
    @Test
    public void testGetFinalQuizButton() {
        Map<String, String> finalButtons = keyboardService.getFinalQuizButton();

        Assertions.assertEquals(2, finalButtons.size(), "Должно быть 2 кнопки в финальном сообщении");
        Assertions.assertEquals(AT_THE_TOP_BUTTON, finalButtons.get("В начало"), "Кнопка 'В начало' должна иметь правильный callback");
        Assertions.assertEquals(END_QUIZ_BUTTON, finalButtons.get("Сдать"), "Кнопка 'Сдать' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопки возврата в меню.
     */
    @Test
    public void testGetGoMenu() {
        Map<String, String> goMenuButtons = keyboardService.getGoMenu();

        Assertions.assertEquals(1, goMenuButtons.size(), "Должна быть 1 кнопка возврата в меню");
        Assertions.assertEquals(MENU_BUTTON, goMenuButtons.get("В меню"), "Кнопка 'В меню' должна иметь правильный callback");
    }

    /**
     * Тестирование получения кнопок выбора режима игры (дуэль/соло).
     */
    @Test
    public void testGetModeSelection() {
        Map<String, String> modeButtons = keyboardService.getModeSelection();

        Assertions.assertEquals(2, modeButtons.size(), "Должно быть 2 кнопки выбора режима");
        Assertions.assertEquals(DUEL_BUTTON, modeButtons.get("Дуэль"), "Кнопка 'Дуэль' должна иметь правильный callback");
        Assertions.assertEquals(SOLO_BUTTON, modeButtons.get("Соло"), "Кнопка 'Соло' должна иметь правильный callback");
    }

    /**
     * Тестирование того, что возвращаемые Map являются копиями, а не оригинальными объектами.
     */
    @Test
    public void testGetButtonsReturnCopies() {
        Map<String, String> buttons1 = keyboardService.getMainButtons();
        Map<String, String> buttons2 = keyboardService.getMainButtons();

        Assertions.assertNotSame(buttons1, buttons2, "Метод должен возвращать новые копии Map");
        Assertions.assertEquals(buttons1, buttons2, "Копии должны содержать одинаковые данные");
    }

    /**
     * Тестирование неизменяемости возвращаемых Map.
     */
    @Test
    public void testGetButtonsImmutability() {
        Map<String, String> buttons = keyboardService.getMainButtons();
        int originalSize = buttons.size();

        // Попытка изменить Map
        buttons.put("Новая кнопка", "new_button");

        // Проверяем, что оригинальный набор кнопок не изменился
        Map<String, String> freshButtons = keyboardService.getMainButtons();
        Assertions.assertEquals(originalSize, freshButtons.size(), "Оригинальный набор кнопок не должен изменяться");
        Assertions.assertFalse(freshButtons.containsKey("Новая кнопка"), "Оригинальный набор не должен содержать добавленную кнопку");
    }

    /**
     * Тестирование правильности всех callback-значений.
     */
    @Test
    public void testAllCallbacksEndWithButton() {
        // Проверяем все наборы кнопок
        Map<String, String>[] allButtonSets = new Map[]{
                keyboardService.getMainButtons(),
                keyboardService.getTestAnswerButtons(),
                keyboardService.getForwardsAndBackwardsTopicButtons(),
                keyboardService.getForwardsAndBackwardsQuizButtons(),
                keyboardService.getChoiceQuiz(),
                keyboardService.getFinalQuizButton(),
                keyboardService.getGoMenu(),
                keyboardService.getModeSelection()
        };

        for (Map<String, String> buttonSet : allButtonSets) {
            for (String callback : buttonSet.values()) {
                Assertions.assertTrue(callback.endsWith("_button"),
                        "Callback должен заканчиваться на '_button': " + callback);
            }
        }
    }
}