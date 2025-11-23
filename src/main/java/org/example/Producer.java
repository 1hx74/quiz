package org.example;
/**
 * Класс-производитель для обработки пользовательского контента и управления состоянием пользователей.
 * Обрабатывает входящие сообщения и возвращает соответствующий контент для ответа.
 */
public class Producer {
    private Users users;
    private final String[] options = {"A", "B", "C", "D"};

    private static final String HELP_MESSAGE = """
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
            • /leaderboard - топ-5 игроков

            Удачи в викторине! 🎉""";

    /**
     * Конструктор по умолчанию.
     */
    public Producer() {
        this.users = new Users();
        System.out.println("[PRODUCER] Producer создан, данные пользователей загружены");
    }

    /**
     * Устанавливает объект для управления пользователями.
     */
    public void setUsers(Users users) {
        this.users = users;
        System.out.println("[PRODUCER] Установлен Users manager");
    }

    /**
     * Возвращает массив сообщений для отправки
     */
    public Content[] produce(Content content) {
        String chatId = content.getChatId();
        System.out.println("[PRODUCER] Обработка входящего сообщения");

        UserData userData = users.getOrCreate(chatId);
        System.out.println("[PRODUCER] Текущее состояние пользователя: " + userData.getState());

        Content[] result = processUserMessage(content, chatId, userData);

        users.saveToDisk();
        System.out.println("[PRODUCER] Данные пользователей сохранены после обработки сообщения");

        return result;
    }

    /**
     * Обрабатывает сообщение пользователя и возвращает результат
     */
    private Content[] processUserMessage(Content content, String chatId, UserData userData) {
        // Если пользователь в состоянии ожидания имени для лидерборда
        if ("waiting_leaderboard_name".equals(userData.getState())) {
            String name = content.getText().trim();
            if (!name.isEmpty() && name.length() <= 20) {
                users.setLeaderboardName(chatId, name);
                userData.setState("menu");
                return new Content[] {
                        createTextContent(chatId, "✅ Имя \"" + name + "\" успешно установлено!\nТеперь вы отображаетесь в лидерборде!"),
                        createTextContent(chatId, users.getFormattedLeaderboard()),
                        createMenuContent(chatId, "Возврат в главное меню:")
                };
            } else {
                return new Content[] {
                        createTextContent(chatId, "❌ Неверное имя!\nИмя должно быть от 1 до 20 символов.\nПожалуйста, введите ваше имя еще раз:")
                };
            }
        }

        // обработка команд и кнопок на клаве
        switch (content.getText()) {
            case "/start":
                System.out.println("[PRODUCER] Обработка команды /start");
                return new Content[] { createMenuContent(chatId, "Добро пожаловать в викторину! Выберите действие:") };
            case "/help":
            case "Помощь":
                System.out.println("[PRODUCER] Обработка команды помощи");
                return new Content[] { createTextContent(chatId, HELP_MESSAGE) };
            case "/leaderboard":
                System.out.println("[PRODUCER] Обработка команды лидерборда");
                String leaderboard = users.getFormattedLeaderboard();
                return new Content[] { createTextContent(chatId, leaderboard) };

            case "Начать викторину":
                System.out.println("[PRODUCER] Пользователь начал стандартную викторину");
                startStandardQuiz(chatId);
                String question = userData.getCurrentQuiz().getCurrentQuestion();
                userData.setLastQuestion(question);
                return new Content[] { createTextContent(chatId, question) };
            case "Выбрать тему":
                System.out.println("[PRODUCER] Пользователь выбрал режим выбора темы");
                startTopicSelection(chatId);
                question = userData.getCurrentQuiz().getCurrentQuestion();
                userData.setLastQuestion(question);
                return new Content[] { createTextContent(chatId, question) };
            case "Начать заново":
                System.out.println("[PRODUCER] Пользователь начал заново");
                userData.setCurrentQuiz(null);
                userData.setLastQuestion(null);
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
        Quiz quiz = userData.getCurrentQuiz();

        if (quiz == null) {
            System.out.println("[PRODUCER] Нет активной викторины для пользователя " + chatId);
            return new Content[] { createMenuContent(chatId, "Викторина не активна. Начните викторину:") };
        }

        String userState = userData.getState();
        System.out.println("[PRODUCER] Обработка ответа в состоянии: " + userState);

        if (content.getText().equalsIgnoreCase("help")) {
            System.out.println("[PRODUCER] Пользователь запросил помощь");
            String currentQuestion = userData.getLastQuestion();
            if (currentQuestion == null) {
                currentQuestion = quiz.getCurrentQuestion();
            }
            return new Content[] { createTextContent(chatId, HELP_MESSAGE + "\n\n" + currentQuestion) };
        }

        int answerIndex = convertAnswerToIndex(content.getText());
        System.out.println("[PRODUCER] Ответ пользователя '" + content.getText() + "' преобразован в индекс: " + answerIndex);

        if (answerIndex == -1) {
            System.out.println("[PRODUCER] Невалидный ответ от пользователя");
            userData.setState("waiting_correct_input");
            String currentQuestion = userData.getLastQuestion();
            if (currentQuestion == null) {
                currentQuestion = quiz.getCurrentQuestion();
            }
            return createInvalidAnswerResponse(chatId, content.getText(), currentQuestion);
        }

        userData.setState(userState.equals("topic_selection") ? "topic_selection" : "quiz");

        if ("topic_selection".equals(userState)) {
            System.out.println("[PRODUCER] Обработка выбора темы с индексом: " + answerIndex);
            return new Content[] { handleTopicSelection(chatId, answerIndex) };
        }

        System.out.println("[PRODUCER] Обработка ответа в викторине");
        return handleQuizAnswer(chatId, answerIndex, quiz);
    }

    /**
     * Обновляет счет пользователя (добавляет к общей сумме)
     */
    private void updateUserScore(String chatId, int additionalScore) {
        UserData userData = users.get(chatId);
        userData.addToScore(additionalScore);
        System.out.println("[PRODUCER] Обновлен счет пользователя " + chatId + ": " + userData.getScore() + " (+" + additionalScore + ")");
    }

    /**
     * Сохраняет результат по теме
     */
    private void saveTopicResult(String chatId, String topic, int score) {
        UserData userData = users.get(chatId);
        userData.addTopicScore(topic, score);
        System.out.println("[PRODUCER] Сохранен результат по теме '" + topic + "' для пользователя " + chatId + ": " + score);
    }

    /**
     * запускает стандартную викторину для пользователя.
     */
    private void startStandardQuiz(String chatId) {
        System.out.println("[PRODUCER] Запуск стандартной викторины для " + chatId);
        Memory memory = new Memory();
        memory.read();

        if (!memory.hasData() || memory.getData().length == 0) {
            System.err.println("[PRODUCER] Ошибка: нет данных для викторины");
            return;
        }

        String firstTopic = memory.getData()[0].getOptions()[0];
        memory.reConnect("/" + firstTopic + ".json");
        memory.read();

        UserData userData = users.get(chatId);
        userData.setCurrentQuiz(new Quiz(memory, false));
        userData.setState("quiz");
        System.out.println("[PRODUCER] Пользователь " + chatId + " начал викторину по теме: " + firstTopic);
    }

    /**
     * запускает выбор темы для пользователя.
     */
    private void startTopicSelection(String chatId) {
        System.out.println("[PRODUCER] Запуск выбора темы для " + chatId);
        Memory memory = new Memory();
        memory.read();

        if (!memory.hasData() || memory.getData().length == 0) {
            System.err.println("[PRODUCER] Ошибка: нет данных для выбора темы");
            return;
        }

        UserData userData = users.get(chatId);
        userData.setCurrentQuiz(new Quiz(memory, true));
        userData.setState("topic_selection");
        System.out.println("[PRODUCER] Пользователь " + chatId + " начал выбор темы, доступно тем: " + memory.getData().length);
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
        Content errorMessage = new Content(true, chatId,
                "❌ Неверный ответ: '" + userAnswer + "'\n\n" +
                        "📝 Пожалуйста, введите ответ еще раз используя: A, B, C, D или 1, 2, 3, 4");

        Content questionMessage = new Content(true, chatId, currentQuestion);

        return new Content[] { errorMessage, questionMessage };
    }

    /**
     * обрабатывает выбор темы пользователем
     */
    private Content handleTopicSelection(String chatId, int topicIndex) {
        System.out.println("[PRODUCER] Обработка выбора темы с индексом " + topicIndex);
        UserData userData = users.get(chatId);
        Quiz currentQuiz = userData.getCurrentQuiz();

        if (currentQuiz == null || !currentQuiz.isChooseMode()) {
            System.err.println("[PRODUCER] Ошибка: нет активного режима выбора темы");
            return createTextContent(chatId, "❌ Ошибка выбора темы. Попробуйте снова.");
        }

        Memory topicMemory = currentQuiz.getMemory();
        String[] availableTopics = topicMemory.getData()[0].getOptions();

        if (topicIndex < 0 || topicIndex >= availableTopics.length) {
            System.err.println("[PRODUCER] Неверный индекс темы: " + topicIndex);
            return createTextContent(chatId, "❌ Неверный выбор темы. Попробуйте снова.");
        }

        String selectedTopic = availableTopics[topicIndex];
        System.out.println("[PRODUCER] Выбрана тема: " + selectedTopic);

        // Создаем новую память для выбранной темы
        Memory newTopicMemory = new Memory();
        newTopicMemory.reConnect("/" + selectedTopic + ".json");
        newTopicMemory.read();

        if (!newTopicMemory.hasData()) {
            System.err.println("[PRODUCER] Ошибка: не удалось загрузить данные по теме: " + selectedTopic);
            return createTextContent(chatId, "❌ Ошибка загрузки темы '" + selectedTopic + "'. Попробуйте другую тему.");
        }

        Quiz newQuiz = new Quiz(newTopicMemory, false);
        userData.setCurrentQuiz(newQuiz);
        userData.setState("quiz");

        String question = newQuiz.getCurrentQuestion();
        userData.setLastQuestion(question);

        Content result = new Content(true, chatId,
                "🎯 Выбрана тема: " + selectedTopic + "\n\n" + question);

        System.out.println("[PRODUCER] Пользователь " + chatId + " выбрал тему: " + selectedTopic +
                ", вопросов: " + newTopicMemory.getData().length);

        return result;
    }

    /**
     * возвращает ТРИ отдельных сообщения: результат ответа, итоги викторины и предложение для лидерборда
     */
    private Content[] handleQuizAnswer(String chatId, int answerIndex, Quiz quiz) {
        System.out.println("[PRODUCER] Обработка ответа викторины с индексом " + answerIndex);
        UserData userData = users.get(chatId);

        String answerResult = quiz.processAnswer(answerIndex);

        if (answerResult.equals("Верно!")) {
            updateUserScore(chatId, 1);
        }

        Content resultMessage = createTextContent(chatId, answerResult);

        if (!quiz.isFinished()) {
            System.out.println("[PRODUCER] Викторина продолжается, отправляем следующий вопрос");
            String nextQuestion = quiz.getCurrentQuestion();
            userData.setLastQuestion(nextQuestion);
            Content nextQuestionMessage = createTextContent(chatId, nextQuestion);
            return new Content[] { resultMessage, nextQuestionMessage };
        } else {
            System.out.println("[PRODUCER] Викторина завершена, показываем результаты");

            int totalScore = userData.getScore();
            int quizScore = quiz.getScore();

            // Сохраняем результат этой викторины
            String topicName = "quiz_" + System.currentTimeMillis();
            saveTopicResult(chatId, topicName, quizScore);

            // Сообщение 1: Результат последнего ответа
            Content finalResultMessage = createTextContent(chatId, answerResult);

            // Сообщение 2: Итоги викторины
            Content quizResultsMessage = createTextContent(chatId,
                    quiz.getResults() +
                            "\nВаш общий счет: " + totalScore + " баллов");

            if (users.canEnterLeaderboard(totalScore) && userData.getLeaderboardName() == null) {
                // Сообщение 3: Поздравление и предложение ввести имя для лидерборда
                Content leaderboardOfferMessage = createTextContent(chatId,
                        "🎉 **ПОЗДРАВЛЯЕМ!** 🎉\n" +
                                "Вы набрали " + quizScore + " баллов в этой викторине!\n" +
                                "Ваш общий счет: " + totalScore + " баллов\n\n" +
                                "Вы попали в ТОП-5 лидеров!\n" +
                                "Для отображения в лидерборде введите ваше имя (имя можно ввести один раз, выбирайте с умом):");

                userData.setState("waiting_leaderboard_name");

                return new Content[] {
                        finalResultMessage,
                        quizResultsMessage,
                        leaderboardOfferMessage
                };
            } else {
                // Сообщение 3: Меню действий
                Content menuMessage = new Content(true, chatId,
                        "Возврат в главное меню:",
                        new String[]{"Начать заново", "Выбрать тему", "В меню"});

                userData.setCurrentQuiz(null);
                userData.setLastQuestion(null);
                userData.setState("menu");

                return new Content[] {
                        finalResultMessage,
                        quizResultsMessage,
                        menuMessage
                };
            }
        }
    }

    /**
     * создает контент для главного меню.
     */
    Content createMenuContent(String chatId, String message) {
        System.out.println("[PRODUCER] Создание меню для пользователя");
        Content result = new Content(true, chatId, message,
                new String[]{"Начать викторину", "Выбрать тему", "Помощь"});
        users.get(chatId).setState("menu");
        return result;
    }

    /**
     * создает простое текстовое сообщение
     */
    Content createTextContent(String chatId, String text) {
        return new Content(true, chatId, text);
    }
}