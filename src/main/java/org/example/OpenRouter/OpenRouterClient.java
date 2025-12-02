package org.example.OpenRouter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * OpenRouterClient - класс работы с ии.
 */

public class OpenRouterClient {
    private final String apiKey;

    public OpenRouterClient(String apiKey) {
        this.apiKey = apiKey;
    }


    /**
     * отправка запроса через OpenRouter.
     *
     * @param userPrompt - наш запрос, который мы задали в StartYesButton
     * @return - возвращаем ответ на запрос в виде строки
     * @throws IOException          - если проблемы с сетью или с вводом/выводом
     * @throws InterruptedException - если поток был вызван ожидания ответа
     */

    public String sendRequest(String userPrompt) throws IOException, InterruptedException {
        String requestBody = createRequestBody(userPrompt);
        HttpRequest request = createHttpRequest(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return processResponse(response);
    }


    /**
     * createRequestBody - метод для создания тела HTTP запроса в формате json
     *
     * @param userPrompt - наш запрос
     * @return - возвращает json строку с телом запроса
     */
    private String createRequestBody(String userPrompt) {
        return String.format("""
                    {
                      "model": "gpt-3.5-turbo",
                      "messages": [
                        {"role": "system", "content": "You are a helpful assistant."},
                        {"role": "user", "content": "%s"}
                      ]
                    }
                """, userPrompt.replace("\"", "\\\""));
    }

    /**
     * createHttpRequest - метод для создания HTTP запроса
     *
     * @param requestBody - тело в формате json (предыдущий метод)
     * @return настроенный объект HttpRequest
     */

    private HttpRequest createHttpRequest(String requestBody) {

        return HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "https://example.com")
                .header("X-Title", "Java Test App")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    /**
     * processResponse - метод обработки HTTp ответ от APi
     *
     * @param response - ответ от сервера
     * @return - возвращаем текст от ии
     * @throws JSONException - ошибка парсинга json
     */

    private String processResponse(HttpResponse<String> response) throws JSONException {
        // если 400 какаято то впн значит отваллися
        System.out.println("Status code: " + response.statusCode());

        JSONObject json = new JSONObject(response.body());

        if (response.statusCode() != 200) {
            String errorMessage = "HTTP Error: " + response.statusCode();

            // ивлекаем сообщение об ошибке
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                errorMessage += " - " + error.getString("message");
                if (error.has("type")) {
                    errorMessage += " (Type: " + error.getString("type") + ")";
                }
            } else if (json.has("message")) {
                errorMessage += " - " + json.getString("message");
            }
            throw new RuntimeException(errorMessage);
        }

        if (!json.has("choices")) {
            throw new RuntimeException("Error: 'choices' field not found in response. Available keys: " + json.keySet());
        }

        // извлекаем текст ответа ии из структуры JSON:
        // json -> choices (массив) -> первый элемент -> message -> content
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }
}
