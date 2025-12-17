package org.example;

import org.example.ModeGame.Duel.PlayerResults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тестовый класс для проверки функциональности {@link PlayerResults}.
 * Тестирует хранение, сравнение и форматирование результатов игроков.
 *
 * @see PlayerResults
 */
public class PlayerResultsTest {

    /**
     * Тестирует создание объекта результатов с заданными значениями.
     */
    @Test
    public void testPlayerResultsWithValues() {
        PlayerResults results = new PlayerResults(8, 45000L);

        Assertions.assertEquals(8, results.getScore());
        Assertions.assertEquals(45000L, results.getTime());
        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertEquals("45,0 сек", results.getFormattedTime());
    }

    /**
     * Тестирует создание пустого объекта результатов.
     */
    @Test
    public void testEmptyPlayerResults() {
        PlayerResults results = new PlayerResults();

        Assertions.assertEquals(0, results.getScore());
        Assertions.assertEquals(0L, results.getTime());
        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.isEmpty());
        Assertions.assertEquals("N/A", results.getFormattedTime());
    }

    /**
     * Тестирует получение исходных (необработанных) значений.
     */
    @Test
    public void testRawValues() {
        PlayerResults results = new PlayerResults(7, 55000L);

        Assertions.assertEquals(Integer.valueOf(7), results.getRawScore());
        Assertions.assertEquals(Long.valueOf(55000L), results.getRawTime());
    }

    /**
     * Тестирует сравнение объектов результатов.
     */
    @Test
    public void testEqualsAndHashCode() {
        PlayerResults results1 = new PlayerResults(8, 45000L);
        PlayerResults results2 = new PlayerResults(8, 45000L);
        PlayerResults results3 = new PlayerResults(7, 45000L);

        Assertions.assertEquals(results1, results2);
        Assertions.assertNotEquals(results1, results3);
        Assertions.assertEquals(results1.hashCode(), results2.hashCode());
    }

    /**
     * Тестирует строковое представление объекта результатов.
     */
    @Test
    public void testToString() {
        PlayerResults results = new PlayerResults(9, 30000L);
        String str = results.toString();

        Assertions.assertTrue(str.contains("PlayerResults"));
        Assertions.assertTrue(str.contains("score=9"));
        Assertions.assertTrue(str.contains("time=30000"));
    }

    /**
     * Тестирует граничные значения результатов.
     */
    @Test
    public void testBoundaryValues() {
        // Минимальные значения
        PlayerResults minResults = new PlayerResults(0, 0L);
        Assertions.assertEquals(0, minResults.getScore());
        Assertions.assertEquals(0L, minResults.getTime());

        // Максимальные значения
        PlayerResults maxResults = new PlayerResults(10, 999999L);
        Assertions.assertEquals(10, maxResults.getScore());
        Assertions.assertEquals(999999L, maxResults.getTime());

        // Отрицательные значения (если разрешены)
        PlayerResults negativeResults = new PlayerResults(-1, -1000L);
        Assertions.assertEquals(-1, negativeResults.getScore());
        Assertions.assertEquals(-1000L, negativeResults.getTime());
    }
}