package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс-производитель для обработки пользовательского контента и управления состоянием пользователей.
 * Обрабатывает входящие сообщения и возвращает соответствующий контент для ответа.
 */
public class Producer {
    private Users users;
    private Map<String, Quiz> userQuizzes = new HashMap<>();
    private Map<String, String> userLastQuestions = new HashMap<>();
    private final String[] options = {"A", "B", "C", "D"};

    private static final String HELP_MESSEGE = """
            📚 Помощь по викторине:

            🎯 Как играть:
            • Выберите 'Начать викторину' для стандартной викторины
            • Или 'Выбрать тему' для выбора конкретной темы
            • Отвечайте на вопросы, выбирая варианты A, B, C, D или 1, 2, 3, 4

            📝 Форматы ответов:
            • Буквенный: A, B, C, D
            • Числовой: 1, 2, 3, 4
            • Помощь: help

            ⚡ Команды:
            • /start - начать работу
            • /help - показать эту справку

            Удачи в викторине! 🎉""";

    /**
     * Конструктор по умолчанию.
     */
    public Producer() {}

    /**
     * Устанавливает объект для управления пользователями.
     *
     * @param users объект для работы с пользователями
     */
    public void setUsers(Users users) {
        this.users = users;
        System.out.println("[PRODUCER] Установлен Users manager");
    }

    /**
     * Взвращает массив сообщений для отправки
     *
     * @param content входящий контент от пользователя
     * @return массив контента для отправки (может быть 1 или 2 сообщения)
     */
    public Content[] produce(Content content) {
        String chatId = content.getChatId();
        System.out.println("[PRODUCER] Обработка входящего сообщения");

        // создание нового пользователя, если не существует
        if (!users.has(chatId)) {
            UserData newUser = new UserData();
            newUser.setLevel(1);
            newUser.setScore(0);
            newUser.setState("menu");
            users.add(chatId, newUser);
            System.out.println("[PRODUCER] Создан новый пользователь: " + chatId);
        }

        UserData userData = users.get(chatId);
        System.out.println("[PRODUCER] Текущее состояние пользователя: " + userData.getState());

        // обработка команд и кнопок на клаве
        switch (content.getText()) {
            case "/start":
                System.out.println("[PRODUCER] Обработка команды /start");
                return new Content[] { createMenuContent(chatId, "Добро пожаловать в викторину! Выберите действие:") };
            case "/help":
            case "Помощь":
                System.out.println("[PRODUCER] Обработка команды помощи");
                return new Content[] { createTextContent(chatId, HELP_MESSEGE ) };

            case "Начать викторину":
                System.out.println("[PRODUCER] Пользователь начал стандартную викторину");
                startStandardQuiz(chatId);
                String question = getUserQuiz(chatId).getCurrentQuestion();
                userLastQuestions.put(chatId, question);
                return new Content[] { createTextContent(chatId, question) };
            case "Выбрать тему":
                System.out.println("[PRODUCER] Пользователь выбрал режим выбора темы");
                startTopicSelection(chatId);
                question = getUserQuiz(chatId).getCurrentQuestion();
                userLastQuestions.put(chatId, question);
                return new Content[] { createTextContent(chatId, question) };
            case "Начать заново":
                System.out.println("[PRODUCER] Пользователь начал заново");
                userQuizzes.remove(chatId);
                userLastQuestions.remove(chatId);
                return new Content[] { createMenuContent(chatId, "Добро пожаловать в викторину! Выберите действие:") };
            default:
                System.out.println("[PRODUCER] Обработка ответа на вопрос викторины");
                return processQuizAnswer(chatId, content);
        }
    }

    /**
     * Возвращает массив сообщений для ответа на вопрос викторины
     */
    private Content[] processQuizAnswer(String chatId, Content content) {
        UserData userData = users.get(chatId);
        Quiz quiz = userQuizzes.get(chatId);

        // Если нет активной викторины, возвращаем в меню
        if (quiz == null) {
            System.out.println("[PRODUCER] Нет активной викторины для пользователя " + chatId);
            return new Content[] { createMenuContent(chatId, "Викторина не активна. Начните викторину:") };
        }

        String userState = userData.getState();
        System.out.println("[PRODUCER] Обработка ответа в состоянии: " + userState);

        // Обработка запроса помощи
        if (content.getText().equalsIgnoreCase("help")) {
            System.out.println("[PRODUCER] Пользователь запросил помощь");
            String currentQuestion = userLastQuestions.get(chatId);
            if (currentQuestion == null) {
                currentQuestion = quiz.getCurrentQuestion();
            }
            return new Content[] { createTextContent(chatId, HELP_MESSEGE  + "\n\n" + currentQuestion) };
        }

        // Преобразуем ответ пользователя в индекс
        int answerIndex = convertAnswerToIndex(content.getText());
        System.out.println("[PRODUCER] Ответ пользователя '" + content.getText() + "' преобразован в индекс: " + answerIndex);

        // если ответ невалидный, просим ввести заново (ДВУМЯ сообщениями)
        if (answerIndex == -1) {
            System.out.println("[PRODUCER] Невалидный ответ от пользователя");
            userData.setState("waiting_correct_input");
            String currentQuestion = userLastQuestions.get(chatId);
            if (currentQuestion == null) {
                currentQuestion = quiz.getCurrentQuestion();
            }
            return createInvalidAnswerResponse(chatId, content.getText(), currentQuestion);
        }

        // Сбрасываем состояние ожидания, так как получили валидный ответ
        userData.setState(userState.equals("topic_selection") ? "topic_selection" : "quiz");

        // РЕКУРСИВНАЯ ЛОГИКА: если пользователь выбирает тему, автоматически запускаем викторину
        if ("topic_selection".equals(userState)) {
            System.out.println("[PRODUCER] Обработка выбора темы с индексом: " + answerIndex);
            return new Content[] { handleTopicSelection(chatId, answerIndex) };
        }

        // Обрабатываем ответ в обычной викторине (ДВУМЯ сообщениями)
        System.out.println("[PRODUCER] Обработка ответа в викторине");
        return handleQuizAnswer(chatId, answerIndex, quiz);
    }

    /**
     * запускает стандартную викторину для пользователя.
     */
    private void startStandardQuiz(String chatId) {
        System.out.println("[PRODUCER] Запуск стандартной викторины для " + chatId);
        Memory memory = new Memory();
        memory.read();
        memory.reConnect("/" + memory.getData()[0].getOptions()[0] + ".json");
        memory.read();
        userQuizzes.put(chatId, new Quiz(memory, false));
        users.get(chatId).setState("quiz");
    }

    /**
     * запускает выбор темы для пользователя.
     */
    private void startTopicSelection(String chatId) {
        System.out.println("[PRODUCER] Запуск выбора темы для " + chatId);
        Memory memory = new Memory();
        memory.read();
        userQuizzes.put(chatId, new Quiz(memory, true));
        users.get(chatId).setState("topic_selection");
    }

    /**
     * преобразует текстовый ответ в индекс варианта.
     */
    private int convertAnswerToIndex(String answer) {
        if (answer == null || answer.isEmpty()) {
            return -1;
        }

        answer = answer.toUpperCase().trim();

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(answer)) {
                return i;
            }
        }

        try {
            int number = Integer.parseInt(answer);
            if (number >= 1 && number <= options.length) {
                return number - 1;
            }
        } catch (NumberFormatException e) {
            // Не число
        }

        return -1;
    }

    /**
     * создает ответ для невалидного формата (два сообщения)
     */
    private Content[] createInvalidAnswerResponse(String chatId, String userAnswer, String currentQuestion) {
        System.out.println("[PRODUCER] Создание ответа для невалидного ввода");
        // первое сообщение: ошибка
        Content errorMessage = new Content(true);
        errorMessage.setChatId(chatId);
        errorMessage.setText("❌ Неверный ответ: '" + userAnswer + "'\n\n" +
                "📝 Пожалуйста, введите ответ еще раз используя: A, B, C, D или 1, 2, 3, 4");

        // ворое сообщение: повтор вопроса
        Content questionMessage = new Content(true);
        questionMessage.setChatId(chatId);
        questionMessage.setText(currentQuestion);

        return new Content[] { errorMessage, questionMessage };
    }

    /**
     * обрабатывает выбор темы пользователем
     */
    private Content handleTopicSelection(String chatId, int topicIndex) {
        System.out.println("[PRODUCER] Обработка выбора темы с индексом " + topicIndex);
        UserData userData = users.get(chatId);

        Memory topicMemory = new Memory();
        topicMemory.read();
        String selectedTopic = topicMemory.getData()[0].getOptions()[topicIndex];
        System.out.println("[PRODUCER] Выбрана тема: " + selectedTopic);

        topicMemory.reConnect("/" + selectedTopic + ".json");
        topicMemory.read();

        Quiz newQuiz = new Quiz(topicMemory, false);
        userQuizzes.put(chatId, newQuiz);
        userData.setState("quiz");

        String question = newQuiz.getCurrentQuestion();
        userLastQuestions.put(chatId, question);

        Content result = new Content(true);
        result.setChatId(chatId);
        result.setText("🎯 Выбрана тема: " + selectedTopic + "\n\n" + question);

        return result;
    }

    /**
     * возвращает ДВА отдельных сообщения: отчет о правильности отвера и следаующий вопрос
     */
    private Content[] handleQuizAnswer(String chatId, int answerIndex, Quiz quiz) {
        System.out.println("[PRODUCER] Обработка ответа викторины с индексом " + answerIndex);
        UserData userData = users.get(chatId);

        // первое сообщение: результат ответа
        String answerResult = quiz.processAnswer(answerIndex);
        Content resultMessage = createTextContent(chatId, answerResult);

        // если викторина продолжается, создаем второе сообщение: следующий вопрос
        if (!quiz.isFinished()) {
            System.out.println("[PRODUCER] Викторина продолжается, отправляем следующий вопрос");
            String nextQuestion = quiz.getCurrentQuestion();
            userLastQuestions.put(chatId, nextQuestion);
            Content nextQuestionMessage = createTextContent(chatId, nextQuestion);
            return new Content[] { resultMessage, nextQuestionMessage };
        } else {
            // викторина завершена - показываем результаты
            System.out.println("[PRODUCER] Викторина завершена, показываем результаты");
            Content finalMessage = createTextContent(chatId, answerResult + "\n\n" + quiz.getResults());
            finalMessage.setOptions(new String[]{"Начать заново", "Выбрать тему", "В меню"});
            userQuizzes.remove(chatId);
            userLastQuestions.remove(chatId);
            userData.setState("menu");
            return new Content[] { finalMessage };
        }
    }

    /**
     * создает контент для главного меню.
     */
    private Content createMenuContent(String chatId, String message) {
        System.out.println("[PRODUCER] Создание меню для пользователя");
        Content result = new Content(true);
        result.setChatId(chatId);
        result.setText(message);
        result.setOptions(new String[]{"Начать викторину", "Выбрать тему", "Помощь"});
        users.get(chatId).setState("menu");
        return result;
    }

    /**
     * создает простое текстовое сообщение
     */
    private Content createTextContent(String chatId, String text) {
        Content result = new Content(true);
        result.setChatId(chatId);
        result.setText(text);
        return result;
    }

    /**
     * возвращает активную викторину пользователя.
     */
    private Quiz getUserQuiz(String chatId) {
        Quiz quiz = userQuizzes.get(chatId);
        System.out.println("[PRODUCER] Получение викторины для " + chatId + ": " + (quiz != null ? "найдена" : "не найдена"));
        return quiz;
    }
}