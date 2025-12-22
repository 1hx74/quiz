package org.example.GenerationQuiz;

import org.example.OpenRouter.OpenRouterClient;
import org.example.Quiz.Memory.AiMemory;
import org.example.Quiz.DataQuestion;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Класс для создания викторин с помощью ИИ
 */
public class CreateQuiz {
    private final OpenRouterClient openRouterClient;

    public CreateQuiz(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    /**
     * Генерирует викторину по заданной теме
     * @param topic тема викторины
     * @return объект AiMemory с сгенерированными вопросами
     */
    public AiMemory generateQuiz(String topic) {
        try {
            // Создаем промпт для генерации викторины
            String prompt = createQuizPrompt(topic);

            // Отправляем запрос к ИИ
            String aiResponse = openRouterClient.sendRequest(prompt);

            // Парсим ответ и создаем объект AiMemory
            return parseQuizResponse(aiResponse, topic);

        } catch (Exception e) {
            System.err.println("[CREATE_QUIZ] Ошибка генерации викторины: " + e.getMessage());
            throw new RuntimeException("Не удалось сгенерировать викторину по теме: " + topic);
        }
    }

    /**
     * Создает промпт для генерации викторины
     */
    private String createQuizPrompt(String topic) {
        return String.format("""
            Сгенерируй викторину на тему "%s" в формате JSON. Требования:
            
            Формат JSON:
            {
              "quiz_topic": "название темы",
              "questions": [
                {
                  "question": "текст вопроса",
                  "options": ["вариант A", "вариант B", "вариант C", "вариант D"],
                  "correct_answer": 0
                }
              ]
            }
            
            Правила:
            - Количество вопросов: 5
            - correct_answer: индекс правильного ответа (0-3)
            - Варианты ответов: ровно 4 варианта
            - Вопросы должны быть разнообразными
            - Возвращай только чистый JSON без дополнительного текста
            
            Тема: %s
            """, topic, topic);
    }

    /**
     * Парсит ответ от ИИ и создает объект AiMemory
     */
    public AiMemory parseQuizResponse(String aiResponse, String topic) {
        String cleanResponse = aiResponse.trim();

        // пытаемся извлечь JSON
        if (cleanResponse.contains("```json")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        } else if (cleanResponse.contains("```")) {
            cleanResponse = cleanResponse.replace("```", "").trim();
        }

        JSONObject json = new JSONObject(cleanResponse);
        JSONArray questionsArray = json.getJSONArray("questions");

        // Создаем массив вопросов
        DataQuestion[] dataQuestions = new DataQuestion[questionsArray.length()];

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject questionObj = questionsArray.getJSONObject(i);

            String questionText = questionObj.getString("question");
            JSONArray optionsArray = questionObj.getJSONArray("options");
            int correctAnswer = questionObj.getInt("correct_answer");

            // Преобразуем JSON массив в String[]
            String[] options = new String[4];
            for (int j = 0; j < 4; j++) {
                options[j] = optionsArray.getString(j);
            }

            // Создаем объект вопроса
            dataQuestions[i] = new DataQuestion(questionText, options, correctAnswer);
        }

        // Создаем и возвращаем AiMemory
        AiMemory aiMemory = new AiMemory(dataQuestions, topic);
        System.out.println("[CREATE_QUIZ] Успешно сгенерирована викторина по теме: " + topic + ", вопросов: " + dataQuestions.length);

        return aiMemory;
    }
}