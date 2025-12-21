package org.example.ModeGame;

import org.example.DataMessage.Content;
import org.example.ModeGame.Duel.*;
import org.example.ModeGame.Duel.Timer.DuelTimeoutManager;
import org.example.Producer;
import org.example.Quiz.UserData;

/**
 * Реализация режима дуэли между двумя игроками.
 * Позволяет игрокам соревноваться в реальном времени, отвечая на одинаковые вопросы.
 * Режим поддерживает как готовые темы из базы данных, так и генерацию тем с помощью ИИ.
 */
public class DuelMode implements ModeSelector {

    /**
     * Тип темы для дуэли.
     */
    public enum TopicType {
        LOCAL,      // соответствует строке "local"
        GENERATED   // соответствует строке "generated"
    }

    private final Producer producer;
    private final String chatId;
    private final UserData userData;
    private final DuelTimeoutManager timeoutManager;

    private static final String MATCH_FOUND_TEMPLATE =
            """
                    🎯 *Пара найдена!*
                    
                    Оппонент: %s
                    Тема: %s
                    Оба игрока получат одинаковые вопросы!
                    
                    Начинаем дуэль через 3... 2... 1...""";

    private static final String MATCH_FOUND_GENERATED_TEMPLATE =
            """
                    🎯 *Пара найдена!*
                    
                    Оппонент: %s
                    Ваша предложенная тема: %s
                    Тема оппонента: %s
                    🎲 Выбранная тема: %s
                    Оба игрока получат одинаковые вопросы!
                    
                    Начинаем дуэль через 3... 2... 1...""";

    private static final String WAITING_SEARCH_TEMPLATE =
            """
                    ⏳ *Ищем оппонента...*
                    
                    %s\
                    Ожидающих: %d
                    
                    Ожидайте подбора противника...
                    ⏰ *Поиск автоматически отменится через 2 минуты*""";

    private static final String SEARCH_TIMEOUT_TEMPLATE =
            """
                    ⏰ *Поиск оппонента отменен по таймауту*
                    
                    Не удалось найти оппонента в течение 2 минут.
                    🎯 Тема: %s
                    
                    Попробуйте поискать чуть позже или выберите другую тему.""";

    private static final String DUEL_OPPONENT_TIMEOUT_TEMPLATE =
            """
                    🏆 *ПРОТИВНИК НЕ УСПЕЛ!*
                    
                    Ваш оппонент (%s) не завершил дуэль в течение 2-х минут.
                    Ваши результаты:
                    • Правильных ответов: %d/5
                    • Время: %.1f сек
                    
                    ⚔️ *Вы получаете победу по умолчанию!*
                    🏆 *Начислено баллов в общий счет: %d*""";

    private static final String DUEL_YOU_TIMEOUT_TEMPLATE =
            """
                    💀 *ВЫ НЕ УСПЕЛИ!*
                    
                    Вы не завершили дуэль в течение 2-х минут.
                    Ваш оппонент (%s) уже давно закончил и получил победу.
                    
                    😞 *Вы получаете 0 баллов за эту дуэль*
                    📉 *В следующий раз отвечайте быстрее!*""";


    private static final String LOCAL_TOPIC_INFO = "Тема: %s\nЖдем игрока с такой же темой...\n";

    private static final String GENERATED_TOPIC_INFO = "Запрос темы: %s\nЖдем игрока для генерации темы...\n";

    private static final String DUEL_RESULTS_TEMPLATE =
            """
                    ⚔️ *РЕЗУЛЬТАТЫ ДУЭЛИ*
                    
                    Ваши результаты:
                    • Правильных ответов: %d/5
                    • Время: %.1f сек
                    
                    Результаты %s:
                    • Правильных ответов: %d/5
                    • Время: %.1f сек
                    
                    %s
                    
                    🏆 *Начислено баллов в общий счет: %d*""";

    private static final String DUEL_COMPLETED_TEMPLATE =
            """
                    ⏳ *Вы завершили дуэль!*
                    
                    Ваши результаты:
                    • Правильных ответов: %d/5
                    • Время: %.1f сек
                    
                    Ожидаем завершения оппонента...
                    Как только %s закончит, вы получите результаты.
                    ⏰ *Если оппонент не завершит в течение 2-х минут, дуэль завершится автоматически*""";

    /**
     * Конструктор для создания режима дуэли.
     *
     * @param producer объект Producer для взаимодействия с бизнес-логикой приложения
     * @param chatId уникальный идентификатор чата пользователя
     * @param userData данные пользователя, содержащие текущее состояние и историю
     */
    public DuelMode(Producer producer, String chatId, UserData userData) {
        this.producer = producer;
        this.chatId = chatId;
        this.userData = userData;
        this.timeoutManager = producer.getDuelTimeoutManager();
    }

    /**
     * Обрабатывает выбор пользователем режима "Дуэль".
     * Устанавливает соответствующий игровой режим в данных пользователя
     * и возвращает информационное сообщение о правилах дуэли.
     *
     * @return массив объектов {@link Content}, содержащий сообщение об активации режима
     *         и меню выбора действия
     */
    @Override
    public Content[] handleModeSelection() {
        userData.setGameMode("duel");
        userData.setState("menu");

        return new Content[] {
                new Content(true, chatId,
                        """
                                ⚔️ *Режим Дуэль активирован*
                                
                                Соперник определяется по выбранной теме:
                                • Для готовых тем: ищем игрока с такой же темой
                                • Для генерации: тема выбирается случайно из предложений обоих игроков
                                
                                ⚠️ *Оба игрока получат одинаковые вопросы для честной дуэли!*"""),
                producer.handleMenuCommand(chatId, userData)[0]
        };
    }

    /**
     * Начинает поиск противника по локальной теме из базы данных.
     *
     * @param topicName название темы из базы данных (например, "programming", "history")
     * @return массив объектов {@link Content} с результатом поиска:
     *         - сообщение о найденном оппоненте или начале ожидания
     *         - соответствующий интерфейс (кнопки)
     */
    public Content[] startLocalDuelSearch(String topicName) {
        try {
            String playerName = getPlayerName();
            DuelMatchmaker matchmaker = producer.getDuelMatchmaker();
            DuelPair pair = matchmaker.registerForDuel(
                    chatId,
                    TopicType.LOCAL,
                    topicName,
                    playerName,
                    topicName
            );

            if (pair != null) {
                return handleLocalMatchFound(pair, topicName);
            } else {
                return handleWaitingInQueue(TopicType.LOCAL, topicName);
            }
        }catch (IllegalStateException e) {
            return new Content[] {
                    new Content(true, chatId, "❌ " + e.getMessage() + "\n\n Имя не найдено в лидерборде")
            };
        }
    }

    /**
     * Начинает поиск противника для дуэли с генерация тем ИИ.
     * Пользователь вводит тему, которая будет использована для генерации вопросов.
     *
     * @param topicRequest запрашиваемая пользователем тема для генерации
     * @return массив объектов {@link Content} с результатом поиска:
     *         - сообщение о найденном оппоненте или начале ожидания
     *         - соответствующий интерфейс (кнопки)
     */
    public Content[] startGeneratedDuelSearch(String topicRequest) {
        try {
            String playerName = getPlayerName();
            DuelMatchmaker matchmaker = producer.getDuelMatchmaker();
            DuelPair pair = matchmaker.registerForDuel(
                    chatId,
                    TopicType.GENERATED,
                    "general",
                    playerName,
                    topicRequest
            );

            if (pair != null) {
                return handleGeneratedMatchFound(pair, topicRequest);
            } else {
                return handleWaitingInQueue(TopicType.GENERATED, topicRequest);
            }
        }catch (IllegalStateException e) {
            return new Content[] {
                    new Content(true, chatId, "❌ " + e.getMessage() + "\n\n Имя не найдено в лидерборде")
            };
        }
    }

    /**
     * Обрабатывает ситуацию, когда найдена пара для дуэли с локальной темой.
     *
     * @param pair объект {@link DuelPair}, содержащий информацию о найденной паре
     * @param topic название темы дуэли
     * @return массив объектов {@link Content} для обоих игроков:
     *         - уведомление о найденной паре
     *         - сообщения с началом дуэли
     */
    private Content[] handleLocalMatchFound(DuelPair pair, String topic) {
        String opponentName = pair.getOpponentName(chatId);
        String opponentChatId = pair.getOpponentChatId(chatId);
        String duelTopic = pair.getTopic();

        userData.setDuelTopic(duelTopic);
        userData.setState("duel_matched");

        // Останавливаем таймер поиска
        timeoutManager.stopTimeout(chatId);

        // Сообщение для текущего игрока
        String currentPlayerMessageText = String.format(MATCH_FOUND_TEMPLATE, opponentName, duelTopic);
        Content currentPlayerMessage = new Content(true, chatId, currentPlayerMessageText);

        // Сообщение для оппонента (который ждал в очереди)
        String currentPlayerName = getPlayerName();
        String opponentMessageText = String.format(MATCH_FOUND_TEMPLATE, currentPlayerName, duelTopic);
        Content opponentMessage = new Content(true, opponentChatId, opponentMessageText);

        // Сразу запускаем дуэль для обоих игроков
        UserData opponentData = producer.getUserData(opponentChatId);

        // Получаем сообщения дуэли для обоих игроков
        Content[] duelMessages = producer.startDuelQuizWithTopicForBothPlayers(
                chatId, userData, opponentChatId, opponentData, duelTopic, pair.getDuelId()
        );

        // Объединяем ВСЕ сообщения: уведомления о найденной паре + сообщения дуэли
        Content[] allMessages = new Content[2 + duelMessages.length];

        allMessages[0] = currentPlayerMessage;
        allMessages[1] = opponentMessage;

        System.arraycopy(duelMessages, 0, allMessages, 2, duelMessages.length);

        return allMessages;
    }

    /**
     * Обрабатывает ситуацию, когда найдена пара для дуэли с генерацией темы.
     *
     * @param pair объект {@link DuelPair}, содержащий информацию о найденной паре
     * @param myTopicRequest тема, предложенная текущим игроком
     * @return массив объектов {@link Content} для обоих игроков:
     *         - уведомление о найденной паре с деталями тем
     *         - сообщения с началом дуэли
     */
    private Content[] handleGeneratedMatchFound(DuelPair pair, String myTopicRequest) {
        String opponentName = pair.getOpponentName(chatId);
        String opponentChatId = pair.getOpponentChatId(chatId);
        String finalTopic = pair.getTopic();
        String opponentTopic = pair.getOpponentTopicRequest(chatId);

        userData.setDuelTopic(finalTopic);
        userData.setState("duel_matched");

        timeoutManager.stopTimeout(chatId);

        // Сообщение для текущего игрока
        String currentPlayerMessageText = String.format(MATCH_FOUND_GENERATED_TEMPLATE,
                opponentName, myTopicRequest, opponentTopic, finalTopic);
        Content currentPlayerMessage = new Content(true, chatId, currentPlayerMessageText);

        // Сообщение для оппонента (который ждал в очереди)
        String currentPlayerName = getPlayerName();
        String opponentMessageText = String.format(MATCH_FOUND_GENERATED_TEMPLATE,
                currentPlayerName, opponentTopic, myTopicRequest, finalTopic);
        Content opponentMessage = new Content(true, opponentChatId, opponentMessageText);

        // Сразу запускаем дуэль для обоих игроков
        UserData opponentData = producer.getUserData(opponentChatId);

        // Получаем сообщения дуэли для обоих игроков
        Content[] duelMessages = producer.startDuelQuizGenerationForBothPlayers(
                chatId, userData, opponentChatId, opponentData, finalTopic, pair.getDuelId()
        );

        // Объединяем ВСЕ сообщения
        Content[] allMessages = new Content[2 + duelMessages.length];

        allMessages[0] = currentPlayerMessage;
        allMessages[1] = opponentMessage;

        System.arraycopy(duelMessages, 0, allMessages, 2, duelMessages.length);

        return allMessages;
    }

    /**
     * Обрабатывает ситуацию, когда игрок помещается в очередь ожидания.
     */
    private Content[] handleWaitingInQueue(TopicType topicType, String topicValue) {
        DuelMatchmaker matchmaker = producer.getDuelMatchmaker();
        int waitingCount = matchmaker.getWaitingCount(topicType,
                topicType == TopicType.LOCAL ? topicValue : "general");
        userData.setState("duel_searching");

        // Запускаем таймер поиска (2 минуты) с указанием темы
        timeoutManager.startSearchTimeout(chatId, 120000, topicType,
                topicType == TopicType.LOCAL ? topicValue : "general");

        String topicInfo;
        if (topicType == TopicType.LOCAL) {
            topicInfo = String.format(LOCAL_TOPIC_INFO, topicValue);
        } else {
            topicInfo = String.format(GENERATED_TOPIC_INFO, topicValue);
        }

        String message = String.format(WAITING_SEARCH_TEMPLATE, topicInfo, waitingCount);

        return new Content[] {
                new Content(true, chatId, message)
        };
    }

    /**
     * Отменяет поиск дуэли и возвращает пользователя в меню.
     */
    public Content[] cancelDuelSearch() {
        DuelMatchmaker matchmaker = producer.getDuelMatchmaker();
        String currentTopic = userData.getTopicSelection();

        if (currentTopic != null && !currentTopic.isEmpty()) {
            matchmaker.cancelSearch(chatId, TopicType.LOCAL, currentTopic);
        }

        matchmaker.cancelSearch(chatId, TopicType.GENERATED, "general");

        timeoutManager.stopTimeout(chatId);

        userData.setState("menu");

        String topicDisplay = currentTopic != null ? currentTopic : "неизвестная тема";

        String timeoutMessage = String.format(SEARCH_TIMEOUT_TEMPLATE, topicDisplay);

        return new Content[] {
                new Content(true, chatId, timeoutMessage),
                producer.handleMenuCommand(chatId, userData)[0]
        };
    }

    /**
     * Завершает дуэль и показывает результаты.
     * Обрабатывает результаты обоих игроков и определяет победителя.
     *
     * @param playerScore количество правильных ответов текущего игрока (от 0 до 10)
     * @param playerTime время прохождения викторины текущим игроком в миллисекундах
     * @return массив объектов {@link Content} с результатами дуэли:
     *         - сообщение о результатах для обоих игроков (если оба завершили)
     *         - сообщение об ожидании оппонента (если только один игрок завершил)
     *         - сообщение о таймауте (если дуэль завершена по таймауту)
     */
    public Content[] finishDuel(int playerScore, long playerTime) {
        String duelId = userData.getDuelId();
        if (duelId == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Ошибка: данные дуэли не найдены.")
            };
        }

        DuelMatchmaker matchmaker = producer.getDuelMatchmaker();
        DuelPair pair = matchmaker.getPairForPlayer(chatId);

        if (pair == null) {
            return new Content[] {
                    new Content(true, chatId, "Дуэль уже завершена.")
            };
        }

        // Проверяем, не завершена ли дуэль по таймауту
        if (pair.isTimedOut()) {
            return handleTimeoutCompletedDuel(pair, playerScore, playerTime);
        }

        String opponentChatId = pair.getOpponentChatId(chatId);
        String opponentName = pair.getOpponentName(chatId);

        pair.savePlayerResults(chatId, playerScore, playerTime);

        System.out.println("[DUEL_MODE] Игрок " + chatId + " завершил дуэль " + duelId +
                " с результатом: " + playerScore + "/5 за " + playerTime + "мс");

        // Проверяем, первый ли это завершивший игрок
        boolean opponentCompleted = pair.hasPlayerCompleted(opponentChatId);
        if (!opponentCompleted) {
            // Это первый завершивший игрок - сохраняем время завершения
            pair.setFirstPlayerCompletionTime(chatId);
            System.out.println("[DUEL_MODE] Сохранено время завершения для первого игрока: " + chatId);
        }

        // Отмечаем игрока как завершившего дуэль в matchmaker
        boolean bothCompleted = matchmaker.markPlayerCompleted(duelId, chatId);
        System.out.println("[DUEL_MODE] bothCompleted после markPlayerCompleted: " + bothCompleted);

        // ВАЖНО: Проверяем результаты обоих игроков ДО возврата результата
        boolean hasBothResults = pair.hasBothResults();
        System.out.println("[DUEL_MODE] hasBothResults в DuelPair: " + hasBothResults);

        if (hasBothResults) {
            System.out.println("[DUEL_MODE] Оба игрока завершили дуэль - показываем результаты");
            // Оба игрока завершили - показываем результаты
            return handleBothPlayersCompleted(pair, playerScore, playerTime, duelId, matchmaker);
        } else {
            System.out.println("[DUEL_MODE] Только один игрок завершил - показываем ожидание");
            // Первый игрок завершил - показываем только ожидание
            return handleFirstPlayerCompleted(pair, playerScore, playerTime, opponentName, matchmaker);
        }
    }
    /**
     * Обрабатывает ситуацию, когда оба игрока завершили дуэли.
     * Сравнивает результаты, определяет победителя, отправляет сообщения обоим игрокам
     * и выполняет очистку ресурсов.
     *
     * @param pair объект DuelPair, содержащий информацию о дуэльной паре
     * @param playerScore количество правильных ответов текущего игрока (0-10)
     * @param playerTime время прохождения дуэли текущим игроком в миллисекундах
     * @param duelId уникальный идентификатор дуэли
     * @param matchmaker экземпляр DuelMatchmaker для управления дуэлями
     * @return массив Content с сообщениями для обоих игроков
     */
    private Content[] handleBothPlayersCompleted(DuelPair pair, int playerScore, long playerTime,
                                                 String duelId, DuelMatchmaker matchmaker) {
        System.out.println("[DUEL_MODE] Оба игрока завершили дуэль " + duelId);

        // Останавливаем таймеры
        timeoutManager.stopTimeout(duelId);    // таймер дуэли (ожидание второго игрока)
        timeoutManager.stopTimeout(chatId);    // таймер поиска (на всякий случай)

        String opponentChatId = pair.getOpponentChatId(chatId);
        String opponentName = pair.getOpponentName(chatId);

        PlayerResults opponentResults = pair.getOpponentResults(chatId);

        if (opponentResults != null && opponentResults.hasResults()) {
            int opponentScore = opponentResults.getScore();
            long opponentTime = opponentResults.getTime();

            System.out.println("[DUEL_MODE] Результаты оппонента: " + opponentScore + "/5 за " + opponentTime + "мс");

            String winnerMessage = determineWinner(playerScore, playerTime,
                    opponentScore, opponentTime, opponentName);

            String playerMessageText = String.format(DUEL_RESULTS_TEMPLATE,
                    playerScore, playerTime/1000.0,
                    opponentName, opponentScore, opponentTime/1000.0,
                    winnerMessage, playerScore);
            Content playerMessage = new Content(true, chatId, playerMessageText, "go_menu");

            String currentPlayerName = getPlayerName();
            String opponentWinnerMessage = determineWinner(opponentScore, opponentTime,
                    playerScore, playerTime, currentPlayerName);

            String opponentMessageText = String.format(DUEL_RESULTS_TEMPLATE,
                    opponentScore, opponentTime/1000.0,
                    currentPlayerName, playerScore, playerTime/1000.0,
                    opponentWinnerMessage, opponentScore);
            Content opponentMessage = new Content(true, opponentChatId, opponentMessageText, "go_menu");

            userData.clearDuelData();
            UserData opponentData = producer.getUserData(opponentChatId);
            if (opponentData != null) {
                opponentData.clearDuelData();
            }

            producer.clearDuelCache(duelId);
            matchmaker.removePairIfCompleted(duelId);

            return new Content[] { playerMessage, opponentMessage };
        } else {
            System.out.println("[DUEL_MODE] Ошибка: результаты оппонента не найдены в DuelPair");

            // Отправляем хотя бы свои результаты
            String errorMessage = "❌ Ошибка: результаты оппонента не получены.\n" +
                    "Ваш результат: " + playerScore + "/5 за " + (playerTime/1000.0) + " сек\n" +
                    "Очков начислено: " + playerScore;

            userData.clearDuelData();
            producer.clearDuelCache(duelId);
            matchmaker.removePairIfCompleted(duelId);

            return new Content[] {
                    new Content(true, chatId, errorMessage, "menu")
            };
        }
    }

    /**
     * Обрабатывает ситуацию, когда только первый игрок завершил дуэль.
     * Сохраняет результаты первого игрока и запускает таймер ожидания второго игрока.
     *
     * @param pair объект DuelPair, содержащий информацию о дуэльной паре
     * @param playerScore количество правильных ответов текущего игрока (0-10)
     * @param playerTime время прохождения дуэли текущим игроком в миллисекундах
     * @param opponentName имя оппонента для отображения в сообщении
     * @param matchmaker экземпляр DuelMatchmaker для управления дуэлями
     * @return массив Content с сообщением об ожидании оппонента
     */
    private Content[] handleFirstPlayerCompleted(DuelPair pair, int playerScore, long playerTime,
                                                 String opponentName, DuelMatchmaker matchmaker) {

        userData.setState("duel_waiting_opponent");
        String playerMessageText = String.format(DUEL_COMPLETED_TEMPLATE,
                playerScore, playerTime/1000.0, opponentName);

        System.out.println("[DUEL_MODE] Первый игрок завершил, ждем оппонента: " + pair.getOpponentChatId(chatId));

        // Запускаем таймер ожидания второго игрока (2 минуты)
        String duelId = userData.getDuelId();
        String opponentChatId = pair.getOpponentChatId(chatId);
        timeoutManager.startDuelTimeout(duelId, chatId, opponentChatId, 120000);

        return new Content[] { new Content(true, chatId, playerMessageText) };
    }

    /**
     * Обрабатывает ситуацию, когда дуэль завершена по таймауту.
     * Определяет, какой игрок опоздал, и распределяет очки соответствующим образом.
     *
     * @param pair объект DuelPair, содержащий информацию о дуэльной паре
     * @param playerScore количество правильных ответов текущего игрока (0-10)
     * @param playerTime время прохождения дуэли текущим игроком в миллисекундах
     * @return массив Content с сообщениями для обоих игроков о таймауте
     */
    private Content[] handleTimeoutCompletedDuel(DuelPair pair, int playerScore, long playerTime) {
        String opponentChatId = pair.getOpponentChatId(chatId);
        String opponentName = pair.getOpponentName(chatId);
        String currentPlayerName = getPlayerName();

        // Определяем, какой игрок завершил первый
        boolean isFirstPlayer = chatId.equals(pair.getFirstCompletedPlayerId());

        if (isFirstPlayer) {

            String playerMessageText = String.format(DUEL_OPPONENT_TIMEOUT_TEMPLATE,
                    opponentName, playerScore, playerTime/1000.0, playerScore);
            Content playerMessage = new Content(true, chatId, playerMessageText, "menu");

            // Сообщение для второго игрока (который не успел)
            String opponentMessageText = String.format(DUEL_YOU_TIMEOUT_TEMPLATE,
                    currentPlayerName);
            Content opponentMessage = new Content(true, opponentChatId, opponentMessageText, "menu");

            userData.clearDuelData();
            UserData opponentData = producer.getUserData(opponentChatId);
            if (opponentData != null) {
                opponentData.clearDuelData();
            }

            producer.clearDuelCache(pair.getDuelId());

            return new Content[] { playerMessage, opponentMessage };
        } else {
            // Это второй игрок, который опоздал - он не получает очков
            String playerMessageText = String.format(DUEL_YOU_TIMEOUT_TEMPLATE,
                    opponentName);

            userData.clearDuelData();

            return new Content[] {
                    new Content(true, chatId, playerMessageText, "menu"),
                    producer.handleMenuCommand(chatId, userData)[0]
            };
        }
    }
    /**
     * Определяет победителя дуэли на основе результатов обоих игроков.
     * Приоритет: количество правильных ответов > время прохождения.
     *
     * @param playerScore количество правильных ответов текущего игрока
     * @param playerTime время прохождения текущего игрока в миллисекундах
     * @param opponentScore количество правильных ответов оппонента
     * @param opponentTime время прохождения оппонента в миллисекундах
     * @param opponentName имя оппонента для отображения в сообщениях
     * @return текстовое сообщение с результатом дуэли (победа/поражение/ничья)
     */
    private String determineWinner(int playerScore, long playerTime,
                                   int opponentScore, long opponentTime,
                                   String opponentName) {
        if (playerScore > opponentScore) {
            return "🏆 *ПОБЕДА!* Вы выиграли дуэль!";
        } else if (playerScore < opponentScore) {
            return "💔 *ПОРАЖЕНИЕ!* " + opponentName + " выиграл дуэль!";
        } else {
            // При равном количестве правильных ответов - смотрим время
            if (playerTime < opponentTime) {
                return "🏆 *ПОБЕДА!* Вы ответили быстрее!";
            } else if (playerTime > opponentTime) {
                return "💔 *ПОРАЖЕНИЕ!* " + opponentName + " ответил быстрее!";
            } else {
                return "🤝 *НИЧЬЯ!* Полное равенство!";
            }
        }
    }

    /**
     * Получает имя игрока для отображения в дуэли.
     * Использует сохраненное имя из лидерборда.
     *
     * @return имя игрока для отображения
     * @throws IllegalStateException если имя игрока не установлено в лидерборде
     */
    private String getPlayerName() {
        String existingName = userData.getLeaderboardName();
        if (existingName != null && !existingName.isEmpty()) {
            return existingName;
        }
        throw new IllegalStateException("Имя игрока не установлено. Ошибка в лидерборде");
    }

    /**
     * Возвращает тип игрового режима.
     *
     * @return строку "duel", идентифицирующую тип режима
     */
    @Override
    public String getModeType() {
        return "duel";
    }
}