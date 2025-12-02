package org.example.DataMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс хранит наборы кнопок
 */
public class KeyboardService {
    // Храним название кнопки и callback для ответов на тесты в виде словаря

    //Набор кнопок для меню
    private final Map<String, String> mainButtons = Map.of(
            "Викторины", "quiz_button",
            "Сгенерировать", "generation_button"

    );

    //Набор кнопок для тестов, уровень 1
    private final Map<String, String> testAnswerButtons = Map.of(
            "A", "A_button",
            "B", "B_button",
            "C", "C_button",
            "D", "D_button"
    );

    //Набор кнопок для выбора викторины уровень 2
    private final Map<String, String>  forwardsAndBackwardsTopicButtons = Map.of(
            "Вперед", "topic_forwards_button",
            "Назад", "topic_backwards_button"
    );

    //Набор кнопок для выбора викторины уровень 2
    private final Map<String, String>  forwardsAndBackwardsQuizButtons = Map.of(
            "Вперед", "quiz_forwards_button",
            "Назад", "quiz_backwards_button"
    );

    //Набор кнопок для выборки викторины, уровень 1
    private final Map<String, String> choiceQuiz = Map.of(
            "Играть", "play_button",
            "В меню", "menu_button"
    );


    //Набор кнопок пред финального сообщения для квиза
    private final Map<String, String> finalQuizButton = Map.of(
            "В начало", "at_the_top_button",
            "Сдать", "end_quiz_button"
    );

    //Набор для финального сообщения
    private final Map<String, String> goMenu = Map.of(
            "В меню", "menu_button"
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
