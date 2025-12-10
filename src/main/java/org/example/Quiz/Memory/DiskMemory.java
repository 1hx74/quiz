package org.example.Quiz.Memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Quiz.DataQuestion;

import java.io.IOException;

/**
 * Класс для работы с хранилищем данных викторины из JSON файлов.
 * Загружает данные при инициализации.
 */
public class DiskMemory extends AbstractMemory {
    private final String name = "MEMORY";
    private final ObjectMapper mapper = new ObjectMapper();
    private String filePath = "/choose.json";

    /**
     * Конструктор по умолчанию - загружает choose.json.
     */
    public DiskMemory() {
        read();
    }

    /**
     * Конструктор с указанием файла для загрузки.
     * @param filePath путь к JSON-файлу с вопросами
     */
    public DiskMemory(String filePath) {
        this.filePath = filePath;
        read();
    }

    /**
     * Изменяет путь к файлу данных.
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
            DataQuestion[] arr = mapper.readValue(getClass().getResourceAsStream(filePath), DataQuestion[].class);
            setData(arr);
            System.out.println("[" + name + "] Загружено " + getData().length + " вопросов из " + filePath);
        } catch (IOException e) {
            System.err.println("[" + name + "] Ошибка загрузки данных из " + filePath + ": " + e.getMessage());
            setData(new DataQuestion[0]);
        } catch (NullPointerException e) {
            System.err.println("[" + name + "] Файл не найден: " + filePath);
            setData(new DataQuestion[0]);
        }
    }

    @Override
    public DataQuestion[] getData() {
        System.out.println("[" + name + "] Вернула " + super.getData().length + " вопросов");
        return super.getData();
    }

}