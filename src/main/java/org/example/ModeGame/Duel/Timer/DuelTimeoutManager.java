package org.example.ModeGame.Duel.Timer;

import org.example.ModeGame.Duel.DuelMatchmaker;
import org.example.ModeGame.Duel.DuelPair;

import org.example.ModeGame.DuelMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Класс для управления таймаутами дуэлей и поиска оппонентов.
 * Использует Timer для планирования задач таймаута.
 */
public class DuelTimeoutManager {
    private final Map<String, Timer> timeoutTimers = new HashMap<>();
    private final DuelMatchmaker matchmaker;

    private final Map<String, SearchInfo> searchInfoMap = new HashMap<>();

    private TimeoutNotifier notifier;

    /**
     * Инициализирует ссылку на DuelMatchmaker.
     */
    public DuelTimeoutManager(DuelMatchmaker duelMatchmaker) {
        this.matchmaker = duelMatchmaker;
    }

    /**
     * Устанавливает нотифаер для получения уведомлений о таймаутах.
     * Нотифаер используется для отправки сообщений пользователям
     * при истечении времени ожидания.
     *
     * @param notifier объект, реализующий интерфейс TimeoutNotifier
     */
    public void setNotifier(TimeoutNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Запускает таймер таймаута для поиска оппонента.
     * Используется, когда игрок ожидает подбора противника.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @param timeoutMillis время таймаута в миллисекундах (рекомендуется 120000 = 2 минуты)
     * @param topicType тип темы (enum TopicType)
     * @param topicValue значение темы (название файла для local, запрос для generated)
     */
    public void startSearchTimeout(String chatId, long timeoutMillis,
                                   DuelMode.TopicType topicType, String topicValue) {
        stopTimeout(chatId);

        searchInfoMap.put(chatId, new SearchInfo(topicType.toString(), topicValue));

        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[TIMEOUT_MANAGER] Таймаут поиска для игрока: " + chatId);

                // Получаем сохраненную информацию о поиске
                SearchInfo info = searchInfoMap.remove(chatId);
                if (info != null) {
                    // Преобразуем строку обратно в TopicType
                    DuelMode.TopicType topicTypeFromString =
                            DuelMode.TopicType.valueOf(info.getTopicType().toUpperCase());

                    // отменяем поиск с известной темой
                    matchmaker.cancelSearch(chatId, topicTypeFromString, info.getTopicValue());
                    System.out.println("[TIMEOUT_MANAGER] Поиск отменен для " + chatId +
                            ", тема: " + info.getTopicType() + ":" + info.getTopicValue());

                    // ВЫЗЫВАЕМ НОТИФИКАЦИЮ
                    if (notifier != null) {
                        notifier.notifySearchTimeout(chatId, info.getTopicValue());
                        System.out.println("[TIMEOUT_MANAGER] Вызвана нотификация поиска для " + chatId);
                    }
                } else {
                    // Если информация потеряна, отменяем все
                    matchmaker.cancelSearch(chatId, DuelMode.TopicType.LOCAL, "");
                    matchmaker.cancelSearch(chatId, DuelMode.TopicType.GENERATED, "general");
                    System.out.println("[TIMEOUT_MANAGER] Поиск отменен (без информации о теме) для " + chatId);

                    // ВЫЗЫВАЕМ НОТИФИКАЦИЮ С ОБЩЕЙ ТЕМОЙ
                    if (notifier != null) {
                        notifier.notifySearchTimeout(chatId, "неизвестная тема");
                    }
                }

                timeoutTimers.remove(chatId);
            }
        };

        timeoutTimers.put(chatId, timer);
        timer.schedule(task, timeoutMillis);
        System.out.println("[TIMEOUT_MANAGER] Запущен таймер поиска для " + chatId +
                " на " + timeoutMillis + "мс, тема: " + topicType + ":" + topicValue);
    }

    /**
     * Запускает таймер ожидания второго игрока в дуэли.
     * Используется, когда первый игрок уже завершил дуэль, а второй еще нет.
     *
     * @param duelId уникальный идентификатор дуэли
     * @param chatId1 ID первого игрока (уже завершил дуэль)
     * @param chatId2 ID второго игрока (еще не завершил дуэль)
     * @param timeoutMillis время таймаута в миллисекундах (рекомендуется 120000 = 2 минуты)
     */
    public void startDuelTimeout(String duelId, String chatId1, String chatId2, long timeoutMillis) {
        stopTimeout(duelId);

        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[TIMEOUT_MANAGER] Таймаут дуэли: " + duelId);

                // Получаем пару
                DuelPair pair = matchmaker.getPairForPlayer(chatId1);
                if (pair != null) {
                    pair.markAsTimedOut();
                    System.out.println("[TIMEOUT_MANAGER] Дуэль " + duelId + " отмечена как завершенная по таймауту");

                    // ВЫЗЫВАЕМ НОТИФИКАЦИЮ ДЛЯ ДУЭЛИ
                    if (notifier != null) {
                        notifier.notifyDuelTimeout(duelId, chatId1, chatId2);
                        System.out.println("[TIMEOUT_MANAGER] Вызвана нотификация дуэли для " + duelId);
                    }
                } else {
                    System.out.println("[TIMEOUT_MANAGER] Пары не найдено для дуэли: " + duelId);
                }

                timeoutTimers.remove(duelId);
            }
        };

        timeoutTimers.put(duelId, timer);
        timer.schedule(task, timeoutMillis);
        System.out.println("[TIMEOUT_MANAGER] Запущен таймер дуэли " + duelId +
                " на " + timeoutMillis + "мс (ожидание " + chatId2 + ")");
    }

    /**
     * Останавливает таймаут для указанного ID.
     * Используется, когда:
     * 1. Игрок найден в процессе поиска
     * 2. Дуэль завершена обоими игроками
     * 3. Игрок отменяет поиск вручную
     *
     * @param id ID игрока (для поиска) или duelId (для дуэли)
     */
    public void stopTimeout(String id) {
        Timer timer = timeoutTimers.remove(id);
        if (timer != null) {
            timer.cancel();
            System.out.println("[TIMEOUT_MANAGER] Остановлен таймер для: " + id);
        }

        // Удаляем информацию о поиске (если это поиск)
        searchInfoMap.remove(id);
    }

    /**
     * Проверяет, есть ли активный таймаут для указанного ID.
     * Может использоваться для проверки как поиска, так и дуэли.
     *
     * @param id ID игрока (для поиска) или duelId (для дуэли)
     * @return true если есть активный таймаут, false в противном случае
     */
    public boolean hasActiveTimeout(String id) {
        return timeoutTimers.containsKey(id);
    }

    /**
     * Проверяет, есть ли активный поиск для указанного игрока.
     * Используется для предотвращения повторного входа в поиск.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @return true если игрок в активном поиске, false в противном случае
     */
    public boolean hasActiveSearch(String chatId) {
        return timeoutTimers.containsKey(chatId);
    }

    /**
     * Получает информацию о поиске для указанного игрока.
     *
     * @param chatId уникальный идентификатор чата игрока
     * @return объект SearchInfo с информацией о поиске или null если поиск не найден
     */
    public SearchInfo getSearchInfo(String chatId) {
        return searchInfoMap.get(chatId);
    }

    /**
     * Удаляет информацию о поиске для указанного игрока.
     *
     * @param chatId уникальный идентификатор чата игрока
     */
    public void removeSearchInfo(String chatId) {
        searchInfoMap.remove(chatId);
    }

    /**
     * Очищает все таймеры и информацию о поиске.
     * Используется при перезапуске приложения или сбросе состояния системы.
     * Гарантирует, что не останется висящих таймеров.
     */
    public void clearAll() {
        for (Timer timer : timeoutTimers.values()) {
            timer.cancel();
        }
        timeoutTimers.clear();
        searchInfoMap.clear();
        System.out.println("[TIMEOUT_MANAGER] Все таймеры и информация очищены");
    }
}