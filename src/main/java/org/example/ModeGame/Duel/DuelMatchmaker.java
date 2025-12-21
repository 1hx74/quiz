package org.example.ModeGame.Duel;

import org.example.ModeGame.DuelMode;
import java.util.*;

/**
 * Класс для управления поиском пар для дуэли между игроками.
 * Обеспечивает подбор игроков по выбранным темам и управление активными дуэлями.
 */
public class DuelMatchmaker {

    /**
     * Карта очередей ожидания по темам.
     * Ключ: строка вида "тип_темы:значение_темы"
     * Значение: очередь игроков, ожидающих дуэль по этой теме
     */
    private final Map<String, Queue<DuelPlayer>> waitingQueues;

    /**
     * Карта активных пар дуэлей.
     * Ключ: chatId игрока
     * Значение: объект дуэльной пары
     */
    private final Map<String, DuelPair> activePairs;

    /**
     * Карта счетчиков завершивших игроков по дуэлям.
     * Ключ: duelId дуэли
     * Значение: количество игроков, завершивших дуэль (0-2)
     */
    private final Map<String, Integer> completedPlayers;

    /**
     * Инициализирует коллекции для хранения данных матчмейкера.
     */
    public DuelMatchmaker() {
        this.waitingQueues = new HashMap<>();
        this.activePairs = new HashMap<>();
        this.completedPlayers = new HashMap<>();
    }

    /**
     * Регистрирует игрока для поиска дуэли.
     * Если в очереди уже есть игрок с такой же темой, создает дуэльную пару.
     * Иначе добавляет игрока в очередь ожидания.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @param topicType тип темы (enum TopicType)
     * @param topicValue значение темы (для LOCAL - название темы, для GENERATED - "general")
     * @param playerName имя игрока для отображения
     * @param playerTopic тема, предложенная игроком (используется для дуэлей с генерацией)
     * @return объект {@link DuelPair} если пара найдена, null если игрок помещен в очередь ожидания
     */
    public DuelPair registerForDuel(String chatId, DuelMode.TopicType topicType, String topicValue,
                                    String playerName, String playerTopic) {
        String topicKey = createTopicKey(topicType, topicValue);

        DuelPlayer player = new DuelPlayer(chatId, playerName, playerTopic);

        synchronized (this) {
            Queue<DuelPlayer> queue = waitingQueues.computeIfAbsent(
                    topicKey, k -> new LinkedList<>()
            );

            if (!queue.isEmpty()) {
                DuelPlayer opponent = queue.poll();
                String opponentTopic = opponent.getTopicRequest();

                DuelPair pair = new DuelPair(player, opponent, playerTopic, opponentTopic, topicType);

                activePairs.put(player.getChatId(), pair);
                activePairs.put(opponent.getChatId(), pair);

                completedPlayers.put(pair.getDuelId(), 0);

                if (queue.isEmpty()) {
                    waitingQueues.remove(topicKey);
                }

                return pair;
            } else {
                queue.add(player);
                return null;
            }
        }
    }

    /**
     * Создает ключ для группировки игроков по теме.
     * Для типа GENERATED всегда возвращает "generated:general".
     * Для типа LOCAL возвращает "local:значение_темы".
     *
     * @param topicType тип темы (enum TopicType)
     * @param topicValue значение темы
     * @return строковый ключ для группировки в очереди ожидания
     */
    private String createTopicKey(DuelMode.TopicType topicType, String topicValue) {
        if (topicType == DuelMode.TopicType.GENERATED) {
            // Для генерации - все в одной группе
            return "generated:general";
        } else {
            // Для локальной темы - точное название
            return "local:" + topicValue.toLowerCase().trim();
        }
    }

    /**
     * Возвращает дуэльную пару для указанного игрока.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @return объект {@link DuelPair} если игрок участвует в активной дуэли, иначе null
     */
    public DuelPair getPairForPlayer(String chatId) {
        return activePairs.get(chatId);
    }

    /**
     * Находит дуэльную пару по её уникальному идентификатору.
     *
     * @param duelId уникальный идентификатор дуэли
     * @return объект {@link DuelPair} если дуэль найдена, иначе null
     */
    private DuelPair findPairByDuelId(String duelId) {
        synchronized (this) {
            for (DuelPair pair : activePairs.values()) {
                if (pair.getDuelId().equals(duelId)) {
                    return pair;
                }
            }
            return null;
        }
    }

    /**
     * Отмечает, что игрок завершил дуэль.
     * Увеличивает счетчик завершивших игроков для указанной дуэли.
     *
     * @param duelId уникальный идентификатор дуэли
     * @param chatId уникальный идентификатор чата игрока
     * @return true если оба игрока завершили дуэль, false если только один игрок завершил
     */
    public boolean markPlayerCompleted(String duelId, String chatId) {
        synchronized (this) {
            // Находим пару
            DuelPair pair = findPairByDuelId(duelId);
            if (pair == null) {
                System.out.println("[DUEL_MATCHMAKER] Пара не найдена для duelId: " + duelId);
                return false;
            }

            // Проверяем, является ли игрок участником этой дуэли
            if (!pair.containsPlayer(chatId)) {
                System.out.println("[DUEL_MATCHMAKER] Игрок " + chatId + " не найден в дуэли " + duelId);
                return false;
            }

            // Получаем текущий счетчик
            Integer count = completedPlayers.get(duelId);

            if (count == null) {
                // Первый завершивший
                completedPlayers.put(duelId, 1);
                System.out.println("[DUEL_MATCHMAKER] Первый игрок завершил: " + chatId + ", duelId: " + duelId);
                return false;
            } else if (count == 1) {
                // Второй завершивший
                completedPlayers.put(duelId, 2);
                System.out.println("[DUEL_MATCHMAKER] Второй игрок завершил: " + chatId + ", duelId: " + duelId);
                System.out.println("[DUEL_MATCHMAKER] Оба игрока завершили дуэль: " + duelId);
                return true;
            } else {
                // Уже оба завершили (count >= 2)
                System.out.println("[DUEL_MATCHMAKER] Оба игрока уже завершили дуэль: " + duelId);
                return true;
            }
        }
    }

    /**
     * Удаляет дуэльную пару, если оба игрока завершили дуэль.
     * Выполняет очистку ресурсов, связанных с завершенной дуэлью.
     *
     * @param duelId уникальный идентификатор дуэли для удаления
     */
    public void removePairIfCompleted(String duelId) {
        synchronized (this) {
            Integer count = completedPlayers.get(duelId);
            if (count != null && count >= 2) {
                // оба игрока завершили - удаляем пару
                System.out.println("[DUEL_MATCHMAKER] Удаляем завершенную дуэль: " + duelId);

                Iterator<Map.Entry<String, DuelPair>> iterator = activePairs.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, DuelPair> entry = iterator.next();
                    if (entry.getValue().getDuelId().equals(duelId)) {
                        System.out.println("[DUEL_MATCHMAKER] Удаляем игрока: " + entry.getKey() + " из дуэли: " + duelId);
                        iterator.remove();
                    }
                }
                // Удаляем из счетчика завершивших
                completedPlayers.remove(duelId);
                System.out.println("[DUEL_MATCHMAKER] Дуэль полностью очищена: " + duelId);
            }
        }
    }

    /**
     * Удаляет дуэльную пару, завершенную по таймауту.
     * Вызывается из Producer при обработке таймаута.
     *
     * @param duelId уникальный идентификатор дуэли для удаления
     */
    public void removeTimedOutPair(String duelId) {
        synchronized (this) {
            System.out.println("[DUEL_MATCHMAKER] Удаляем дуэль по таймауту: " + duelId);

            // Найдем и удалим пару из activePairs
            Iterator<Map.Entry<String, DuelPair>> iterator = activePairs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, DuelPair> entry = iterator.next();
                if (entry.getValue().getDuelId().equals(duelId)) {
                    System.out.println("[DUEL_MATCHMAKER] Удаляем игрока по таймауту: " + entry.getKey() + " из дуэли: " + duelId);
                    iterator.remove();
                }
            }

            // Удаляем из счетчика завершивших
            completedPlayers.remove(duelId);
            System.out.println("[DUEL_MATCHMAKER] Дуэль по таймауту полностью очищена: " + duelId);
        }
    }

    /**
     * Отменяет поиск дуэли для указанного игрока.
     * Удаляет игрока из очереди ожидания по указанной теме.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @param topicType тип темы (enum TopicType)
     * @param topicValue значение темы
     */
    public void cancelSearch(String chatId, DuelMode.TopicType topicType, String topicValue) {
        String topicKey = createTopicKey(topicType, topicValue);

        synchronized (this) {
            Queue<DuelPlayer> queue = waitingQueues.get(topicKey);
            if (queue != null) {
                queue.removeIf(player -> player.getChatId().equals(chatId));

                if (queue.isEmpty()) {
                    waitingQueues.remove(topicKey);
                }
            }
        }
    }

    /**
     * Проверяет, находится ли указанный игрок в поиске дуэли.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @return true если игрок находится в любой очереди ожидания, иначе false
     */
    public boolean isSearching(String chatId) {
        synchronized (this) {
            for (Queue<DuelPlayer> queue : waitingQueues.values()) {
                for (DuelPlayer player : queue) {
                    if (player.getChatId().equals(chatId)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Возвращает количество игроков, ожидающих дуэль по указанной теме.
     *
     * @param topicType тип темы (enum TopicType)
     * @param topicValue значение темы
     * @return количество игроков в очереди ожидания по указанной теме
     */
    public int getWaitingCount(DuelMode.TopicType topicType, String topicValue) {
        String topicKey = createTopicKey(topicType, topicValue);

        synchronized (this) {
            Queue<DuelPlayer> queue = waitingQueues.get(topicKey);
            return queue != null ? queue.size() : 0;
        }
    }

    /**
     * Удаляет дуэльную пару, завершенную по таймауту ожидания второго игрока.
     * Вызывается из DuelTimeoutManager при таймауте.
     *
     * @param duelId уникальный идентификатор дуэли для удаления
     */
    public void removeTimedOutDuel(String duelId) {
        synchronized (this) {
            System.out.println("[DUEL_MATCHMAKER] Удаляем дуэль по таймауту ожидания: " + duelId);

            Iterator<Map.Entry<String, DuelPair>> iterator = activePairs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, DuelPair> entry = iterator.next();
                if (entry.getValue().getDuelId().equals(duelId)) {
                    System.out.println("[DUEL_MATCHMAKER] Удаляем игрока по таймауту ожидания: " +
                            entry.getKey() + " из дуэли: " + duelId);
                    iterator.remove();
                }
            }

            completedPlayers.remove(duelId);
            System.out.println("[DUEL_MATCHMAKER] Дуэль по таймауту ожидания полностью очищена: " + duelId);
        }
    }

    /**
     * Полностью очищает все данные матчмейкера.
     * Удаляет все очереди ожидания, активные пары и счетчики завершивших игроков.
     * Используется для сброса состояния при перезапуске приложения.
     */
    public void clearAll() {
        synchronized (this) {
            waitingQueues.clear();
            activePairs.clear();
            completedPlayers.clear();
        }
    }
}