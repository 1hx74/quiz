package org.example.ModeGame.Duel;

/**
 * Класс для представления игрока в дуэли.
 * Содержит базовую информацию об игроке, необходимую для поиска пары и проведения дуэли.
 */
public class DuelPlayer {
    /**
     * Уникальный идентификатор чата игрока в мессенджере.
     * Используется для отправки сообщений и идентификации игрока в системе.
     */
    private final String chatId;

    /**
     * Имя игрока для отображения в интерфейсе.
     * Если имя не предоставлено, используется значение по умолчанию "Игрок".
     */
    private final String userName;

    /**
     * Тема, предложенная игроком для дуэли.
     * Для локальных дуэлей - название темы из базы данных.
     * Для генерации - запрошенная игроком тема для создания вопросов ИИ.
     */
    private final String topicRequest;

    /**
     * Конструктор для создания объекта игрока в дуэли.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @param userName имя игрока для отображения (если null или пустое, используется "Игрок")
     * @param topicRequest тема, предложенная игроком для дуэли
     */
    public DuelPlayer(String chatId, String userName, String topicRequest) {
        this.chatId = chatId;
        this.userName = userName != null && !userName.isEmpty() ? userName : "Игрок";
        this.topicRequest = topicRequest;
    }

    /**
     * Возвращает уникальный идентификатор чата игрока.
     *
     * @return идентификатор чата игрока
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * Возвращает имя игрока для отображения в интерфейсе.
     *
     * @return имя игрока
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Возвращает тему, предложенную игроком для дуэли.
     *
     * @return тема, запрошенная игроком
     */
    public String getTopicRequest() {
        return topicRequest;
    }

    /**
     * Возвращает строковое представление объекта игрока.
     * Используется для логирования и отладки.
     *
     * @return строковое представление игрока в формате:
     *         "DuelPlayer{chatId='...', userName='...', topicRequest='...'}"
     */
    @Override
    public String toString() {
        return "DuelPlayer{chatId='" + chatId + "', userName='" + userName +
                "', topicRequest='" + topicRequest + "'}";
    }
}