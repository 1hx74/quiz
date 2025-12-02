package org.example.Tokens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Класс для загрузки и хранения токена Open Router.
 * Токен загружается из файла ресурсов при создании объекта.
 */
public class OpenRouterToken {
    private final String token;

    /**
     * Конструктор загружает токен при создании объекта.
     */
    public OpenRouterToken() {
        this.token = loadToken();
        System.out.println("[OPEN_ROUTER_TOKEN] Токен загружен: " +
                (!token.isEmpty() ? "успешно" : "не удалось"));
    }

    /**
     * Возвращает токен Open Router.
     *
     * @return токен Open Router или пустую строку если не удалось загрузить
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
        String fileName = "/open_router_token.txt";

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String loadedToken = reader.readLine();
                    if (loadedToken != null && !loadedToken.trim().isEmpty()) {
                        return loadedToken.trim();
                    }
                }
            } else {
                System.err.println("[OPEN_ROUTER_TOKEN] Файл токена не найден: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("[OPEN_ROUTER_TOKEN] Ошибка загрузки токена: " + e.getMessage());
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