package org.example;

/**
 * Класс для хранения данных вопроса викторины.
 * Содержит вопрос, варианты ответов и правильный ответ.
 */

public class Data {
    private final String question;  //ТЕПЕРЬ ФАЙНАЛ
    private final String[] options; //ТЕПЕРЬ ФАЙНАЛ
    private final int answer;       //ТЕПЕРЬ ФАЙНАЛ

    /**
     * Конструктор для создания объекта вопроса.
     *
     * @param question текст вопроса
     * @param options массив вариантов ответов
     * @param answer индекс правильного ответа
     */
    public Data(String question, String[] options, int answer) {
        this.question = question;
        this.options = options != null ? options.clone() : new String[0];
        this.answer = answer;
    }

    /**
     * Конструктор по умолчанию для Jackson
     */
    public Data() {
        this.question = "";
        this.options = new String[0];
        this.answer = 0;
    }

    public String getQuestion() {
        return question;
    }

    /**
     * Возвращает копию массива вариантов ответов
     */
    public String[] getOptions() {
        return options.clone();
    }

    public int getAnswer() {
        return answer;
    }

    /**
     * Проверяет правильность ответа пользователя.
     *
     * @param userAnswer индекс ответа пользователя
     * @return true если ответ правильный, false иначе
     */
    public boolean validAnswer(int userAnswer) {
        return userAnswer == answer;
    }
}