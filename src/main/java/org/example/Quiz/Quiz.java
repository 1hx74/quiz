package org.example.Quiz;

import org.example.Quiz.Memory.AbstractMemory;
import org.example.Quiz.Memory.DiskMemory;

/**
 * Класс для проведения викторины.
 * Управляет вопросами, ответами пользователя и навигацией по викторине.
 * Обеспечивает функциональность для отображения вопросов, обработки ответов,
 * подсчета очков и управления прогрессом викторины.
 */
public class Quiz {
    private AbstractMemory memory;
    private int currentQuestionIndex = 0;
    private int score = 0;

    /**
     * Создает викторину с указанным хранилищем данных.
     * @param memory хранилище данных с вопросами и ответами
     */
    public Quiz(AbstractMemory memory) {
        this.memory = memory;
        System.out.println("[QUIZ] Создан Quiz, вопросов=" + memory.getData().length);
    }

    /**
     * Создает пустую викторину для десериализации Jackson.
     */
    public Quiz() {
        this.memory = new DiskMemory();
    }

    /**
     * Возвращает хранилище данных викторины.
     * @return объект AbstractMemory с вопросами и ответами
     */
    public AbstractMemory getMemory() {
        return memory;
    }

    /**
     * Устанавливает хранилище данных викторины.
     * @param memory объект AbstractMemory с вопросами и ответами
     */
    public void setMemory(AbstractMemory memory) {
        this.memory = memory;
    }

    /**
     * Возвращает индекс текущего вопроса.
     * @return индекс текущего вопроса (начинается с 0)
     */
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    /**
     * Устанавливает индекс текущего вопроса.
     * @param currentQuestionIndex индекс текущего вопроса
     */
    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    /**
     * Возвращает текущее количество очков.
     * @return количество правильных ответов
     */
    public int getScore() {
        return score;
    }

    /**
     * Устанавливает количество очков.
     * @param score количество правильных ответов
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Возвращает текст текущего вопроса с навигацией и вариантами ответов.
     * @return форматированная строка с номером вопроса, текстом и вариантами ответов
     */
    public String getCurrentQuestionText() {
        DataQuestion[] data = memory.getData();
        if (data.length == 0) {
            return "❌ Нет доступных вопросов";
        }

        if (currentQuestionIndex < 0 || currentQuestionIndex >= data.length) {
            currentQuestionIndex = 0;
        }

        DataQuestion currentDataQuestion = data[currentQuestionIndex];
        return formatQuestionWithNavigation(currentDataQuestion, currentQuestionIndex);
    }

    /**
     * Возвращает финальное сообщение при завершении вопросов.
     * @return строка с информацией о завершении викторины
     */
    public String getFinalMessage() {
        DataQuestion[] data = memory.getData();
        int totalQuestions = data.length;
        int answered = countAnsweredQuestions();

        return "🏁 Вопросы закончились!\n\n" +
                "📊 Вы ответили на " + answered + " из " + totalQuestions + " вопросов\n\n" +
                "Проверьте свои ответы, нажмите кнопку заново или сдайте тест";
    }

    /**
     * Возвращает результаты викторины с подсчетом очков и процентов.
     * @return форматированная строка с результатами викторины
     */
    public String getResults() {
        DataQuestion[] data = memory.getData();
        int totalQuestions = data.length;
        double percentage = totalQuestions > 0 ? (score * 100.0 / totalQuestions) : 0;

        return "🏆 Викторина завершена!\n\n" +
                "✅ Правильных ответов: " + score + " из " + totalQuestions + "\n" +
                "📊 Результат: " + String.format("%.1f", percentage) + "%\n" +
                "🎯 Отвечено вопросов: " + countAnsweredQuestions() + " из " + totalQuestions;
    }

    /**
     * Возвращает общее количество вопросов в викторине.
     * @return количество вопросов
     */
    public int getTotalQuestions() {
        return memory.getData().length;
    }

    /**
     * Проверяет, находится ли викторина на финальном сообщении.
     * @return true если все вопросы пройдены, иначе false
     */
    public boolean isOnFinalMessage() {
        return currentQuestionIndex >= memory.getData().length;
    }

    /**
     * Проверяет, завершена ли викторина.
     * @return true если викторина завершена, иначе false
     */
    public boolean isFinished() {
        return currentQuestionIndex >= memory.getData().length;
    }

    /**
     * Обрабатывает ответ пользователя на текущий вопрос.
     * Обновляет счетчик очков в зависимости от правильности ответа.
     * Автоматически переходит к следующему вопросу после ответа.
     * @param answerText текст ответа пользователя (A, B, C, D)
     * @return сообщение о результате обработки ответа
     */
    public String processAnswer(String answerText) {
        DataQuestion[] data = memory.getData();
        System.out.println("[QUIZ] Обработка ответа '" + answerText + "' на вопрос " + (currentQuestionIndex + 1));

        if (currentQuestionIndex >= data.length) {
            return "❌ Викторина завершена!";
        }

        DataQuestion currentDataQuestion = data[currentQuestionIndex];
        String previousAnswer = currentDataQuestion.getUserAnswer();
        currentDataQuestion.setUserAnswer(answerText);

        String result;
        int answerIndex = convertAnswerToIndex(answerText);

        //  ВНИМАНИЕ: специально оставлено уведомление об ответе пользователя
        if (!answerText.equals(previousAnswer)) {
            if (answerIndex != -1 && currentDataQuestion.validAnswer(answerIndex)) {
                if (previousAnswer == null || !currentDataQuestion.validAnswer(convertAnswerToIndex(previousAnswer))) {
                    score++;
                }
                result = "✅ Ваш ответ \"" + answerText + "\" успешно сохранен!";
                System.out.println("[QUIZ] Правильный ответ! Счет: " + score);
            } else {
                if (previousAnswer != null && currentDataQuestion.validAnswer(convertAnswerToIndex(previousAnswer))) {
                    score--;
                }
                result = "✅ Ваш ответ \"" + answerText + "\" успешно сохранен!";
                System.out.println("[QUIZ] Ответ сохранен. Счет: " + score);
            }
        } else {
            result = "ℹ️ Вы уже выбрали этот ответ";
        }

        // Автоматически переходим к следующему вопросу
        // Но только если это не был повторный выбор того же ответа
        if (!answerText.equals(previousAnswer) && currentQuestionIndex < data.length) {
            if (currentQuestionIndex == data.length - 1) {
                // Если это был последний вопрос, переходим к финальному сообщению
                currentQuestionIndex = data.length;
            } else {
                currentQuestionIndex++;
            }
        }

        return result;
    }

    /**
     * Переходит к следующему вопросу.
     * Если это последний вопрос, переходит к финальному сообщению.
     */
    public void nextQuestion() {
        DataQuestion[] data = memory.getData();
        if (data.length == 0) return;

        if (currentQuestionIndex < data.length - 1) {
            currentQuestionIndex++;
        } else if (currentQuestionIndex == data.length - 1) {
            currentQuestionIndex = data.length;
        }
        System.out.println("[QUIZ] Переход к позиции: " + currentQuestionIndex);
    }

    /**
     * Переходит к предыдущему вопросу.
     * Если это первый вопрос, переходит к последнему вопросу.
     */
    public void previousQuestion() {
        DataQuestion[] data = memory.getData();
        if (data.length == 0) return;

        if (currentQuestionIndex == data.length) {
            currentQuestionIndex = data.length - 1;
        } else if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
        } else {
            currentQuestionIndex = data.length - 1;
        }
        System.out.println("[QUIZ] Переход к позиции: " + currentQuestionIndex);
    }

    /**
     * Переходит к финальному сообщению викторины.
     */
    public void goToFinalMessage() {
        currentQuestionIndex = memory.getData().length;
        System.out.println("[QUIZ] Переход к финальному сообщению");
    }

    /**
     * Переходит к первому вопросу викторины.
     * Сохраняет все предыдущие ответы пользователя.
     */
    public void goToFirstQuestion() {
        DataQuestion[] data = memory.getData();
        if (data.length > 0) {
            currentQuestionIndex = 0;
            System.out.println("[QUIZ] Переход к первому вопросу с сохраненными ответами");
        }
    }

    /**
     * Сбрасывает состояние викторины к начальному.
     * Обнуляет счет, текущий вопрос и очищает все ответы пользователя.
     */
    public void reset() {
        this.currentQuestionIndex = 0;
        this.score = 0;
        // Очищаем ответы пользователя во всех вопросах
        for (DataQuestion dataQuestion : memory.getData()) {
            dataQuestion.setUserAnswer(null);
        }
        System.out.println("[QUIZ] Состояние викторины сброшено");
    }

    /**
     * Форматирует вопрос с навигацией и вариантами ответов.
     * @param dataQuestion объект вопроса
     * @param questionIndex индекс вопроса
     * @return форматированная строка с вопросом
     */
    private String formatQuestionWithNavigation(DataQuestion dataQuestion, int questionIndex) {
        StringBuilder result = new StringBuilder();
        DataQuestion[] data = memory.getData();

        result.append("🎯 Вопрос ").append(questionIndex + 1)
                .append(" из ").append(data.length)
                .append("\n\n");

        result.append(dataQuestion.getQuestion()).append("\n\n");

        String[] options = dataQuestion.getOptions();
        String userAnswer = dataQuestion.getUserAnswer();

        for (int i = 0; i < options.length; i++) {
            String letter = convertIndexToLetter(i);
            String optionText = options[i];
            result.append(letter).append(") ").append(optionText).append("\n");
        }

        if (userAnswer != null && !userAnswer.isEmpty()) {
            result.append("\n📝 Ваш ответ: ").append(userAnswer);
        }

        return result.toString();
    }

    /**
     * Подсчитывает количество вопросов, на которые дан ответ.
     * @return количество отвеченных вопросов
     */
    private int countAnsweredQuestions() {
        int count = 0;
        for (DataQuestion dataQuestion : memory.getData()) {
            if (dataQuestion.getUserAnswer() != null && !dataQuestion.getUserAnswer().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Конвертирует буквенный ответ в индекс варианта.
     * @param answer буква ответа (A, B, C, D)
     * @return индекс варианта (0-3) или -1 если ответ невалидный
     */
    private int convertAnswerToIndex(String answer) {
        if (answer == null) return -1;

        return switch (answer.toUpperCase()) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default -> -1;
        };
    }

    /**
     * Конвертирует индекс варианта в букву.
     * @param index индекс варианта (0-3)
     * @return буква варианта (A, B, C, D) или "?" если индекс невалидный
     */
    private String convertIndexToLetter(int index) {
        return switch (index) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "C";
            case 3 -> "D";
            default -> "?";
        };
    }
}