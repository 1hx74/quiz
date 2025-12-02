package org.example.Quiz;

import org.example.TopicSelector.TopicSelector;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.HashMap;

/**
 * Класс для хранения данных пользователя.
 */
public class UserData {
    private String state;
    private int score;
    private Map<String, Integer> allScore;
    private String leaderboardName;

    @JsonIgnore
    private transient Quiz currentQuiz;

    @JsonIgnore
    private transient TopicSelector topicSelector;

    /**
     * Конструктор по умолчанию для Jackson
     */
    public UserData() {
        this.state = "menu";
        this.score = 0;
        this.allScore = new HashMap<>();
    }

    // Геттеры и сеттеры
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<String, Integer> getAllScore() {
        return allScore;
    }

    public void setAllScore(Map<String, Integer> allScore) {
        this.allScore = allScore;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public void setLeaderboardName(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }

    public Quiz getCurrentQuiz() {
        return currentQuiz;
    }

    public void setCurrentQuiz(Quiz currentQuiz) {
        this.currentQuiz = currentQuiz;
    }

    public TopicSelector getTopicSelector() {
        return topicSelector;
    }

    public void setTopicSelector(TopicSelector topicSelector) {
        this.topicSelector = topicSelector;
    }
}