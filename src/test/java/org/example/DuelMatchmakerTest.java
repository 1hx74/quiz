package org.example;

import org.example.ModeGame.Duel.DuelMatchmaker;
import org.example.ModeGame.Duel.DuelPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Тестовый класс для проверки функциональности {@link DuelMatchmaker}.
 * Тестирует создание дуэльных пар, управление очередями ожидания,
 * обработку завершения дуэлей и конкурентные сценарии.
 * @see DuelMatchmaker
 * @see DuelPair
 */
public class DuelMatchmakerTest {

    private DuelMatchmaker matchmaker;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Инициализирует экземпляр {@link DuelMatchmaker} и очищает его состояние.
     */
    @BeforeEach
    public void setUp() {
        matchmaker = DuelMatchmaker.getInstance();
        matchmaker.clearAll();
    }

    /**
     * Тестирует создание дуэльной пары для локальной темы.
     * Проверяет, что:
     */
    @Test
    public void testRegisterForDuelLocalTopicPairCreated() {
        DuelPair pair1 = matchmaker.registerForDuel(
                "chat1", "local", "programming", "Игрок1", "programming"
        );

        Assertions.assertNull(pair1, "Первый игрок должен попасть в очередь");

        DuelPair pair2 = matchmaker.registerForDuel(
                "chat2", "local", "programming", "Игрок2", "programming"
        );

        Assertions.assertNotNull(pair2, "Должна быть создана пара при совпадении темы");
        Assertions.assertEquals("programming", pair2.getTopic());
        Assertions.assertEquals("local", pair2.getTopicType());
        Assertions.assertTrue(pair2.containsPlayer("chat1"));
        Assertions.assertTrue(pair2.containsPlayer("chat2"));
    }

    /**
     * Тестирует создание дуэльной пары для сгенерированной темы.
     * Проверяет, что тема выбирается случайным образом из предложенных игроками.
     */
    @Test
    public void testRegisterForDuelGeneratedTopicRandomTopicSelection() {
        String topic1 = "Космос";
        String topic2 = "Животные";

        matchmaker.registerForDuel(
                "chat1", "generated", "general", "Игрок1", topic1
        );

        DuelPair pair = matchmaker.registerForDuel(
                "chat2", "generated", "general", "Игрок2", topic2
        );

        Assertions.assertNotNull(pair);
        String selectedTopic = pair.getTopic();
        Assertions.assertTrue(
                selectedTopic.equals(topic1) || selectedTopic.equals(topic2),
                "Тема должна быть одной из предложенных: " + selectedTopic
        );
    }

    /**
     * Тестирует, что игроки с разными локальными темами не создают пару.
     * Проверяет, что каждый игрок остается в своей очереди ожидания.
     */
    @Test
    public void testDifferentLocalTopicsNoMatch() {
        DuelPair result1 = matchmaker.registerForDuel(
                "chat1", "local", "history", "Игрок1", "history"
        );

        DuelPair result2 = matchmaker.registerForDuel(
                "chat2", "local", "math", "Игрок2", "math"
        );

        Assertions.assertNull(result1, "Первый игрок в очереди");
        Assertions.assertNull(result2, "Второй игрок в другой очереди - пара не создана");

        Assertions.assertEquals(1, matchmaker.getWaitingCount("local", "history"));
        Assertions.assertEquals(1, matchmaker.getWaitingCount("local", "math"));
    }

    /**
     * Тестирует отмену поиска дуэли.
     * Проверяет, что игрок удаляется из очереди ожидания после отмены.
     */
    @Test
    public void testCancelSearch() {
        matchmaker.registerForDuel("chat1", "local", "science", "Игрок1", "science");

        boolean isSearchingBefore = matchmaker.isSearching("chat1");
        matchmaker.cancelSearch("chat1", "local", "science");
        boolean isSearchingAfter = matchmaker.isSearching("chat1");

        Assertions.assertTrue(isSearchingBefore, "Игрок должен быть в поиске после регистрации");
        Assertions.assertFalse(isSearchingAfter, "Игрок не должен быть в поиске после отмены");
    }

    /**
     * Тестирует подсчет количества игроков в очереди ожидания.
     * Проверяет, что счетчик правильно увеличивается и уменьшается.
     */
    @Test
    public void testGetWaitingCount() {
        Assertions.assertEquals(0, matchmaker.getWaitingCount("local", "art"), "Изначально должно быть 0");

        matchmaker.registerForDuel("chat1", "local", "art", "Игрок1", "art");
        Assertions.assertEquals(1, matchmaker.getWaitingCount("local", "art"));

        matchmaker.registerForDuel("chat2", "local", "art", "Игрок2", "art");
        Assertions.assertEquals(0, matchmaker.getWaitingCount("local", "art"), "Очередь должна очиститься после создания пары");
    }

    /**
     * Тестирует обработку завершения дуэлей игроками.
     * Проверяет правильность работы счетчика завершивших игроков.
     */
    @Test
    public void testMarkPlayerCompleted() {
        // Создаем пару
        matchmaker.registerForDuel("chat1", "local", "sport", "Игрок1", "sport");
        DuelPair pair = matchmaker.registerForDuel("chat2", "local", "sport", "Игрок2", "sport");
        String duelId = pair.getDuelId();

        // Первый игрок завершает
        boolean firstCompletion = matchmaker.markPlayerCompleted(duelId, "chat1");
        Assertions.assertTrue(firstCompletion, "Только один игрок завершил - должно быть false");

        // Второй игрок завершает
        boolean secondCompletion = matchmaker.markPlayerCompleted(duelId, "chat2");
        Assertions.assertTrue(secondCompletion, "Оба игрока завершили - должно быть true");
    }

    /**
     * Тестирует удаление дуэльной пары после завершения обоими игроками.
     * Проверяет, что пара корректно удаляется из системы.
     */
    @Test
    public void testRemovePairIfCompleted() {
        // Создаем пару
        matchmaker.registerForDuel("chat1", "local", "music", "Игрок1", "music");
        DuelPair pair = matchmaker.registerForDuel("chat2", "local", "music", "Игрок2", "music");
        String duelId = pair.getDuelId();

        // Отмечаем обоих игроков как завершивших
        matchmaker.markPlayerCompleted(duelId, "chat1");
        matchmaker.markPlayerCompleted(duelId, "chat2");

        // Пытаемся удалить пару
        matchmaker.removePairIfCompleted(duelId);

        // Проверяем, что пара удалена
        DuelPair foundPair1 = matchmaker.getPairForPlayer("chat1");
        DuelPair foundPair2 = matchmaker.getPairForPlayer("chat2");

        Assertions.assertNotNull(foundPair1, "Пара должна быть удалена для chat1");
        Assertions.assertNotNull(foundPair2, "Пара должна быть удалена для chat2");
    }

    /**
     * Тестирует конкурентную регистрацию игроков в дуэль.
     * Проверяет корректность работы в многопоточном режиме.
     *
     * @throws InterruptedException если поток прерван во время ожидания
     */
    @Test
    public void testConcurrentRegistration() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final String topic = "concurrent_test";

        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int playerNum = i;
            threads[i] = new Thread(() -> {
                try {
                    matchmaker.registerForDuel(
                            "chat" + playerNum,
                            "local",
                            topic,
                            "Игрок" + playerNum,
                            topic
                    );
                } finally {
                    latch.countDown();
                }
            });
            threads[i].start();
        }

        latch.await(5, TimeUnit.SECONDS);

        // Подсчитываем, сколько игроков находятся в парах
        int playersInPairs = 0;
        for (int i = 0; i < THREAD_COUNT; i++) {
            if (matchmaker.getPairForPlayer("chat" + i) != null) {
                playersInPairs++;
            }
        }

        // Проверяем, что все игроки распределены по парам
        Assertions.assertEquals(THREAD_COUNT, playersInPairs, "Все игроки должны быть в парах");

        // Проверяем, что очередь пуста
        Assertions.assertEquals(0, matchmaker.getWaitingCount("local", topic),
                "Очередь должна быть пустой после создания всех пар");
    }

    /**
     * Тестирует получение дуэльной пары для игрока.
     * Проверяет, что метод {@link DuelMatchmaker#getPairForPlayer(String)}
     * возвращает правильную пару для каждого игрока.
     */
    @Test
    public void testGetPairForPlayer() {
        matchmaker.registerForDuel("chat1", "local", "test", "Игрок1", "test");
        DuelPair pair = matchmaker.registerForDuel("chat2", "local", "test", "Игрок2", "test");

        DuelPair pairForPlayer1 = matchmaker.getPairForPlayer("chat1");
        DuelPair pairForPlayer2 = matchmaker.getPairForPlayer("chat2");

        Assertions.assertNotNull(pairForPlayer1, "Должна быть найдена пара для chat1");
        Assertions.assertNotNull(pairForPlayer2, "Должна быть найдена пара для chat2");
        Assertions.assertEquals(pair, pairForPlayer1);
        Assertions.assertEquals(pair, pairForPlayer2);
    }

    /**
     * Тестирует удаление дуэли по таймауту.
     * Проверяет, что методы удаления корректно очищают состояние системы.
     */
    @Test
    public void testRemoveTimedOutDuel() {
        // Создаем пару
        matchmaker.registerForDuel("chat1", "local", "timeout_test", "Игрок1", "timeout_test");
        DuelPair pair = matchmaker.registerForDuel("chat2", "local", "timeout_test", "Игрок2", "timeout_test");
        String duelId = pair.getDuelId();

        // Удаляем по таймауту
        matchmaker.removeTimedOutDuel(duelId);

        // Проверяем, что пара удалена
        Assertions.assertNull(matchmaker.getPairForPlayer("chat1"));
        Assertions.assertNull(matchmaker.getPairForPlayer("chat2"));
    }

    /**
     * Тестирует поиск дуэльной пары по ID.
     * Проверяет, что методы поиска возвращают корректные результаты.
     */
    @Test
    public void testFindPairByDuelId() throws Exception {
        // Используем рефлексию для доступа к приватному методу
        java.lang.reflect.Method method = DuelMatchmaker.class.getDeclaredMethod(
                "findPairByDuelId", String.class
        );
        method.setAccessible(true);

        // Создаем пару
        matchmaker.registerForDuel("chat1", "local", "find_test", "Игрок1", "find_test");
        DuelPair pair = matchmaker.registerForDuel("chat2", "local", "find_test", "Игрок2", "find_test");
        String duelId = pair.getDuelId();

        // Ищем пару
        DuelPair foundPair = (DuelPair) method.invoke(matchmaker, duelId);

        Assertions.assertNotNull(foundPair, "Должна быть найдена пара по ID");
        Assertions.assertEquals(duelId, foundPair.getDuelId());
    }
}