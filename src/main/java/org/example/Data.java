package org.example;

/**
 * Класс для хранения данных вопроса викторины.
 * Содержит вопрос, варианты ответов и правильный ответ.
 */

public class Data {
    private String question;
    private String[] options;
    private int answer;

    Data() {}

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public int getAnswer() {
        return answer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public boolean validAnswer(int userAnswer) {
        return userAnswer == answer;
    }
}
