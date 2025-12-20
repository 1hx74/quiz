package org.example.Tokens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TokensResources implements TokenInterface {

    private final String TelegramToken;
    private final String OpenRouterToken;

    public TokensResources() {

        this.TelegramToken = load(
                "/bot_token.txt",
                "[TELEGRAM_TOKEN]"
        );

        this.OpenRouterToken = load(
                "/open_router_token.txt",
                "[OPEN_ROUTER_TOKEN]"
        );
    }

    private String load(String resourceFile, String logPrefix) {

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

    @Override
    public String getTelegramToken() {
        return TelegramToken;
    }

    @Override
    public String getOpenRouterToken() {
        return OpenRouterToken;
    }
}
