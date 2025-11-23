package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Класс для работы с хранилищем данных викторины.
 * Обеспечивает загрузку и управление вопросами из JSON-файлов.
 */
public class Memory {
    private final ObjectMapper mapper = new ObjectMapper();     //ТЕПЕРЬ FINAL
    private Data[] data = new Data[0];
    private String filePath = "/choose.json";

    /**
     * Конструктор по умолчанию.
     */
    public Memory() {}

    /**
     * Конструктор с немедленной загрузкой указанного файла.
     *
     * @param filePath путь к JSON-файлу с вопросами
     */
    public Memory(String filePath) {
        this.filePath = filePath;
        read();
    }

    public Data[] getData() {
        return data;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setData(Data[] data) {
        this.data = data != null ? data : new Data[0];
    }

    /**
     * Изменяет путь к файлу данных.
     *
     * @param newPath новый путь к файлу
     */
    public void reConnect(String newPath) {
        this.filePath = newPath;
    }

    /**
     * Загружает данные из текущего файла.
     */
    public void read() {
        try {
            data = mapper.readValue(getClass().getResourceAsStream(filePath), Data[].class);
            System.out.println("[MEMORY] Загружено " + data.length + " вопросов из " + filePath);
        } catch (IOException e) {
            System.err.println("[MEMORY] Ошибка загрузки данных из " + filePath + ": " + e.getMessage());
            data = new Data[0];
        } catch (NullPointerException e) {
            System.err.println("[MEMORY] Файл не найден: " + filePath);
            data = new Data[0];
        }
    }

    /**
     * Проверяет, есть ли загруженные данные.
     *
     * @return true если данные загружены, false иначе
     */
    public boolean hasData() {
        return data != null && data.length > 0;
    }
}