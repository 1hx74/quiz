package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.HashMap;

/**
 * Класс для хранения данных пользователя.
 * Содержит информацию о состоянии, прогрессе и результатах пользователя в викторине.
 */
public class UserData {
    private final String chatId;        //ТЕПЕРЬ FINAL
    private String state;
    private int score;
    private final Map<String, Integer> allScore;
    private String leaderboardName;

    // Временные данные викторины (не сохраняются на диск)
    @JsonIgnore
    private transient Quiz currentQuiz;
    @JsonIgnore
    private transient String lastQuestion;

    /**
     * Конструктор для создания нового пользователя.
     *
     * @param chatId идентификатор чата пользователя
     */
    public UserData(String chatId) {
        this.chatId = chatId;
        this.allScore = new HashMap<>();
        this.score = 0;
        this.state = "menu";
        this.leaderboardName = null;
    }

    /**
     * Конструктор по умолчанию для Jackson.
     */
    public UserData() {
        this.chatId = null;
        this.allScore = new HashMap<>();
        this.score = 0;
        this.state = "menu";
        this.leaderboardName = null;
    }

    public String getChatId() {
        return chatId;
    }

    public String getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getAllScore() {
        return new HashMap<>(allScore); // Защитная копия
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public Quiz getCurrentQuiz() {
        return currentQuiz;
    }

    public String getLastQuestion() {
        return lastQuestion;
    }

    // Сеттеры только для изменяемых полей

    public void setState(String state) {
        this.state = state;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Устанавливает результаты по темам.
     * Создает новую карту для защиты от внешних изменений.
     */
    public void setAllScore(Map<String, Integer> allScore) {
        this.allScore.clear();
        if (allScore != null) {
            this.allScore.putAll(allScore);
        }
    }

    public void setLeaderboardName(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }

    public void setCurrentQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
    }

    public void setLastQuestion(String question) {
        this.lastQuestion = question;
    }

    /**
     * Добавляет результат по конкретной теме.
     *
     * @param topic тема
     * @param score количество баллов
     */
    public void addTopicScore(String topic, int score) {
        this.allScore.put(topic, score);
    }

    /**
     * Увеличивает общий счет на указанное значение.
     *
     * @param additionalScore дополнительные баллы
     */
    public void addToScore(int additionalScore) {
        this.score += additionalScore;
    }
}