package org.example.Tokens;

public class TokensENV implements TokenInterface {

    private static final String TELEGRAM_ENV = "TELEGRAM_TOKEN";
    private static final String OPEN_ROUTER_ENV = "OPEN_ROUTER_TOKEN";

    private final String TelegramToken;
    private final String OpenRouterToken;

    public TokensENV() {

        this.TelegramToken = load(
                TELEGRAM_ENV,
                "[TELEGRAM_TOKEN]"
        );

        this.OpenRouterToken = load(
                OPEN_ROUTER_ENV,
                "[OPEN_ROUTER_TOKEN]"
        );
    }

    private String load(String envName, String logPrefix) {

        String envToken = System.getenv(envName);
        if (envToken != null && !envToken.trim().isEmpty()) {
            System.out.println(logPrefix + " Загружен из ENV");
            return envToken.trim();
        }
        else  {
            System.out.println(logPrefix + " Токен не найден в ENV");
        }
        return null;
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
