package org.example.ModeGame.Duel.Timer;


import org.example.ModeGame.DuelMode.TopicType;

/**
 * Класс для хранения информации о поиске дуэли.
 * Используется для корректной отмены поиска при таймауте.
 */
public class SearchInfo {
    private TopicType topicType;
    private String topicValue;

    /**
     * Конструктор для создания информации о поиске.
     *
     * @param topicType тип темы (TopicType.LOCAL или TopicType.GENERATED)
     * @param topicValue значение темы (название темы или запрос)
     */
    public SearchInfo(TopicType topicType, String topicValue) {
        this.topicType = topicType;
        this.topicValue = topicValue;
    }

    /**
     * Возвращает тип темы поиска.
     *
     * @return тип темы (TopicType)
     */
    public TopicType getTopicType() {
        return topicType;
    }

    /**
     * Устанавливает тип темы поиска.
     *
     * @param topicType тип темы (TopicType)
     */
    public void setTopicType(TopicType topicType) {
        this.topicType = topicType;
    }

    /**
     * Возвращает значение темы поиска.
     *
     * @return значение темы (название файла для LOCAL, запрос для GENERATED)
     */
    public String getTopicValue() {
        return topicValue;
    }

    /**
     * Устанавливает значение темы поиска.
     *
     * @param topicValue значение темы (название файла для LOCAL, запрос для GENERATED)
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
        return topicType.toString() + ":" + topicValue;
    }
}