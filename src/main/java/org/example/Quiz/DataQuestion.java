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

    /**
     * Конструктор по умолчанию. Создает пустой объект вопроса.
     */
    public DataQuestion() {
        this.question = null;
        this.options = null;
        this.answer = 0;
        this.userAnswer = null;
        this.answered = false;
    }

    /**
     * Конструктор с параметрами для создания вопроса с ответами.
     *
     * @param question Текст вопроса
     * @param options  Массив вариантов ответов
     * @param answer   Индекс правильного ответа в массиве options (начинается с 0)
     */
    public DataQuestion(String question, String[] options, int answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.userAnswer = null;
        this.answered = false;
    }

    /**
     * Конструктор копирования - создает глубокую копию объекта
     * с теми же вопросами и правильными ответами,
     * но сбрасывает ответ пользователя.
     *
     * @param other Исходный объект DataQuestion для копирования
     */
    public DataQuestion(DataQuestion other) {
        this.question = other.question;
        // Клонируем массив вариантов ответов
        this.options = other.options != null ? other.options.clone() : null;
        this.answer = other.answer;
        this.userAnswer = null;  // ОЧЕНЬ ВАЖНО: сбрасываем ответ пользователя!
        this.answered = false;   // И флаг ответа тоже сбрасываем!
    }

    // Геттеры и сеттеры

    /**
     * Возвращает текст вопроса.
     *
     * @return Текст вопроса
     */
    public String getQuestion() { return question; }

    /**
     * Возвращает массив вариантов ответов.
     *
     * @return Массив вариантов ответов
     */
    public String[] getOptions() { return options; }

    /**
     * Возвращает индекс правильного ответа.
     *
     * @return Индекс правильного ответа (начинается с 0)
     */
    public int getAnswer() { return answer; }

    /**
     * Возвращает флаг, указывающий, был ли дан ответ.
     * Используется для сохранения на диск.
     *
     * @return true - если ответ был дан, false - в противном случае
     */
    public Boolean getAnswered() { return answered; }

    /**
     * Устанавливает флаг, указывающий, был ли дан ответ.
     *
     * @param answered true - если ответ был дан, false - в противном случае
     */
    public void setAnswered(Boolean answered) { this.answered = answered; }

    /**
     * Возвращает ответ пользователя.
     *
     * @return Ответ пользователя или null, если ответ не был дан
     */
    public String getUserAnswer() { return userAnswer; }

    /**
     * Устанавливает ответ пользователя.
     * Автоматически устанавливает флаг answered в true, если userAnswer не null.
     *
     * @param userAnswer Ответ пользователя
     */
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.answered = (userAnswer != null);
    }

    /**
     * Проверяет правильность ответа по индексу.
     *
     * @param index Индекс ответа для проверки (начинается с 0)
     * @return true - если индекс соответствует правильному ответу, false - в противном случае
     */
    public boolean validAnswer(int index) {
        return index == answer;
    }

    /**
     * Проверяет, является ли объект пустым.
     *
     * @return true - если вопрос, первый вариант ответа и правильный ответ пусты,
     *         false - в противном случае
     */
    public boolean isEmpty() {
        boolean questionEmpty = question == null || question.isEmpty();
        boolean firstOptionEmpty = options == null || options.length == 0 || options[0] == null || options[0].isEmpty();
        boolean answerEmpty = answer == 0;

        return questionEmpty && firstOptionEmpty && answerEmpty;
    }

    /**
     * Проверяет, ответил ли пользователь на вопрос.
     *
     * @return true - если пользователь дал ответ, false - в противном случае
     */
    public boolean isAnswered() {
        return userAnswer != null && !userAnswer.isEmpty();
    }
}