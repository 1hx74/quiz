package org.example.TopicSelector;

import org.example.Quiz.DataQuestion;
import org.example.Quiz.Memory.DiskMemory;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для выбора темы викторины со скроллингом
 */
public class TopicSelector {
    private final List<Topic> topics; // Храним темы
    private int currentIndex;

    public TopicSelector() {
        this.topics = new ArrayList<>();
        this.currentIndex = 0;
    }

    /**
     * Инициализирует список тем из choose.json
     */
    public void initializeFromMemory(DiskMemory memory) {
        topics.clear();


        if (memory.hasData() && memory.getData().length > 0) {
            DataQuestion chooseDataQuestion = new DataQuestion(
                     memory.getData()[0].getQuestion()
                    ,memory.getData()[0].getOptions()
                    ,memory.getData()[0].getAnswer());
            String[] options = chooseDataQuestion.getOptions();
            System.out.println("[TOPIC_SELECTOR]Тема №1 "+ options[0]);
            for (String topicFileName : options) {
                    String displayName = getDisplayName(topicFileName);
                    topics.add(new Topic(topicFileName, displayName));
                    System.out.println("[TOPIC_SELECTOR] Добавлена тема: " + topicFileName + " -> " + displayName);
                }
            }
            System.out.println("[TOPIC_SELECTOR] Инициализировано тем: " + topics.size());
        }


    /**
     * Преобразует имя файла в красивое название для пользователя
     */
    private String getDisplayName(String fileName) {
        return switch (fileName) {
            case "countries" -> "🌍 Страны мира";
            case "capitals" -> "🏛️ Столицы";
            case "space" -> "🚀 Космос";
            case "different" -> "🎭 Обо всём";
            default -> fileName;
        };
    }

    /**
     * Переходит к следующей теме (циклически)
     */
    public void next() {
        if (topics.isEmpty()) return;
        currentIndex = (currentIndex + 1) % topics.size();
        System.out.println("[TOPIC_SELECTOR] Переход к теме: " + getCurrentTopic());
    }

    /**
     * Переходит к предыдущей теме (циклически)
     */
    public void previous() {
        if (topics.isEmpty()) return;
        currentIndex = (currentIndex - 1 + topics.size()) % topics.size();
        System.out.println("[TOPIC_SELECTOR] Переход к теме: " + getCurrentTopic());
    }

    /**
     * Получает текущую тему (название JSON файла)
     */
    public String getCurrentTopic() {
        if (topics.isEmpty()) return null;
        return topics.get(currentIndex).fileName;
    }

    /**
     * Получает отформатированное сообщение для показа темы
     */
    public String getDisplayMessage() {
        if (topics.isEmpty()) {
            return "❌ Нет доступных тем для викторины";
        }

        Topic currentTopic = topics.get(currentIndex);

        return "🎯 Выбор темы викторины\n\n" +
                currentTopic.displayName + "\n\n" +
                "Листайте кнопками чтобы увидеть другие темы\n" +
                "Нажмите 'Играть' чтобы начать викторину\n\n" +
                "Страница " + (currentIndex + 1) + " из " + topics.size();
    }

    /**
     * Получает количество тем
     */
    public int getTopicCount() {
        return topics.size();
    }

    /**
     * Получает текущий индекс
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Внутренний класс для хранения информации о теме
     */
    private static class Topic {
        private final String fileName;    // Имя JSON файла (например "countries")
        private final String displayName; // Красивое название для пользователя

        public Topic(String fileName, String displayName) {
            this.fileName = fileName;
            this.displayName = displayName;
        }
    }
}