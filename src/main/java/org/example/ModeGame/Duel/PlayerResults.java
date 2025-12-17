package org.example.ModeGame.Duel;

import java.util.Objects;

/**
 * Вспомогательный класс для хранения результатов игрока в дуэли.
 * Предоставляет иммутабельный контейнер для счета и времени прохождения.
 * Используется для передачи и хранения результатов между компонентами системы.
 */
public class PlayerResults {
    private final Integer score;
    private final Long time;

    /**
     * Конструктор для создания объекта результатов игрока.
     *
     * @param score количество правильных ответов (от 0 до 10)
     * @param time время прохождения викторины в миллисекундах
     */
    public PlayerResults(Integer score, Long time) {
        this.score = score;
        this.time = time;
    }

    /**
     * Конструктор для создания пустого объекта результатов.
     * Используется для представления отсутствующих результатов.
     */
    public PlayerResults() {
        this.score = null;
        this.time = null;
    }

    /**
     * Возвращает количество правильных ответов.
     *
     * @return счет игрока (от 0 до 10), или 0 если счет не установлен
     */
    public Integer getScore() {
        return score != null ? score : 0;
    }

    /**
     * Возвращает время прохождения викторины.
     *
     * @return время в миллисекундах, или 0 если время не установлено
     */
    public Long getTime() {
        return time != null ? time : 0L;
    }

    /**
     * Возвращает исходное значение счета без обработки null.
     *
     * @return исходное значение счета или null
     */
    public Integer getRawScore() {
        return score;
    }

    /**
     * Возвращает исходное значение времени без обработки null.
     *
     * @return исходное значение времени или null
     */
    public Long getRawTime() {
        return time;
    }

    /**
     * Проверяет, содержатся ли результаты в этом объекте.
     *
     * @return true если и счет и время не равны null, иначе false
     */
    public boolean hasResults() {
        return score != null && time != null;
    }

    /**
     * Проверяет, являются ли результаты пустыми.
     *
     * @return true если и счет и время равны null, иначе false
     */
    public boolean isEmpty() {
        return score == null && time == null;
    }

    /**
     * Создает строковое представление времени в удобном формате.
     *
     * @return строка с временем в секундах с одной десятичной цифрой
     */
    public String getFormattedTime() {
        if (time == null) {
            return "N/A";
        }
        return String.format("%.1f сек", time / 1000.0);
    }

    /**
     * Возвращает строковое представление результатов игрока.
     *
     * @return строка в формате "PlayerResults{score=X, time=Y}" или "PlayerResults{empty}"
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "PlayerResults{empty}";
        }
        return String.format("PlayerResults{score=%s, time=%s}", score, time);
    }

    /**
     * Сравнивает этот объект результатов с другим объектом.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerResults that = (PlayerResults) o;
        return Objects.equals(score, that.score) && Objects.equals(time, that.time);
    }

    /**
     * Возвращает хэш-код объекта результатов.
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(score, time);
    }
}