package org.example.Quiz;

/**
 * Класс данных для хранения вопроса, вариантов ответов и правильного ответа.
 */
public class DataQuestion {
    private final String question;
    private final String[] options;
    private final int answer;
    private String userAnswer;
    private Boolean answered;

    public DataQuestion() {
        this.question = null;
        this.options = null;
        this.answer = 0;
        this.userAnswer = null;
        this.answered = false;
    }

    public DataQuestion(String question, String[] options, int answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.userAnswer = null;
        this.answered = false;
    }

    // Геттеры и сеттеры
    public String getQuestion() { return question; }

    public String[] getOptions() { return options; }

    public int getAnswer() { return answer; }

    // Для сохранения на диск
    public Boolean getAnswered() { return answered; }
    public void setAnswered(Boolean answered) { this.answered = answered; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.answered = (userAnswer != null);
    }

    /**
     * Проверяет правильность ответа по индексу
     */
    public boolean validAnswer(int index) {
        return index == answer;
    }


    public boolean isEmpty() {
        boolean questionEmpty = question == null || question.isEmpty();
        boolean firstOptionEmpty = options == null || options.length == 0 || options[0] == null || options[0].isEmpty();
        boolean answerEmpty = answer == 0;

        return questionEmpty && firstOptionEmpty && answerEmpty;
    }


    /**
     * Проверяет, ответил ли пользователь на вопрос
     */
    public boolean isAnswered() {
        return userAnswer != null && !userAnswer.isEmpty();
    }
}