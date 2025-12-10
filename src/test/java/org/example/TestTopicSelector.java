package org.example;

import org.example.Quiz.DataQuestion;
import org.example.Quiz.Memory.DiskMemory;
import org.example.TopicSelector.TopicSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestTopicSelector {

    private TopicSelector topicSelector;
    private DiskMemory memory;

    @BeforeEach
    public void setUp() {
        topicSelector = new TopicSelector();
        memory = new DiskMemory();
    }

    /**
     * Тестирование инициализации с пустой памятью.
     */
    @Test
    public void testInitializeFromEmptyMemory() {
        memory.setData(new DataQuestion[0]);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(0, topicSelector.getTopicCount(), "Количество тем должно быть 0 для пустой памяти");
        Assertions.assertNull(topicSelector.getCurrentTopic(), "Текущая тема должна быть null для пустой памяти");
    }

    /**
     * Тестирование инициализации с данными.
     */
    @Test
    public void testInitializeFromMemoryWithData() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(3, topicSelector.getTopicCount(), "Должно быть 3 темы");
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "Текущая тема должна быть 'countries'");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Текущий индекс должен быть 0");
    }

    /**
     * Тестирование навигации вперед.
     */
    @Test
    public void testNextNavigation() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        topicSelector.next();
        Assertions.assertEquals("capitals", topicSelector.getCurrentTopic(), "После next тема должна быть 'capitals'");
        Assertions.assertEquals(1, topicSelector.getCurrentIndex(), "Индекс должен быть 1");

        topicSelector.next();
        Assertions.assertEquals("space", topicSelector.getCurrentTopic(), "После next тема должна быть 'space'");
        Assertions.assertEquals(2, topicSelector.getCurrentIndex(), "Индекс должен быть 2");

        topicSelector.next();
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "После next на последней теме должна быть циклическая навигация к первой");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Индекс должен быть 0 после цикла");
    }

    /**
     * Тестирование навигации назад.
     */
    @Test
    public void testPreviousNavigation() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        topicSelector.previous();
        Assertions.assertEquals("space", topicSelector.getCurrentTopic(), "После previous с первой темы должна быть циклическая навигация к последней");
        Assertions.assertEquals(2, topicSelector.getCurrentIndex(), "Индекс должен быть 2 после цикла");

        topicSelector.previous();
        Assertions.assertEquals("capitals", topicSelector.getCurrentTopic(), "После previous тема должна быть 'capitals'");
        Assertions.assertEquals(1, topicSelector.getCurrentIndex(), "Индекс должен быть 1");

        topicSelector.previous();
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "После previous тема должна быть 'countries'");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Индекс должен быть 0");
    }

    /**
     * Тестирование навигации с одной темой.
     */
    @Test
    public void testNavigationWithSingleTopic() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        topicSelector.next();
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "При одной теме next не должен менять тему");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Индекс должен остаться 0");

        topicSelector.previous();
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "При одной теме previous не должен менять тему");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Индекс должен остаться 0");
    }
    /**
     * Тестирование получения отформатированного сообщения.
     */
    @Test
    public void testGetDisplayMessage() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        String message = topicSelector.getDisplayMessage();

        // Детальная проверка первой страницы
        String expectedFirstPage = """
        🎯 Выбор темы викторины

        🌍 Страны мира

        Листайте кнопками чтобы увидеть другие темы
        Нажмите 'Играть' чтобы начать викторину

        Страница 1 из 2""";

        Assertions.assertEquals(expectedFirstPage, message, "Сообщение первой страницы должно полностью совпадать");

        // Переходим ко второй теме и проверяем
        topicSelector.next();
        message = topicSelector.getDisplayMessage();

        String expectedSecondPage = """
        🎯 Выбор темы викторины

        🏛️ Столицы

        Листайте кнопками чтобы увидеть другие темы
        Нажмите 'Играть' чтобы начать викторину

        Страница 2 из 2""";

        Assertions.assertEquals(expectedSecondPage, message, "Сообщение второй страницы должно полностью совпадать");

        // Проверяем что навигация работает циклически
        topicSelector.next();
        message = topicSelector.getDisplayMessage();
        Assertions.assertEquals(expectedFirstPage, message, "После циклической навигации должна вернуться первая страница");
    }

    /**
     * Тестирование получения отформатированного сообщения без тем.
     */
    @Test
    public void testGetDisplayMessageWithNoTopics() {
        memory.setData(new DataQuestion[0]);
        topicSelector.initializeFromMemory(memory);

        String message = topicSelector.getDisplayMessage();

        Assertions.assertEquals("❌ Нет доступных тем для викторины", message,
                "Сообщение должно указывать на отсутствие тем");
    }

    /**
     * Тестирование преобразования имен файлов в отображаемые названия.
     */
    @Test
    public void testDisplayNameConversion() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space", "different", "unknown"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        // Проверяем первую тему - countries
        String message = topicSelector.getDisplayMessage();
        String[] lines = message.split("\n");
        Assertions.assertEquals("🌍 Страны мира", lines[2], "countries должно преобразовываться в '🌍 Страны мира'");

        // Проверяем вторую тему - capitals
        topicSelector.next();
        message = topicSelector.getDisplayMessage();
        lines = message.split("\n");
        Assertions.assertEquals("🏛️ Столицы", lines[2], "capitals должно преобразовываться в '🏛️ Столицы'");

        // Проверяем третью тему - space
        topicSelector.next();
        message = topicSelector.getDisplayMessage();
        lines = message.split("\n");
        Assertions.assertEquals("🚀 Космос", lines[2], "space должно преобразовываться в '🚀 Космос'");

        // Проверяем четвертую тему - different
        topicSelector.next();
        message = topicSelector.getDisplayMessage();
        lines = message.split("\n");
        Assertions.assertEquals("🎭 Обо всём", lines[2], "different должно преобразовываться в '🎭 Обо всём'");

        // Проверяем пятую тему - unknown
        topicSelector.next();
        message = topicSelector.getDisplayMessage();
        lines = message.split("\n");
        Assertions.assertEquals("unknown", lines[2], "unknown тема должна оставаться 'unknown'");
    }

    /**
     * Тестирование получения количества тем.
     */
    @Test
    public void testGetTopicCount() {
        Assertions.assertEquals(0, topicSelector.getTopicCount(), "Изначально количество тем должно быть 0");

        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(3, topicSelector.getTopicCount(), "После инициализации должно быть 3 темы");
    }

    /**
     * Тестирование получения текущего индекса.
     */
    @Test
    public void testGetCurrentIndex() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals", "space"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Начальный индекс должен быть 0");

        topicSelector.next();
        Assertions.assertEquals(1, topicSelector.getCurrentIndex(), "После next индекс должен быть 1");

        topicSelector.previous();
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "После previous индекс должен быть 0");
    }

    /**
     * Тестирование получения текущей темы.
     */
    @Test
    public void testGetCurrentTopic() {
        DataQuestion[] data = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals"}, 0)
        };
        memory.setData(data);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "Начальная тема должна быть 'countries'");

        topicSelector.next();
        Assertions.assertEquals("capitals", topicSelector.getCurrentTopic(), "После next тема должна быть 'capitals'");

        topicSelector.previous();
        Assertions.assertEquals("countries", topicSelector.getCurrentTopic(), "После previous тема должна быть 'countries'");
    }

    /**
     * Тестирование множественной инициализации.
     */
    @Test
    public void testMultipleInitialization() {
        DataQuestion[] firstData = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"countries", "capitals"}, 0)
        };
        memory.setData(firstData);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(2, topicSelector.getTopicCount(), "После первой инициализации должно быть 2 темы");

        DataQuestion[] secondData = new DataQuestion[] {
                new DataQuestion("Выберите тему", new String[]{"space"}, 0)
        };
        memory.setData(secondData);
        topicSelector.initializeFromMemory(memory);

        Assertions.assertEquals(1, topicSelector.getTopicCount(), "После второй инициализации должно быть 1 тема");
        Assertions.assertEquals("space", topicSelector.getCurrentTopic(), "Текущая тема должна быть 'space'");
        Assertions.assertEquals(0, topicSelector.getCurrentIndex(), "Индекс должен быть сброшен до 0");
    }
}