package org.example;

import org.example.ModeGame.Duel.Timer.DuelTimeoutManager;
import org.example.ModeGame.Duel.Timer.SearchInfo;
import org.example.ModeGame.Duel.Timer.TimeoutNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Timer;
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
        timeoutManager = DuelTimeoutManager.getInstance();
        timeoutManager.clearAll();
    }

    /**
     * Тестирует запуск таймера поиска оппонента.
     * Проверяет, что таймер запускается и правильно сохраняет информацию о поиске.
     */
    @Test
    public void testStartSearchTimeout() throws Exception {
        timeoutManager.startSearchTimeout("chat123", 100, "local", "programming");

        // Проверяем, что таймер активен
        boolean hasTimeout = timeoutManager.hasActiveTimeout("chat123");
        Assertions.assertTrue(hasTimeout, "Должен быть активный таймаут для chat123");

        // Проверяем, что есть активный поиск
        boolean hasSearch = timeoutManager.hasActiveSearch("chat123");
        Assertions.assertTrue(hasSearch, "Должен быть активный поиск для chat123");

        // Проверяем сохраненную информацию о поиске
        SearchInfo searchInfo = timeoutManager.getSearchInfo("chat123");
        Assertions.assertNotNull(searchInfo, "Информация о поиске должна быть сохранена");
        Assertions.assertEquals("local", searchInfo.getTopicType());
        Assertions.assertEquals("programming", searchInfo.getTopicValue());
    }

    /**
     * Тестирует остановку таймера поиска.
     * Проверяет, что таймер корректно останавливается и информация очищается.
     */
    @Test
    public void testStopTimeout() throws Exception {
        timeoutManager.startSearchTimeout("chat456", 1000, "generated", "Космос");

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
        timeoutManager.startSearchTimeout("chat789", 50, "local", "history");

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
        timeoutManager.startSearchTimeout("chat1", 10000, "local", "topic1");
        timeoutManager.startSearchTimeout("chat2", 10000, "generated", "topic2");
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
        timeoutManager.startSearchTimeout("chatA", 1000, "local", "topicA");
        timeoutManager.startSearchTimeout("chatB", 2000, "local", "topicB");
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
     */
    @Test
    public void testRestartTimeoutForSameId() throws Exception {
        // Запускаем первый таймер
        timeoutManager.startSearchTimeout("chatRestart", 10000, "local", "topicOld");

        // Сохраняем первый таймер для проверки
        Field timersField = DuelTimeoutManager.class.getDeclaredField("timeoutTimers");
        timersField.setAccessible(true);
        Map<String, Timer> timersBefore = (Map<String, Timer>) timersField.get(timeoutManager);
        Timer firstTimer = timersBefore.get("chatRestart");

        // Запускаем второй таймер для того же ID
        timeoutManager.startSearchTimeout("chatRestart", 5000, "generated", "topicNew");

        // Получаем новый таймер
        Map<String, Timer> timersAfter = (Map<String, Timer>) timersField.get(timeoutManager);
        Timer secondTimer = timersAfter.get("chatRestart");

        // Проверяем, что таймеры разные
        Assertions.assertNotSame(firstTimer, secondTimer, "Должен быть создан новый таймер");

        // Проверяем новую информацию о поиске
        SearchInfo searchInfo = timeoutManager.getSearchInfo("chatRestart");
        Assertions.assertNotNull(searchInfo);
        Assertions.assertEquals("generated", searchInfo.getTopicType());
        Assertions.assertEquals("topicNew", searchInfo.getTopicValue());
    }

    /**
     * Тестирует удаление информации о поиске.
     */
    @Test
    public void testRemoveSearchInfo() throws Exception {
        timeoutManager.startSearchTimeout("chatInfo", 10000, "local", "programming");

        // Проверяем, что информация сохранена
        SearchInfo infoBefore = timeoutManager.getSearchInfo("chatInfo");
        Assertions.assertNotNull(infoBefore);

        // Удаляем информацию
        timeoutManager.removeSearchInfo("chatInfo");

        // Проверяем, что информация удалена
        SearchInfo infoAfter = timeoutManager.getSearchInfo("chatInfo");
        Assertions.assertNull(infoAfter, "Информация о поиске должна быть удалена");

        // Таймер должен остаться активным
        Assertions.assertTrue(timeoutManager.hasActiveTimeout("chatInfo"));
    }
}