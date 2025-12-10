package org.example.Quiz.Memory;

import org.example.Quiz.DataQuestion;

/**
 * Класс для работы с хранилищем данных викторины из JSON файлов.
 * Имплементирует MemoryInterface и загружает данные при инициализации.
 */
abstract public class AbstractMemory {
    private DataQuestion[] data = new DataQuestion[0];

    public DataQuestion[] getData() {
        return data;
    }

    public void setData(DataQuestion[] data) {
        this.data = data != null ? data : new DataQuestion[0];
    }

    /**
     * Проверяет, есть ли загруженные данные.
     * @return true если данные загружены, false иначе
     */
    public boolean hasData() {
        return data != null && data.length > 0;
    }
}