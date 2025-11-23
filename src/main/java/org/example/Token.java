package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Класс для загрузки и хранения токена телеграм бота.
 * Токен загружается из файла ресурсов при создании объекта.
 */
public class Token {
    private final String token;     //ТЕПЕРЬ ФАЙНАЛ

    /**
     * Конструктор загружает токен при создании объекта.
     */
    public Token() {
        this.token = loadToken();
        System.out.println("[TOKEN] Токен загружен: " +
                (!token.isEmpty() ? "успешно" : "не удалось"));
    }

    /**
     * Возвращает токен бота.
     *
     * @return токен бота или пустую строку если не удалось загрузить
     */
    public String get() {
        return token;
    }

    /**
     * Загружает токен из файла ресурсов.
     *
     * @return загруженный токен или пустая строка при ошибке
     */
    private String loadToken() {
        String fileName = "/bot_token.txt";

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String loadedToken = reader.readLine();
                    if (loadedToken != null && !loadedToken.trim().isEmpty()) {
                        return loadedToken.trim();
                    }
                }
            } else {
                System.err.println("[TOKEN] Файл токена не найден: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("[TOKEN] Ошибка загрузки токена: " + e.getMessage());
        }

        return "";
    }

    /**
     * Проверяет валидность токена.
     *
     * @return true если токен не пустой, false иначе
     */
    public boolean isValid() {
        return token != null && !token.isEmpty();
    }
}