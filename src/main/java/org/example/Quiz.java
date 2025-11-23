package org.example;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Класс для проведения викторины.
 * Предоставляет функционал для отображения вопросов, обработки ответов пользователя
 * и подсчета результатов. Поддерживает два режима работы: выбор темы и обычная викторина.
 */
public class Quiz {
    private final String[] alphabet = {"A","B","C","D"};    //ТЕПЕРЬ FINAL
    private final Memory memory;                            //ТЕПЕРЬ FINAL
    private final boolean chooseMode;                       //ТЕПЕРЬ FINAL
    private final Scanner sc = new Scanner(System.in);      //ТЕПЕРЬ FINAL

    private int currentQuestion = 0;
    private int score = 0;

    /**
     * Конструктор класса Quiz для пошагового режима.
     *
     * @param memory хранилище данных с вопросами
     * @param chooseMode true - режим выбора темы, false - обычная викторина
     */
    public Quiz(Memory memory, boolean chooseMode) {
        this.memory = memory;
        this.chooseMode = chooseMode;
        System.out.println("[QUIZ] Создан Quiz: режим=" + (chooseMode ? "выбор темы" : "викторина") +
                ", вопросов=" + memory.getData().length);
    }

    /**
     * Конструктор по умолчанию для рекурсивных вызовов (для обратной совместимости).
     */
    public Quiz() {
        this.memory = new Memory();
        this.chooseMode = false;
        System.out.println("[QUIZ] Создан пустой экземпляр Quiz");
    }

    /**
     * Получает текст текущего вопроса.
     *
     * @return текст текущего вопроса или результаты если викторина завершена
     */
    public String getCurrentQuestion() {
        if (currentQuestion >= memory.getData().length) {
            System.out.println("[QUIZ] Викторина завершена, показываем результаты");
            return getResults();
        }
        System.out.println("[QUIZ] Получение вопроса " + (currentQuestion + 1) + "/" + memory.getData().length);
        return printQuestion(memory, currentQuestion, chooseMode);
    }

    /**
     * Обрабатывает ответ пользователя и возвращает результат.
     *
     * @param answer индекс ответа пользователя
     * @return строку с результатом обработки ответа
     */
    public String processAnswer(int answer) {
        System.out.println("[QUIZ] Обработка ответа " + answer + " на вопрос " + (currentQuestion + 1));

        if (currentQuestion >= memory.getData().length) {
            System.out.println("[QUIZ] Викторина уже завершена");
            return "Викторина завершена!\n" + getResults();
        }

        String result;
        if (memory.getData()[currentQuestion].validAnswer(answer)) {
            result = "Верно!";
            score++;
            System.out.println("[QUIZ] Правильный ответ! Счет: " + score);
        } else {
            int correctAnswerIndex = memory.getData()[currentQuestion].getAnswer();
            String correctAnswerLetter = alphabet[correctAnswerIndex];
            String correctAnswerText = memory.getData()[currentQuestion].getOptions()[correctAnswerIndex];
            result = "К сожалению это не правильный ответ, правильным был вариант " +
                    correctAnswerLetter + ") " + correctAnswerText;
            System.out.println("[QUIZ] Неправильный ответ. Правильный: " + correctAnswerLetter);
        }

        currentQuestion++;
        System.out.println("[QUIZ] Переход к вопросу: " + currentQuestion + "/" + memory.getData().length);
        return result;
    }

    /**
     * Проверяет завершена ли викторина.
     *
     * @return true если викторина завершена, false иначе
     */
    public boolean isFinished() {
        boolean finished = currentQuestion >= memory.getData().length;
        System.out.println("[QUIZ] Проверка завершения: " + finished);
        return finished;
    }

    /**
     * Возвращает результаты викторины.
     *
     * @return строку с результатами
     */
    public String getResults() {
        String results = "Результаты викторины: " + score + " из " + memory.getData().length;
        System.out.println("[QUIZ] Результаты: " + results);
        return results;
    }

    /**
     * Получает текущий счет викторины.
     *
     * @return количество правильных ответов
     */
    public int getScore() {
        return score;
    }

    /**
     * Получает режим работы викторины.
     *
     * @return true если режим выбора темы, false если обычная викторина
     */
    public boolean isChooseMode() {
        return chooseMode;
    }

    /**
     * Получает хранилище данных.
     *
     * @return объект Memory с вопросами
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * Формирует текст вопроса с вариантами ответов.
     *
     * @param memory хранилище данных с вопросами
     * @param index индекс текущего вопроса
     * @param choose true - режим выбора темы, false - обычный вопрос
     * @return форматированный текст вопроса с вариантами ответов
     */
    private String printQuestion(Memory memory, int index, boolean choose) {
        StringBuilder result = new StringBuilder();

        if (choose) {
            result.append(memory.getData()[index].getQuestion()).append("\n");
        } else {
            result.append("Вопрос №").append(index + 1).append(") ")
                    .append(memory.getData()[index].getQuestion()).append("\n");
        }

        for (int j = 0; j < memory.getData()[index].getOptions().length; j++) {
            result.append(alphabet[j]).append(") ")
                    .append(memory.getData()[index].getOptions()[j]).append("\n");
        }

        if (choose) {
            result.append("\nВаш выбор: ");
        } else {
            result.append("\nВаш ответ: ");
        }

        return result.toString();
    }

    /**
     * Получает индекс ответа от пользователя из консоли.
     *
     * @return индекс ответа или -1 для справки
     */
    public int getIdxAnswer() {
        // Добавляем проверку наличия данных для предотвращения NoSuchElementException
        if (!sc.hasNextLine()) {
            System.out.println("[QUIZ] Нет данных для чтения");
            return -1;
        }

        String answer = sc.nextLine().toUpperCase().trim();

        if (answer.isEmpty()) {
            return -1;
        }

        if (answer.equalsIgnoreCase("help")) {
            return -1;
        }
        try {
            int temp = Integer.parseInt(answer);
            if (temp >= 1 && temp <= alphabet.length) {
                return temp - 1;
            }
        } catch (NumberFormatException e) {
            // Игнорируем, буквенный формат
        }

        return Arrays.asList(alphabet).indexOf(answer);
    }

    /**
     * Обрабатывает одиночный ответ для пошагового режима.
     *
     * @param memory хранилище данных с вопросами
     * @param index индекс текущего вопроса
     * @param answer ответ пользователя (-1 для справки)
     * @param choose true - выбор темы, false - обычный вопрос
     * @return строку с результатом обработки ответа
     */
    private String processSingleAnswer(Memory memory, int index, int answer, boolean choose) {
        System.out.println("[QUIZ] processSingleAnswer: индекс=" + index + ", ответ=" + answer + ", режим=" + choose);

        if (choose) {
            String selectedTopic = memory.getData()[index].getOptions()[answer];
            System.out.println("[QUIZ] Выбрана тема: " + selectedTopic);
            return "Выбрана тема: " + selectedTopic + "\n(Для начала викторины используйте команду /start)";
        } else {
            if (memory.getData()[index].validAnswer(answer)) {
                System.out.println("[QUIZ] Ответ правильный в processSingleAnswer");
                return "Верно!";
            } else {
                int correctAnswerIndex = memory.getData()[index].getAnswer();
                String correctAnswerLetter = alphabet[correctAnswerIndex];
                String correctAnswerText = memory.getData()[index].getOptions()[correctAnswerIndex];
                System.out.println("[QUIZ] Ответ неправильный в processSingleAnswer. Правильный: " + correctAnswerLetter);
                return "К сожалению это не правильный ответ, правильным был вариант " +
                        correctAnswerLetter + ") " + correctAnswerText;
            }
        }
    }

    /**
     * Запускает викторину в указанном режиме (с рекурсией).
     *
     * @param memory хранилище данных с вопросами
     * @param choose true - режим выбора темы, false - обычный вопрос
     * @return строку с результатом викторины
     */
    public String run(Memory memory, boolean choose) {
        System.out.println("[QUIZ] Запуск викторины в режиме: " + (choose ? "выбор темы" : "обычная"));
        StringBuilder result = new StringBuilder();
        int localScore = 0;

        for (int i = 0; i < memory.getData().length; i++) {
            result.append(printQuestion(memory, i, choose));

            int answerIndex = getIdxAnswer();
            // Проверяем, что получили валидный ответ
            if (answerIndex == -1) {
                result.append("Неверный формат ответа\n");
                continue;
            }

            String answerResult = processSingleAnswer(memory, i, answerIndex, choose);
            result.append(answerResult).append("\n");

            // если это рекурсивный вызов (выбор темы), прерываем текущую викторину
            if (choose && i == 0) {
                System.out.println("[QUIZ] Выбор темы завершен, возвращаем результат");
                return result.toString();
            }

            if (!choose && memory.getData()[i].validAnswer(answerIndex)) {
                localScore++;
            }

            result.append("\n");
        }

        if (!choose) {
            result.append("Ваш счёт: ").append(localScore).append(" из ").append(memory.getData().length);
        }

        System.out.println("[QUIZ] Викторина завершена");
        return result.toString();
    }

    /**
     * Сбрасывает состояние викторины для повторного использования.
     */
    public void reset() {
        this.currentQuestion = 0;
        this.score = 0;
        System.out.println("[QUIZ] Состояние викторины сброшено");
    }
}