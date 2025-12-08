package org.example.Tokens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Tokens {
    private final String TelegramToken;
    private final String OpenRouterToken;
    private final String FileName1 = "/bot_token.txt";
    private final String FileName2 = "/open_router_token.txt";
    public Tokens() {

        String token1 = loadForTelegram(FileName1);
        String token2 = loadForOpenRouter(FileName2);
        this.TelegramToken = token1;
        System.out.println("[TOKEN] Токен загружен: " +
                (!TelegramToken.isEmpty() ? "успешно" : "не удалось"));
        this.OpenRouterToken = token2;
        System.out.println("[OPEN_ROUTER_TOKEN] Токен загружен: " +
                (!OpenRouterToken.isEmpty() ? "успешно" : "не удалось"));

    }
    private String loadForTelegram(String filename){
        return load(filename);
    }
    private String loadForOpenRouter(String filename){
        return load(filename);
    }


    private String load(String filename) {
        String fileName = filename;

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


    public String getTelegramToken(){
        return TelegramToken;

    }
    public String getOpenRouterToken(){
        return OpenRouterToken;

    }
    public boolean isValidForTelegramToken() {
        return TelegramToken != null && !TelegramToken.isEmpty();
    }
    public boolean isValidForOpenrouterToken() {return OpenRouterToken!= null && !OpenRouterToken.isEmpty();}

}



