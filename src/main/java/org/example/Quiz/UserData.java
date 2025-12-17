package org.example.Quiz;

import org.example.TopicSelector.TopicSelector;
import org.example.ModeGame.ModeSelector;
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

    @JsonIgnore
    private transient ModeSelector currentMode;

    // Поля для дуэли
    @JsonIgnore
    private transient String gameMode;
    @JsonIgnore
    private transient String duelTopic;
    @JsonIgnore
    private transient String quizMode;
    @JsonIgnore
    private transient String duelId;
    @JsonIgnore
    private transient String duelOpponent;
    @JsonIgnore
    private transient String duelGenerationTopic;
    @JsonIgnore
    private transient long duelStartTime;
    @JsonIgnore
    private transient long duelEndTime;
    @JsonIgnore
    private transient long searchStartTime;

    /**
     * Конструктор по умолчанию для Jackson
     */
    public UserData() {
        this.state = "menu";
        this.score = 0;
        this.allScore = new HashMap<>();
    }

    // Геттеры и сеттеры для основных полей
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Map<String, Integer> getAllScore() { return allScore; }
    public void setAllScore(Map<String, Integer> allScore) { this.allScore = allScore; }

    public String getLeaderboardName() { return leaderboardName; }
    public void setLeaderboardName(String leaderboardName) { this.leaderboardName = leaderboardName; }

    // Геттеры для только для внутреннего использования
    @JsonIgnore
    public Quiz getCurrentQuiz() { return currentQuiz; }
    @JsonIgnore
    public void setCurrentQuiz(Quiz currentQuiz) { this.currentQuiz = currentQuiz; }

    @JsonIgnore
    public TopicSelector getTopicSelector() { return topicSelector; }
    @JsonIgnore
    public void setTopicSelector(TopicSelector topicSelector) { this.topicSelector = topicSelector; }

    @JsonIgnore
    public ModeSelector getCurrentMode() { return currentMode; }
    @JsonIgnore
    public void setCurrentMode(ModeSelector currentMode) { this.currentMode = currentMode; }

    // Геттеры для полей дуэли
    @JsonIgnore
    public String getGameMode() { return gameMode; }
    @JsonIgnore
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    @JsonIgnore
    public String getDuelTopic() { return duelTopic; }
    @JsonIgnore
    public void setDuelTopic(String duelTopic) { this.duelTopic = duelTopic; }

    @JsonIgnore
    public String getQuizMode() { return quizMode; }
    @JsonIgnore
    public void setQuizMode(String quizMode) { this.quizMode = quizMode; }

    @JsonIgnore
    public String getDuelId() { return duelId; }
    @JsonIgnore
    public void setDuelId(String duelId) { this.duelId = duelId; }

    @JsonIgnore
    public String getDuelOpponent() { return duelOpponent; }
    @JsonIgnore
    public void setDuelOpponent(String duelOpponent) { this.duelOpponent = duelOpponent; }

    @JsonIgnore
    public String getDuelGenerationTopic() { return duelGenerationTopic; }
    @JsonIgnore
    public void setDuelGenerationTopic(String duelGenerationTopic) { this.duelGenerationTopic = duelGenerationTopic; }

    @JsonIgnore
    public long getDuelStartTime() { return duelStartTime; }
    @JsonIgnore
    public long getDuelEndTime() { return duelEndTime; }

    @JsonIgnore
    public long getSearchStartTime() { return searchStartTime; }
    @JsonIgnore
    public void setSearchStartTime(long searchStartTime) { this.searchStartTime = searchStartTime; }

    // МЕТОДЫ ДЛЯ ДУЭЛИ

    /**
     * Отмечает начало дуэли (запускается при получении первого вопроса)
     */
    public void markDuelStartTime() {
        this.duelStartTime = System.currentTimeMillis();
        this.duelEndTime = 0;
    }

    /**
     * Отмечает завершение дуэли и останавливает таймер
     * Возвращает финальное время в миллисекундах
     */
    public long markDuelCompletion() {
        if (duelStartTime > 0 && duelEndTime == 0) {
            this.duelEndTime = System.currentTimeMillis();
            return duelEndTime - duelStartTime;
        }
        return 0;
    }

    /**
     * Возвращает общее время дуэли в миллисекундах
     */
    public long getDuelTotalTime() {
        if (duelStartTime == 0) {
            return 0;
        }
        if (duelEndTime > 0) {
            return duelEndTime - duelStartTime;
        } else {
            return System.currentTimeMillis() - duelStartTime;
        }
    }

    /**
     * Сбрасывает таймер дуэли
     */
    public void resetDuelTimer() {
        this.duelStartTime = 0;
        this.duelEndTime = 0;
    }

    /**
     * Сбрасывает таймеры и временные данные дуэли
     */
    public void clearDuelData() {
        this.duelId = null;
        this.duelOpponent = null;
        this.duelTopic = null;
        this.duelGenerationTopic = null;
        this.quizMode = null;
        this.gameMode = null;
        this.resetDuelTimer();
    }

    /**
     * Сбрасывает только таймер дуэли (не все данные)
     */
    public void resetDuelTimers() {
        this.resetDuelTimer();
    }

    // Вспомогательные методы
    @JsonIgnore
    public boolean isSoloMode() {
        return "solo".equals(gameMode);
    }

    @JsonIgnore
    public boolean isDuelMode() {
        return "duel".equals(gameMode);
    }

    @JsonIgnore
    public boolean isInMenu() {
        return "menu".equals(state);
    }

    @JsonIgnore
    public boolean isInQuiz() {
        return state != null && state.contains("quiz");
    }

    @JsonIgnore
    public boolean isInTopicSelection() {
        return state != null && state.contains("topic_selection");
    }

    public void resetSession() {
        this.state = "menu";
        this.clearDuelData();
        this.currentQuiz = null;
        this.topicSelector = null;
        this.currentMode = null;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void updateTopicScore(String topicName, int points) {
        allScore.put(topicName, allScore.getOrDefault(topicName, 0) + points);
    }

    @JsonIgnore
    public String getTopicSelection() {
        if (topicSelector != null) {
            return topicSelector.getCurrentTopic();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("UserData{state='%s', score=%d, leaderboardName='%s'}",
                state, score, leaderboardName);
    }
}