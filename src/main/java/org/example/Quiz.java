package org.example;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Класс для проведения викторины.
 * Предоставляет функционал для отображения вопросов, обработки ответов пользователя
 * и подсчета результатов. Поддерживает два режима работы: выбор темы и обычная викторина.
 */
public class Quiz {
    private final String[] alphabet = {"A","B","C","D"};
    private int currentQuestion = 0;
    private int score = 0;
    private Memory memory;
    private boolean chooseMode;
    private final Scanner sc = new Scanner(System.in);

    /**
     * Конструктор по умолчанию для рекурсивных вызовов.
     */
    public Quiz() {
        System.out.println("[QUIZ] Создан новый экземпляр Quiz");
    }

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
        String results = "Ваш счёт: " + score + " из " + memory.getData().length;
        System.out.println("[QUIZ] Результаты: " + results);
        return results;
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
        String result = "";

        if (choose) {
            result += memory.getData()[index].getQuestion() + "\n";
        } else {
            result += "Вопрос №" + (index + 1) + ") " + memory.getData()[index].getQuestion() + "\n";
        }

        for (int j = 0; j < memory.getData()[index].getOptions().length; j++) {
            result += alphabet[j] + ") " + memory.getData()[index].getOptions()[j] + "\n";
        }

        if (choose) {
            result += "\nВаш ответ: ";
        } else {
            result += "\nВаш выбор: ";
        }

        return result;
    }

    /**
     * Получает индекс ответа от пользователя из консоли.
     *
     * @return индекс ответа или -1 для справки
     */
    int getIdxAnswer() {
        String answer = sc.next().toUpperCase();
        if (answer.equalsIgnoreCase("help")) {
            return -1;
        }
        try {
            int temp = Integer.parseInt(answer);
            if (temp >= 0 && temp <= alphabet.length) {
                return temp - 1;
            }
        } catch (NumberFormatException e) {
            // pass
        }

        return Arrays.asList(alphabet).indexOf(answer);
    }

    /**
     * Обрабатывает ответ пользователя на вопрос викторины (рекурсивная версия).
     *
     * @param memory хранилище данных с вопросами
     * @param index индекс текущего вопроса
     * @param answer ответ пользователя (-1 для справки)
     * @param choose true - выбор темы, false - обычный вопрос
     * @return строку с результатом: справку, сообщение о правильности ответа или результат новой викторины
     */
    private String processAnswer(Memory memory, int index, int answer, boolean choose) {
        if (answer == -1) {
            String helpMessage = """
    Usage\s
    for a classic qiuz:
    \tjava -jar ./out/artifacts/first_jar/first.jar
    or usage for your quiz
    \tjava -jar ./out/artifacts/first_jar/first.jar /your/path/to/question\s
    You can see this message if you use -h or --help
    To see input file format check us repository:
    https://github.com/1hx74/quiz
 \s
    \s""";
            return helpMessage + "\n" + printQuestion(memory, index, choose);
        }

        if (choose) {
            // Рекурсивный выбор темы
            memory.reConnect("/" + memory.getData()[index].getOptions()[answer] + ".json");
            memory.read();
            Quiz quiz = new Quiz();  // Используем конструктор по умолчанию
            return quiz.run(memory, false);  // Рекурсивный вызов с новой темой
        } else {
            if (memory.getData()[index].validAnswer(answer)) {
                return "Верно!";
            } else {
                int correctAnswerIndex = memory.getData()[index].getAnswer();
                String correctAnswerLetter = alphabet[correctAnswerIndex];
                String correctAnswerText = memory.getData()[index].getOptions()[correctAnswerIndex];
                return "К сожалению это не правильный ответ, правильным был вариант " +
                        correctAnswerLetter + ") " + correctAnswerText;
            }
        }
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

        if (answer == -1) {
            System.out.println("[QUIZ] Пользователь запросил справку");
            String helpMessage = """
    Usage\s
    for a classic qiuz:
    \tjava -jar ./out/artifacts/first_jar/first.jar
    or usage for your quiz
    \tjava -jar ./out/artifacts/first_jar/first.jar /your/path/to/question\s
    You can see this message if you use -h or --help
    To see input file format check us repository:
    https://github.com/1hx74/quiz
 \s
    \s""";
            return helpMessage + "\n" + printQuestion(memory, index, choose);
        }

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
     * Запускает викторину в указанном режиме (с рекусией).
     *
     * @param memory хранилище данных с вопросами
     * @param choose true - режим выбора темы, false - обычный вопрос
     * @return строку с результатом викторины
     */
    public String run(Memory memory, boolean choose) {
        System.out.println("[QUIZ] Запуск викторины в режиме: " + (choose ? "выбор темы" : "обычная"));
        String result = "";
        int score = 0;

        for (int i = 0; i < memory.getData().length; i++) {
            result += printQuestion(memory, i, choose);

            String answerResult = processSingleAnswer(memory, i, getIdxAnswer(), choose);
            result += answerResult + "\n";

            // если это рекурсивный вызов (выбор темы), прерываем текущую викторину
            if (choose && i == 0) {
                System.out.println("[QUIZ] Выбор темы завершен, возвращаем результат");
                return result;
            }

            if (!choose && memory.getData()[i].validAnswer(getIdxAnswer())) {
                score++;
            }

            result += "\n";
        }

        if (!choose) {
            result += "Ваш счёт: " + score + " из " + memory.getData().length;
        }

        System.out.println("[QUIZ] Викторина завершена");
        return result;
    }
}