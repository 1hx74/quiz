package org.example;

import org.example.ModeGame.Duel.DuelMatchmaker;
import org.example.ModeGame.Duel.Timer.DuelTimeoutManager;
import org.example.ModeGame.Duel.Timer.SearchInfo;
import org.example.ModeGame.Duel.Timer.TimeoutNotifier;
import org.example.ModeGame.DuelMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Тестовый класс для проверки функциональности {@link DuelTimeoutManager}.
 * Тестирует управление таймаутами поиска оппонентов и дуэлей,
 * корректное срабатывание таймеров и очистку ресурсов.
 */
public class DuelTimeoutManagerTest {

    private DuelTimeoutManager timeoutManager;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Получает экземпляр DuelTimeoutManager и очищает его состояние.
     */
    @BeforeEach
    public void setUp() {
        DuelMatchmaker matchmaker = new DuelMatchmaker();
        timeoutManager = new DuelTimeoutManager(matchmaker);
        timeoutManager.clearAll();
    }

    /**
     * Тестирует запуск таймера поиска оппонента.
     * Проверяет, что таймер запускается и правильно сохраняет информацию о поиске.
     */
    @Test
    public void testStartSearchTimeout() throws Exception {
        timeoutManager.startSearchTimeout("chat123", 100, DuelMode.TopicType.LOCAL, "programming");

        // Проверяем, что таймер активен
        boolean hasTimeout = timeoutManager.hasActiveTimeout("chat123");
        Assertions.assertTrue(hasTimeout, "Должен быть активный таймаут для chat123");

        // Проверяем, что есть активный поиск
        boolean hasSearch = timeoutManager.hasActiveSearch("chat123");
        Assertions.assertTrue(hasSearch, "Должен быть активный поиск для chat123");

        // Проверяем сохраненную информацию о поиске
        SearchInfo searchInfo = timeoutManager.getSearchInfo("chat123");
        Assertions.assertNotNull(searchInfo, "Информация о поиске должна быть сохранена");
        Assertions.assertEquals("LOCAL", searchInfo.getTopicType());
        Assertions.assertEquals("programming", searchInfo.getTopicValue());
    }

    /**
     * Тестирует остановку таймера поиска.
     * Проверяет, что таймер корректно останавливается и информация очищается.
     */
    @Test
    public void testStopTimeout() throws Exception {
        timeoutManager.startSearchTimeout("chat456", 1000, DuelMode.TopicType.GENERATED, "Космос");

        // Проверяем, что таймер запущен
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chat456"));

        // Останавливаем таймер
        timeoutManager.stopTimeout("chat456");

        // Проверяем, что таймер остановлен
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("chat456"), "Таймаут должен быть остановлен");
        Assertions.assertFalse(timeoutManager.hasActiveSearch("chat456"), "Поиск должен быть остановлен");

        // Проверяем, что информация о поиске удалена
        SearchInfo searchInfo = timeoutManager.getSearchInfo("chat456");
        Assertions.assertNull(searchInfo, "Информация о поиске должна быть удалена");
    }

    /**
     * Тестирует корректное срабатывание таймера поиска через указанное время.
     */
    @Test
    public void testSearchTimeoutExpires() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] timeoutChatId = new String[1];

        // Создаем мок-нотификатор для отслеживания вызовов
        TimeoutNotifier mockNotifier = new TimeoutNotifier() {
            @Override
            public void notifySearchTimeout(String chatId, String topic) {
                timeoutChatId[0] = chatId;
                latch.countDown();
            }

            @Override
            public void notifyDuelTimeout(String duelId, String player1ChatId, String player2ChatId) {
                // Не используется в этом тесте
            }
        };

        timeoutManager.setNotifier(mockNotifier);

        // Запускаем короткий таймаут (50 мс)
        timeoutManager.startSearchTimeout("chat789", 50, DuelMode.TopicType.LOCAL, "history");

        // Ждем срабатывания таймаута
        boolean timeoutTriggered = latch.await(200, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(timeoutTriggered, "Таймаут должен сработать в течение 200 мс");
        Assertions.assertEquals("chat789", timeoutChatId[0], "Таймаут должен сработать для chat789");
    }

    /**
     * Тестирует запуск таймера дуэли для ожидания второго игрока.
     */
    @Test
    public void testStartDuelTimeout() throws Exception {
        timeoutManager.startDuelTimeout("duel123", "player1", "player2", 1000);

        // Проверяем, что таймер дуэли активен
        boolean hasTimeout = timeoutManager.hasActiveTimeout("duel123");
        Assertions.assertTrue(hasTimeout, "Должен быть активный таймаут дуэли для duel123");
    }

    /**
     * Тестирует очистку всех таймеров.
     */
    @Test
    public void testClearAll() throws Exception {
        // Запускаем несколько таймеров
        timeoutManager.startSearchTimeout("chat1", 10000, DuelMode.TopicType.LOCAL, "topic1");
        timeoutManager.startSearchTimeout("chat2", 10000, DuelMode.TopicType.GENERATED, "topic2");
        timeoutManager.startDuelTimeout("duel1", "player1", "player2", 10000);

        // Проверяем, что все таймеры запущены
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chat1"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chat2"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("duel1"));

        // Очищаем все
        timeoutManager.clearAll();

        // Проверяем, что все таймеры очищены
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("chat1"));
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("chat2"));
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("duel1"));
    }

    /**
     * Тестирует работу с несколькими таймерами одновременно.
     */
    @Test
    public void testMultipleTimers() throws Exception {
        // Запускаем три разных таймера
        timeoutManager.startSearchTimeout("chatA", 1000, DuelMode.TopicType.LOCAL, "topicA");
        timeoutManager.startSearchTimeout("chatB", 2000, DuelMode.TopicType.LOCAL, "topicB");
        timeoutManager.startDuelTimeout("duelX", "playerX", "playerY", 1500);

        // Проверяем, что все активны
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chatA"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chatB"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("duelX"));

        // Останавливаем один таймер
        timeoutManager.stopTimeout("chatA");

        // Проверяем, что только chatA остановлен
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("chatA"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chatB"));
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("duelX"));

        // Останавливаем остальные
        timeoutManager.stopTimeout("chatB");
        timeoutManager.stopTimeout("duelX");

        Assertions.assertFalse(timeoutManager.hasActiveTimeout("chatB"));
        Assertions.assertFalse(timeoutManager.hasActiveTimeout("duelX"));
    }

    /**
     * Тестирует повторный запуск таймера для одного и того же ID.
     * Проверяет, что старый таймер останавливается перед запуском нового.
     * Использованно мокирование
     */
    @Test
    public void testRestartTimeoutForSameId() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] lastNotifiedChatId = new String[1];
        final String[] lastNotifiedTopic = new String[1];

        // Создаем мок-таймаутнофитиер, который запоминает последний вызов
        TimeoutNotifier mockNotifier = new TimeoutNotifier() {
            @Override
            public void notifySearchTimeout(String chatId, String topic) {
                lastNotifiedChatId[0] = chatId;
                lastNotifiedTopic[0] = topic;
                latch.countDown();
            }

            @Override
            public void notifyDuelTimeout(String duelId, String player1ChatId, String player2ChatId) {
                // Не используется в этом тесте
            }
        };

        timeoutManager.setNotifier(mockNotifier);

        // Запускаем первый таймер с большим временем (не должен сработать)
        timeoutManager.startSearchTimeout("chatRestart", 5000, DuelMode.TopicType.LOCAL, "topicOld");

        // Проверяем, что информация о поиске установлена
        SearchInfo info1 = timeoutManager.getSearchInfo("chatRestart");
        Assertions.assertNotNull(info1);
        Assertions.assertEquals("LOCAL", info1.getTopicType());
        Assertions.assertEquals("topicOld", info1.getTopicValue());

        // Немного ждем
        Thread.sleep(50);

        // Перезапускаем таймер с коротким временем (должен сработать)
        timeoutManager.startSearchTimeout("chatRestart", 50, DuelMode.TopicType.GENERATED, "topicNew");

        // Проверяем, что информация о поиске обновилась
        SearchInfo info2 = timeoutManager.getSearchInfo("chatRestart");
        Assertions.assertNotNull(info2);
        Assertions.assertEquals("GENERATED", info2.getTopicType());
        Assertions.assertEquals("topicNew", info2.getTopicValue());

        // Ждем срабатывания таймаута
        boolean timeoutTriggered = latch.await(200, TimeUnit.MILLISECONDS);

        // Проверяем, что таймаут сработал
        Assertions.assertTrue(timeoutTriggered, "Таймаут должен сработать");
        Assertions.assertEquals("chatRestart", lastNotifiedChatId[0], "Таймаут должен сработать для chatRestart");
        Assertions.assertEquals("topicNew", lastNotifiedTopic[0], "Должна быть новая тема");

        // Проверяем, что после срабатывания таймаута поиск неактивен
        System.out.println(timeoutManager.hasActiveSearch("chatRestart"));
        Assertions.assertFalse(timeoutManager.hasActiveSearch("chatRestart"), "Поиск должен быть неактивен после таймаута");
    }

    /**
     * Тестирует удаление информации о поиске.
     */
    @Test
    public void testRemoveSearchInfo() throws Exception {
        timeoutManager.startSearchTimeout("chatInfo", 10000, DuelMode.TopicType.LOCAL, "programming");

        // Проверяем, что информация сохранена
        SearchInfo infoBefore = timeoutManager.getSearchInfo("chatInfo");
        Assertions.assertNotNull(infoBefore);
        Assertions.assertEquals("LOCAL", infoBefore.getTopicType());

        // Удаляем информацию
        timeoutManager.removeSearchInfo("chatInfo");

        // Проверяем, что информация удалена
        SearchInfo infoAfter = timeoutManager.getSearchInfo("chatInfo");
        Assertions.assertNull(infoAfter, "Информация о поиске должна быть удалена");

        // Таймер должен остаться активным
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chatInfo"));
    }
}