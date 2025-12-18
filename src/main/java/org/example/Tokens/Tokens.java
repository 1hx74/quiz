package org.example.Tokens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Tokens {

    private static final String TELEGRAM_ENV = "TELEGRAM_TOKEN";
    private static final String OPEN_ROUTER_ENV = "OPEN_ROUTER_TOKEN";

    private final String TelegramToken;
    private final String OpenRouterToken;

    public Tokens() {

        this.TelegramToken = load(
                TELEGRAM_ENV,
                "/bot_token.txt",
                "[TELEGRAM_TOKEN]"
        );

        this.OpenRouterToken = load(
                OPEN_ROUTER_ENV,
                "/open_router_token.txt",
                "[OPEN_ROUTER_TOKEN]"
        );
    }

    private String load(String envName, String resourceFile, String logPrefix) {

        String envToken = System.getenv(envName);
        if (envToken != null && !envToken.trim().isEmpty()) {
            System.out.println(logPrefix + " Загружен из ENV");
            return envToken.trim();
        }

        try (InputStream is = getClass().getResourceAsStream(resourceFile)) {
            if (is == null) {
                System.err.println(logPrefix + " Файл не найден: " + resourceFile);
                return "";
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String token = reader.readLine();
                if (token != null && !token.trim().isEmpty()) {
                    System.out.println(logPrefix + " Загружен из файла");
                    return token.trim();
                }
            }

        } catch (IOException e) {
            System.err.println(logPrefix + " Ошибка чтения файла: " + e.getMessage());
        }

        return "";
    }

    public String getTelegramToken() {
        return TelegramToken;
    }

    public String getOpenRouterToken() {
        return OpenRouterToken;
    }

    public boolean isValidForTelegramToken() {
        return TelegramToken != null && !TelegramToken.isEmpty();
    }

    public boolean isValidForOpenrouterToken() {
        return OpenRouterToken != null && !OpenRouterToken.isEmpty();
    }
}
