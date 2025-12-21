package org.example.ModeGame.Duel;

import org.example.ModeGame.DuelMode;
import java.util.UUID;
import java.util.Random;

/**
 * Класс для представления пары игроков в дуэли.
 * Хранит информацию об обоих игроках, теме дуэли и результатах.
 * Каждая дуэльная пара имеет уникальный идентификатор для отслеживания.
 */
public class DuelPair {
    private final DuelPlayer player1;
    private final DuelPlayer player2;
    private final String topic;
    private final DuelMode.TopicType topicType; //String → DuelMode.TopicType
    private final String player1Topic;
    private final String player2Topic;
    private final long createdTime;
    private final String duelId;

    private Integer player1Score = null;
    private Long player1Time = null;
    private Integer player2Score = null;
    private Long player2Time = null;

    private String firstCompletedPlayerId = null;
    private long firstCompletionTime = 0L;
    private long lastActivityTime = 0L;
    private boolean duelTimedOut = false;

    private static final Random random = new Random();

    /**
     * Конструктор для создания новой дуэльной пары.
     *
     * @param player1 первый игрок дуэли
     * @param player2 второй игрок дуэли
     * @param player1Topic тема, предложенная первым игроком
     * @param player2Topic тема, предложенная вторым игроком
     * @param topicType тип темы (enum TopicType)
     */
    public DuelPair(DuelPlayer player1, DuelPlayer player2,
                    String player1Topic, String player2Topic, DuelMode.TopicType topicType) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Topic = player1Topic;
        this.player2Topic = player2Topic;
        this.topicType = topicType;
        this.createdTime = System.currentTimeMillis();
        this.duelId = UUID.randomUUID().toString();

        // Определяем тему для дуэли
        if (topicType == DuelMode.TopicType.GENERATED) {
            // Случайный выбор между темами игроков
            this.topic = random.nextBoolean() ? player1Topic : player2Topic;
        } else {
            // Для локальной темы - тема одинаковая у обоих
            this.topic = player1Topic;
        }
    }

    /**
     * Сохраняет результаты игрока в дуэли.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @param score количество правильных ответов (от 0 до 10)
     * @param time время прохождения викторины в миллисекундах
     */
    public void savePlayerResults(String chatId, int score, long time) {
        if (player1.getChatId().equals(chatId)) {
            player1Score = score;
            player1Time = time;
            System.out.println("[DUEL_PAIR] Сохранены результаты игрока1 (" + chatId + "): " +
                    score + "/5 за " + time + "мс");
        } else if (player2.getChatId().equals(chatId)) {
            player2Score = score;
            player2Time = time;
            System.out.println("[DUEL_PAIR] Сохранены результаты игрока2 (" + chatId + "): " +
                    score + "/5 за " + time + "мс");
        } else {
            System.out.println("[DUEL_PAIR] Ошибка: игрок " + chatId + " не найден в дуэли " + duelId);
        }
    }

    /**
     * Возвращает результаты оппонента для указанного игрока.
     *
     * @param chatId уникальный идентификатор чата текущего игрока
     * @return объект {@link PlayerResults} с результатами оппонента или null, если результаты не сохранены
     */
    public PlayerResults getOpponentResults(String chatId) {
        if (player1.getChatId().equals(chatId)) {
            if (player2Score != null && player2Time != null) {
                return new PlayerResults(player2Score, player2Time);
            }
        } else if (player2.getChatId().equals(chatId)) {
            if (player1Score != null && player1Time != null) {
                return new PlayerResults(player1Score, player1Time);
            }
        }
        return new PlayerResults(); // Возвращаем пустой объект вместо null
    }

    /**
     * Получает результаты игрока по его chatId.
     *
     * @param chatId ID игрока
     * @return результаты игрока или null если не найдены
     */
    public PlayerResults getPlayerResults(String chatId) {
        if (player1.getChatId().equals(chatId)) {
            return new PlayerResults(player1Score, player1Time);
        } else if (player2.getChatId().equals(chatId)) {
            return new PlayerResults(player2Score, player2Time);
        }
        return null;
    }

    /**
     * Проверяет, завершил ли конкретный игрок дуэль.
     *
     * @param chatId ID игрока
     * @return true если игрок завершил дуэль
     */
    public boolean hasPlayerCompleted(String chatId) {
        if (player1.getChatId().equals(chatId)) {
            return player1Score != null;
        } else if (player2.getChatId().equals(chatId)) {
            return player2Score != null;
        }
        return false;
    }

    /**
     * Проверяет, сохранены ли результаты обоих игроков дуэли.
     *
     * @return true если оба игрока завершили дуэль и их результаты сохранены, иначе false
     */
    public boolean hasBothResults() {
        return player1Score != null && player2Score != null;
    }

    /**
     * Проверяет, завершена ли дуэль.
     * @return true если оба игрока завершили дуэль или дуэль завершена по таймауту
     */
    public boolean isCompleted() {
        return (player1Score != null && player2Score != null) || duelTimedOut;
    }

    /**
     * Устанавливает время завершения первого игрока.
     * Также инициализирует время последней активности.
     *
     * @param playerChatId ID игрока, который завершил первым
     */
    public void setFirstPlayerCompletionTime(String playerChatId) {
        this.firstCompletedPlayerId = playerChatId;
        this.firstCompletionTime = System.currentTimeMillis();
        this.lastActivityTime = this.firstCompletionTime;
        System.out.println("[DUEL_PAIR] Установлено время завершения для игрока " + playerChatId +
                ", время активности инициализировано: " + lastActivityTime);
    }

    /**
     * Возвращает время завершения первого игрока.
     *
     * @return время в миллисекундах, когда первый игрок завершил
     */
    public long getFirstCompletionTime() {
        return firstCompletionTime;
    }

    /**
     * Возвращает ID первого завершившего игрока.
     *
     * @return chatId первого завершившего игрока
     */
    public String getFirstCompletedPlayerId() {
        return firstCompletedPlayerId;
    }

    /**
     * Обновляет время последней активности второго игрока.
     * Вызывается при ЛЮБОМ сообщении от второго игрока.
     *
     * @param chatId ID игрока, который проявил активность
     */
    public void updateLastActivityTime(String chatId) {
        // Обновляем время только если:
        // 1. Есть первый завершивший игрок
        // 2. Это не первый игрок
        // 3. Дуэль еще не завершена по таймауту
        // 4. Оба игрока еще не завершили
        if (firstCompletedPlayerId != null &&
                !firstCompletedPlayerId.equals(chatId) &&
                !duelTimedOut &&
                !hasBothResults()) {

            this.lastActivityTime = System.currentTimeMillis();
            System.out.println("[DUEL_PAIR] Обновлено время активности для " + chatId +
                    ": " + lastActivityTime + " (дуэль: " + duelId + ")");
        }
    }

    /**
     * Проверяет, истек ли таймаут ожидания второго игрока.
     * Таймаут истекает, если прошло более 2 минут БЕЗ активности второго игрока.
     *
     * @return true если прошло более 2 минут без активности второго игрока
     */
    public boolean isTimeoutExpired() {
        // Условия для проверки таймаута:
        // 1. Есть первый завершивший игрок
        // 2. Дуэль еще не завершена по таймауту
        // 3. Оба игрока еще не завершили
        // 4. Время последней активности установлено
        if (firstCompletedPlayerId == null || duelTimedOut || hasBothResults() || lastActivityTime == 0) {
            return false;
        }

        long timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime;
        boolean expired = timeSinceLastActivity > 120000;

        if (expired) {
            System.out.println("[DUEL_PAIR] Таймаут истек для дуэли " + duelId +
                    ", бездействие: " + (timeSinceLastActivity/1000) + " секунд");
        }

        return expired;
    }

    /**
     * Отмечает дуэль как завершенную по таймауту.
     */
    public void markAsTimedOut() {
        this.duelTimedOut = true;
        System.out.println("[DUEL_PAIR] Дуэль " + duelId + " отмечена как завершенная по таймауту");
    }

    /**
     * Проверяет, завершена ли дуэль по таймауту.
     *
     * @return true если дуэль завершена по таймауту
     */
    public boolean isTimedOut() {
        return duelTimedOut;
    }

    /**
     * Возвращает время последней активности второго игрока.
     * Используется для отладки.
     *
     * @return время последней активности в миллисекундах
     */
    public long getLastActivityTime() {
        return lastActivityTime;
    }

    /**
     * Возвращает тему, предложенную оппонентом указанного игрока.
     * Используется для дуэлей с генерацией тем.
     *
     * @param chatId уникальный идентификатор чата текущего игрока
     * @return тему, предложенную оппонентом, или null если игрок не найден
     */
    public String getOpponentTopicRequest(String chatId) {
        if (player1.getChatId().equals(chatId)) {
            return player2Topic;
        } else if (player2.getChatId().equals(chatId)) {
            return player1Topic;
        }
        return null;
    }

    /**
     * Возвращает объект оппонента для указанного игрока.
     *
     * @param chatId уникальный идентификатор чата текущего игрока
     * @return объект {@link DuelPlayer} оппонента или null если игрок не найден
     */
    public DuelPlayer getOpponent(String chatId) {
        if (player1.getChatId().equals(chatId)) {
            return player2;
        } else if (player2.getChatId().equals(chatId)) {
            return player1;
        }
        return null;
    }

    /**
     * Возвращает chatId оппонента указанного игрока.
     *
     * @param chatId уникальный идентификатор чата текущего игрока
     * @return chatId оппонента или null если игрок не найден
     */
    public String getOpponentChatId(String chatId) {
        DuelPlayer opponent = getOpponent(chatId);
        return opponent != null ? opponent.getChatId() : null;
    }

    /**
     * Возвращает имя оппонента указанного игрока.
     *
     * @param chatId уникальный идентификатор чата текущего игрока
     * @return имя оппонента или null если игрок не найден
     */
    public String getOpponentName(String chatId) {
        DuelPlayer opponent = getOpponent(chatId);
        return opponent != null ? opponent.getUserName() : null;
    }

    /**
     * Проверяет, является ли указанный игрок участником этой дуэльной пары.
     *
     * @param chatId уникальный идентификатор чата для проверки
     * @return true если игрок участвует в дуэли, иначе false
     */
    public boolean containsPlayer(String chatId) {
        return player1.getChatId().equals(chatId) || player2.getChatId().equals(chatId);
    }

    /**
     * Возвращает результаты первого игрока в виде объекта PlayerResults.
     *
     * @return объект {@link PlayerResults} первого игрока
     */
    public PlayerResults getPlayer1Results() {
        return new PlayerResults(player1Score, player1Time);
    }

    /**
     * Возвращает результаты второго игрока в виде объекта PlayerResults.
     *
     * @return объект {@link PlayerResults} второго игрока
     */
    public PlayerResults getPlayer2Results() {
        return new PlayerResults(player2Score, player2Time);
    }

    // Геттеры
    public DuelPlayer getPlayer1() { return player1; }
    public DuelPlayer getPlayer2() { return player2; }
    public String getTopic() { return topic; }
    public DuelMode.TopicType getTopicType() { return topicType; }
    public long getCreatedTime() { return createdTime; }
    public String getDuelId() { return duelId; }
    public String getPlayer1Topic() { return player1Topic; }
    public String getPlayer2Topic() { return player2Topic; }

    // Геттеры для отладки
    public Integer getPlayer1Score() { return player1Score; }
    public Long getPlayer1Time() { return player1Time; }
    public Integer getPlayer2Score() { return player2Score; }
    public Long getPlayer2Time() { return player2Time; }

    /**
     * Возвращает строковое представление дуэльной пары.
     * Включает информацию об игроках, теме и результатах.
     *
     * @return строковое представление дуэльной пары
     */
    @Override
    public String toString() {
        return String.format("DuelPair{player1=%s, player2=%s, topic='%s', type='%s', duelId='%s', " +
                        "p1Score=%s, p2Score=%s, firstCompleted=%s, lastActivity=%s, timedOut=%s}",
                player1.getChatId(), player2.getChatId(), topic, topicType.toString(), duelId,
                player1Score, player2Score, firstCompletedPlayerId, lastActivityTime, duelTimedOut);
    }
}