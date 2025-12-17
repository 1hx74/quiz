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
     * Конструктор копирования - создаёт глубокую копию другой памяти.
     * @param other другая память для копирования
     */
    public AiMemory(AiMemory other) {
        if (other != null && other.hasData()) {
            // Создаём ГЛУБОКУЮ копию массива DataQuestion
            DataQuestion[] originalData = other.getData();
            DataQuestion[] copiedData = new DataQuestion[originalData.length];

            for (int i = 0; i < originalData.length; i++) {
                // ГЛУБОКОЕ КОПИРОВАНИЕ каждого DataQuestion
                copiedData[i] = new DataQuestion(originalData[i]);
            }

            this.setData(copiedData);
            System.out.println("[" + name + "] Создана ГЛУБОКАЯ копия памяти с " +
                    copiedData.length + " вопросами");
        } else {
            System.out.println("[" + name + "] Создана пустая копия памяти");
        }
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

    /**
     * Возвращает массив вопросов с логированием операции.
     * Переопределяет метод родительского класса для добавления логирования
     * при каждом обращении к данным памяти.
     *
     * @return массив объектов DataQuestion, содержащий все вопросы,
     *         хранящиеся в памяти. Если вопросы не были установлены,
     *         возвращает пустой массив или массив нулевой длины
     *         (в зависимости от реализации родительского класса).
     */
    @Override
    public DataQuestion[] getData() {
        System.out.println("[" + name + "] Вернула " + super.getData().length + " вопросов");
        return super.getData();
    }

    /**
     * Создаёт глубокую копию этой памяти.
     * @return новая копия AiMemory
     */
    public AiMemory copy() {
        return new AiMemory(this);
    }
}