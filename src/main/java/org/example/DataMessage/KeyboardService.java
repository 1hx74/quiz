package org.example.DataMessage;

import java.util.HashMap;
import java.util.Map;

import static org.example.DataMessage.Constants.*;

/**
 * Класс хранит наборы кнопок
 */
public class KeyboardService {
    // Храним название кнопки и callback для ответов на тесты в виде словаря

    //Набор кнопок для меню
    private final Map<String, String> mainButtons = Map.of(
            "Викторины", QUIZ_BUTTON,
            "Сгенерировать", GENERATION_BUTTON

    );

    //Набор кнопок для тестов, уровень 1
    private final Map<String, String> testAnswerButtons = Map.of(
            "A", A_BUTTON,
            "B", B_BUTTON,
            "C", C_BUTTON,
            "D", D_BUTTON
    );

    //Набор кнопок для выбора викторины уровень 2
    private final Map<String, String>  forwardsAndBackwardsTopicButtons = Map.of(
            "Вперед", TOPIC_FORWARDS_BUTTON,
            "Назад", TOPIC_BACKWARDS_BUTTON
    );

    //Набор кнопок для выбора викторины уровень 2
    private final Map<String, String>  forwardsAndBackwardsQuizButtons = Map.of(
            "Вперед", QUIZ_FORWARDS_BUTTON,
            "Назад", QUIZ_BACKWARDS_BUTTON
    );

    //Набор кнопок для выборки викторины, уровень 1
    private final Map<String, String> choiceQuiz = Map.of(
            "Играть", PLAY_BUTTON,
            "В меню", MENU_BUTTON
    );


    //Набор кнопок пред финального сообщения для квиза
    private final Map<String, String> finalQuizButton = Map.of(
            "В начало", AT_THE_TOP_BUTTON,
            "Сдать", END_QUIZ_BUTTON
    );

    //Набор для финального сообщения
    private final Map<String, String> goMenu = Map.of(
            "В меню", MENU_BUTTON
    );


    //геттеры
    public Map<String, String> getMainButtons() {return new HashMap<>(mainButtons);}
    public Map<String, String> getTestAnswerButtons() {return new HashMap<>(testAnswerButtons);}
    public Map<String, String> getForwardsAndBackwardsTopicButtons() {return  new HashMap<>(forwardsAndBackwardsTopicButtons);}
    public Map<String, String> getForwardsAndBackwardsQuizButtons() {return new HashMap<>(forwardsAndBackwardsQuizButtons);}
    public Map<String, String> getChoiceQuiz() {return new HashMap<>(choiceQuiz);}
    public Map<String, String> getFinalQuizButton() {return new HashMap<>(finalQuizButton);}
    public Map<String, String> getGoMenu() {return new HashMap<>(goMenu);}
}
