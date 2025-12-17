package org.example.ModeGame.Duel.Timer;

/**
 * Класс для хранения информации о поиске дуэли.
 * Используется для корректной отмены поиска при таймауте.
 */
public class SearchInfo {
    private String topicType;
    private String topicValue;

    /**
     * Конструктор для создания информации о поиске.
     *
     * @param topicType тип темы ("local" или "generated")
     * @param topicValue значение темы (название темы или запрос)
     */
    public SearchInfo(String topicType, String topicValue) {
        this.topicType = topicType;
        this.topicValue = topicValue;
    }

    /**
     * Возвращает тип темы поиска.
     *
     * @return тип темы ("local" для готовых тем или "generated" для генерации ИИ)
     */
    public String getTopicType() {
        return topicType;
    }

    /**
     * Устанавливает тип темы поиска.
     *
     * @param topicType тип темы ("local" или "generated")
     */
    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }

    /**
     * Возвращает значение темы поиска.
     *
     * @return значение темы (название файла для "local", запрос для "generated")
     */
    public String getTopicValue() {
        return topicValue;
    }

    /**
     * Устанавливает значение темы поиска.
     *
     * @param topicValue значение темы (название файла для "local", запрос для "generated")
     */
    public void setTopicValue(String topicValue) {
        this.topicValue = topicValue;
    }

    /**
     * Возвращает строковое представление информации о поиске.
     *
     * @return строка в формате "тип_темы:значение_темы"
     */
    @Override
    public String toString() {
        return topicType + ":" + topicValue;
    }
}