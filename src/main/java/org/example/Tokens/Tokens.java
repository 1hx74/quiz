package org.example.Tokens;


public class Tokens implements TokenInterface {

    private final String TelegramToken;
    private final String OpenRouterToken;

    public Tokens() {
        TokensENV tokensENV = new TokensENV();
        TokensResources tokensResources = new TokensResources();

        String temp = tokensENV.getTelegramToken();
        if (temp == null) {
            TelegramToken = tokensResources.getTelegramToken();
        } else {
            TelegramToken = temp;
        }

        temp = tokensENV.getOpenRouterToken();
        if (temp == null) {
            OpenRouterToken = tokensResources.getOpenRouterToken();
        } else {
            OpenRouterToken = temp;
        }
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
