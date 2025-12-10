package org.example.Quiz.Memory;

import org.example.Quiz.DataQuestion;

/**
 * Память для вопросов, сделанных ИИ.
 * Просто хранит данные и пишет логи.
 */
public class AiMemory extends AbstractMemory {
    private final String name = "AI_MEMORY";

    /**
     * Создаёт пустую память.
     */
    public AiMemory() {
        System.out.println("[" + name + "] Создана пустая память");
    }

    /**
     * Создаёт память и загружает вопросы.
     * @param data массив вопросов
     * @param topic тема (просто для вывода)
     */
    public AiMemory(DataQuestion[] data, String topic) {
        setData(data);
        System.out.println("[" + name + "] Создана память с " + getData().length +
                " вопросами по теме: " + topic);
    }

    /**
     * Сохраняет вопросы.
     * @param data массив вопросов
     */
    @Override
    public void setData(DataQuestion[] data) {
        super.setData(data);
        System.out.println("[" + name + "] Установлено " + super.getData().length + " вопросов");
    }

    @Override
    public DataQuestion[] getData() {
        System.out.println("[" + name + "] Вернула " + super.getData().length + " вопросов");
        return super.getData();
    }
}
