package org.example;

import org.example.ModeGame.Duel.DuelPair;
import org.example.ModeGame.Duel.DuelPlayer;
import org.example.ModeGame.Duel.PlayerResults;
import org.example.ModeGame.DuelMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тестовый класс для проверки функциональности {@link DuelPair}.
 * Тестирует хранение результатов игроков, механизм таймаутов
 * и основные операции с дуэльными парами.
 *
 * @see DuelPair
 * @see PlayerResults
 */
public class DuelPairTest {

    private DuelPlayer player1;
    private DuelPlayer player2;
    private DuelPair pair;

    /**
     * Подготовка тестового окружения перед каждым тестом.
     * Создает тестовых игроков и дуэльную пару.
     */
    @BeforeEach
    public void setUp() {
        player1 = new DuelPlayer("chat1", "Игрок1", "Тема1");
        player2 = new DuelPlayer("chat2", "Игрок2", "Тема2");
        pair = new DuelPair(player1, player2, "Тема1", "Тема2", DuelMode.TopicType.GENERATED);
    }

    /**
     * Тестирует сохранение результатов игроков.
     * Проверяет, что результаты корректно сохраняются и извлекаются.
     */
    @Test
    public void testSavePlayerResults() {
        pair.savePlayerResults("chat1", 8, 45000L);
        pair.savePlayerResults("chat2", 6, 60000L);

        PlayerResults results1 = pair.getPlayerResults("chat1");
        PlayerResults results2 = pair.getPlayerResults("chat2");

        Assertions.assertNotNull(results1);
        Assertions.assertNotNull(results2);
        Assertions.assertEquals(8, results1.getScore());
        Assertions.assertEquals(6, results2.getScore());
        Assertions.assertEquals(45000L, results1.getTime());
        Assertions.assertEquals(60000L, results2.getTime());
    }

    /**
     * Тестирует получение результатов оппонента.
     * Проверяет, что метод возвращает правильные результаты для каждого игрока.
     */
    @Test
    public void testGetOpponentResults() {
        pair.savePlayerResults("chat1", 7, 50000L);
        pair.savePlayerResults("chat2", 9, 40000L);

        PlayerResults opponentResults1 = pair.getOpponentResults("chat1");
        PlayerResults opponentResults2 = pair.getOpponentResults("chat2");

        Assertions.assertNotNull(opponentResults1);
        Assertions.assertNotNull(opponentResults2);
        Assertions.assertEquals(9, opponentResults1.getScore());
        Assertions.assertEquals(7, opponentResults2.getScore());
    }

    /**
     * Тестирует проверку завершения дуэли игроком.
     * Проверяет, что метод корректно определяет статус завершения.
     */
    @Test
    public void testHasPlayerCompleted() {
        Assertions.assertFalse(pair.hasPlayerCompleted("chat1"));
        Assertions.assertFalse(pair.hasPlayerCompleted("chat2"));

        pair.savePlayerResults("chat1", 5, 55000L);

        Assertions.assertTrue(pair.hasPlayerCompleted("chat1"));
        Assertions.assertFalse(pair.hasPlayerCompleted("chat2"));
    }

    /**
     * Тестирует проверку наличия результатов обоих игроков.
     */
    @Test
    public void testHasBothResults() {
        Assertions.assertFalse(pair.hasBothResults());

        pair.savePlayerResults("chat1", 8, 45000L);
        Assertions.assertFalse(pair.hasBothResults());

        pair.savePlayerResults("chat2", 7, 50000L);
        Assertions.assertTrue(pair.hasBothResults());
    }

    /**
     * Тестирует получение объекта оппонента.
     */
    @Test
    public void testGetOpponent() {
        DuelPlayer opponent1 = pair.getOpponent("chat1");
        DuelPlayer opponent2 = pair.getOpponent("chat2");

        Assertions.assertNotNull(opponent1);
        Assertions.assertNotNull(opponent2);
        Assertions.assertEquals(player2.getChatId(), opponent1.getChatId());
        Assertions.assertEquals(player1.getChatId(), opponent2.getChatId());
    }

    /**
     * Тестирует получение chatId и имени оппонента.
     */
    @Test
    public void testGetOpponentChatIdAndName() {
        Assertions.assertEquals("chat2", pair.getOpponentChatId("chat1"));
        Assertions.assertEquals("chat1", pair.getOpponentChatId("chat2"));
        Assertions.assertEquals("Игрок2", pair.getOpponentName("chat1"));
        Assertions.assertEquals("Игрок1", pair.getOpponentName("chat2"));
    }

    /**
     * Тестирует проверку принадлежности игрока к паре.
     */
    @Test
    public void testContainsPlayer() {
        Assertions.assertTrue(pair.containsPlayer("chat1"));
        Assertions.assertTrue(pair.containsPlayer("chat2"));
        Assertions.assertFalse(pair.containsPlayer("chat3"));
    }

    /**
     * Тестирует получение темы, предложенной оппонентом.
     */
    @Test
    public void testGetOpponentTopicRequest() {
        Assertions.assertEquals("Тема2", pair.getOpponentTopicRequest("chat1"));
        Assertions.assertEquals("Тема1", pair.getOpponentTopicRequest("chat2"));
    }

    /**
     * Тестирует установку времени завершения первым игроком.
     */
    @Test
    public void testSetFirstPlayerCompletionTime() {
        long before = System.currentTimeMillis();
        pair.setFirstPlayerCompletionTime("chat1");
        long after = System.currentTimeMillis();

        Assertions.assertEquals("chat1", pair.getFirstCompletedPlayerId());
        Assertions.assertTrue(pair.getFirstCompletionTime() >= before);
        Assertions.assertTrue(pair.getFirstCompletionTime() <= after);
    }

    /**
     * Тестирует механизм таймаутов в дуэльной паре.
     *
     * @throws InterruptedException если поток прерван во время ожидания
     */
    @Test
    public void testTimeoutMechanism() throws InterruptedException {
        pair.setFirstPlayerCompletionTime("chat1");

        Assertions.assertFalse(pair.isTimeoutExpired(), "Таймаут не должен истечь сразу");

        pair.updateLastActivityTime("chat2");

        Assertions.assertFalse(pair.isTimedOut(), "Дуэль не должна быть отмечена как завершенная по таймауту");

        pair.markAsTimedOut();

        Assertions.assertTrue(pair.isTimedOut(), "Дуэль должна быть отмечена как завершенная по таймауту");
    }

    /**
     * Тестирует проверку завершения дуэли.
     * Проверяет оба сценария: нормальное завершение и таймаут.
     */
    @Test
    public void testIsCompleted() {
        Assertions.assertFalse(pair.isCompleted());

        pair.savePlayerResults("chat1", 8, 45000L);
        Assertions.assertFalse(pair.isCompleted());

        pair.savePlayerResults("chat2", 7, 50000L);
        Assertions.assertTrue(pair.isCompleted());

        DuelPair newPair = new DuelPair(player1, player2, "Тема1", "Тема2", DuelMode.TopicType.GENERATED);
        newPair.markAsTimedOut();
        Assertions.assertTrue(newPair.isCompleted(), "Дуэль должна быть завершена по таймауту");
    }

    /**
     * Тестирует получение типа темы (enum).
     */
    @Test
    public void testGetTopicType() {
        Assertions.assertEquals(DuelMode.TopicType.GENERATED, pair.getTopicType());

        // Тест для LOCAL темы
        DuelPair localPair = new DuelPair(player1, player2, "История", "История", DuelMode.TopicType.LOCAL);
        Assertions.assertEquals(DuelMode.TopicType.LOCAL, localPair.getTopicType());
    }

    /**
     * Тестирует тему дуэли для GENERATED типа.
     */
    @Test
    public void testTopicForGeneratedType() {
        // Для GENERATED тема выбирается случайно между двумя предложенными
        String topic = pair.getTopic();
        Assertions.assertTrue("Тема1".equals(topic) || "Тема2".equals(topic));
    }

}