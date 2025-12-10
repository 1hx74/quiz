package org.example;

import org.example.DataMessage.Content;
import org.example.DataMessage.KeyboardService;
import org.example.Quiz.*;
import org.example.Quiz.Memory.AiMemory;
import org.example.Quiz.Memory.DiskMemory;
import org.example.TopicSelector.TopicSelector;
import org.example.GenerationQuiz.CreateQuiz;
import org.example.OpenRouter.OpenRouterClient;
import org.example.Tokens.OpenRouterToken;

/**
 * Класс-производитель для обработки пользовательского контента и управления состоянием пользователей.
 * Обрабатывает входящие сообщения и возвращает соответствующий контент для ответа.
 * Управляет состояниями пользователей, обработкой команд и навигацией по викторине.
 */
public class Producer {
    private Users users;
    private final KeyboardService keyboardService;
    private final CreateQuiz createQuiz;

    private static final String HELP_MESSAGE = """
            📚 Помощь по викторине:

            🎯 Как играть:
            • Выберите 'Выбрать тему' для выбора конкретной темы
            • Или 'Сгенерировать' для генерации уникальной викторины ИИ
            • Отвечайте на вопросы, выбирая варианты кнопок
            • Используйте кнопки навигации для перемещения между вопросами

            📝 Форматы ответов:
            • Кнопки: кнопки под сообщениями
            • Помощь: help

            ⚡ Команды:
            • /start - начать работу
            • /help - показать эту справку
            • /leaderboard - топ-5 игроков

            Удачи в викторине! 🎉""";

    private static final String START_MESSAGE = """
            🧠 *QuizMaster Bot* - Твой умный помощник в мире викторин!
            
            ✨ *Что умеет этот бот?*
            • 🎯 Проводит увлекательные викторины на разные темы
            • 🤖 Генерирует уникальные викторины с помощью ИИ
            • 🔄 Навигация между вопросами с сохранением ответов
            • 🏆 Соревнуйся с друзьями в таблице лидеров
            
            🎮 *Как играть?*
            • Выбирай тему из доступных вариантов ИЛИ создай свою
            • Отвечай на вопросы, нажимая кнопки A, B, C, D
            • Перемещайся между вопросами с помощью навигации
            • Видишь свои ответы рядом с вариантами
            • Стань лучшим в рейтинге игроков!""";

    private static final String MENU_MESSAGE = """
            🎪 *Вы находитесь в главном меню!*
            
            🚀 *Создай свою уникальную викторину* с помощью нейросети
            📚 *Или выбери готовые темы* из нашей коллекции
            
            🎯 Что будем делать?""";

    /**
     * Конструктор по умолчанию.
     * Инициализирует менеджер пользователей и сервис клавиатур.
     */
    public Producer() {
        this.users = new Users();
        this.keyboardService = new KeyboardService();

        // Инициализация генератора викторин
        OpenRouterToken openRouterToken = new OpenRouterToken();
        OpenRouterClient openRouterClient = new OpenRouterClient(openRouterToken.get());
        this.createQuiz = new CreateQuiz(openRouterClient);

        System.out.println("[PRODUCER] Producer создан, данные пользователей загружены");
    }

    /**
     * Устанавливает объект для управления пользователями.
     * @param users менеджер пользователей
     */
    public void setUsers(Users users) {
        this.users = users;
        System.out.println("[PRODUCER] Установен Users manager");
    }

    /**
     * Основной метод обработки пользовательского контента.
     * Определяет состояние пользователя и вызывает соответствующие обработчики.
     * @param content входящий контент от пользователя
     * @return массив контента для ответа пользователю
     */
    public Content[] produce(Content content) {
        String chatId = content.getChatId();
        System.out.println("[PRODUCER] Обработка входящего сообщения от " + chatId);

        UserData userData = users.getOrCreate(chatId);
        System.out.println("[PRODUCER] Текущее состояние пользователя: " + userData.getState());

        Content[] result = processUserMessage(content, chatId, userData);
        System.out.println("[PRODUCER] Данные пользователей сохранены");

        return result;
    }

    /**
     * Обрабатывает пользовательское сообщение в зависимости от состояния пользователя.
     * @param content входящий контент
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] processUserMessage(Content content, String chatId, UserData userData) {
        String messageText = content.getText();

        // Обработка состояния ожидания имени для лидерборда
        if ("waiting_leaderboard_name".equals(userData.getState())) {
            return handleLeaderboardName(chatId, messageText, userData);
        }

        // Обработка состояния ожидания темы для генерации
        if ("waiting_generation_topic".equals(userData.getState())) {
            return handleGenerationTopic(chatId, messageText, userData);
        }

        return handleUserInput(messageText, chatId, userData);
    }

    /**
     * Обрабатывает ввод имени для лидерборда.
     * @param chatId идентификатор чата
     * @param name введное имя пользователя
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleLeaderboardName(String chatId, String name, UserData userData) {
        name = name.trim();
        if (!name.isEmpty() && name.length() <= 20) {
            users.setLeaderboardName(chatId, name);
            userData.setState("menu");

            return new Content[] {
                    new Content(true, chatId, "✅ Имя \"" + name + "\" успешно установлено!\nТеперь вы отображаетесь в лидерборде!"),
                    new Content(true, chatId, users.getFormattedLeaderboard(), "go_menu"),
            };
        } else {
            return new Content[] {
                    new Content(true, chatId, "❌ Неверное имя!\nИмя должно быть от 1 до 20 символов.\nПожалуйста, введите ваше имя еще раз:")
            };
        }
    }

    /**
     * Обрабатывает ввод темы для генерации викторины
     */
    private Content[] handleGenerationTopic(String chatId, String topic, UserData userData) {
        topic = topic.trim();

        if (topic.isEmpty() || topic.length() > 100) {
            return new Content[] {
                    new Content(true, chatId, """
                        ❌ Неверная тема!
                        Тема должна быть от 1 до 100 символов.
                        Пожалуйста, введите тему еще раз:""")
            };
        }

        try {
            System.out.println("[PRODUCER] Генерация викторины по теме: " + topic);

            // Генерируем викторину с помощью ИИ - теперь возвращает AiMemory
            AiMemory generatedMemory = createQuiz.generateQuiz(topic);

            // Создаем квиз из сгенерированной памяти
            Quiz generatedQuiz = new Quiz(generatedMemory); // Квиз принимает
            userData.setCurrentQuiz(generatedQuiz);
            userData.setState("quiz");

            String firstQuestion = generatedQuiz.getCurrentQuestionText();

            return new Content[] {
                    new Content(true, chatId, "✅ *Викторина успешно создана!*\n\n" +
                            "🎯 Тема: " + topic + "\n" +
                            "📊 Вопросов: " + generatedQuiz.getTotalQuestions() + "\n\n" +
                            "Приятной игры! 🎮"),
                    new Content(true, chatId, firstQuestion, null, "test_answer")
            };

        } catch (Exception e) {
            System.err.println("[PRODUCER] Ошибка генерации викторины: " + e.getMessage());
            userData.setState("menu");

            return new Content[] {
                    new Content(true, chatId, "❌ *Ошибка генерации викторины*\n\n" +
                            "Не удалось создать викторину по теме: " + topic + "\n" +
                            "Попробуйте другую тему или используйте готовые викторины.", null, "menu")
            };
        }
    }

    /**
     * Обрабатывает пользовательский ввод (команды и текстовые сообщения).
     * @param inputText текст ввода пользователя
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleUserInput(String inputText, String chatId, UserData userData) {
        if (inputText.startsWith("/")) {
            return handleCommand(inputText, chatId, userData);
        } else if (isButtonCallback(inputText)) {
            return handleButtonCallback(inputText, chatId, userData);
        } else {
            return handleTextMessage(inputText, chatId, userData);
        }
    }

    /**
     * Проверяет, является ли ввод callback от кнопки.
     * @param inputText текст ввода
     * @return true если это callback от кнопки, иначе false
     */
    private boolean isButtonCallback(String inputText) {
        return inputText.endsWith("_button");
    }

    /**
     * Обрабатывает callback от кнопок.
     * @param callbackData данные callback
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleButtonCallback(String callbackData, String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка callback: " + callbackData);

        // Навигационные кнопки в выборе темы
        switch (callbackData) {
            case "topic_forwards_button", "topic_backwards_button" -> {
                return handleTopicNavigationButtons(callbackData, chatId, userData);
            }


            // Навигационные кнопки в викторине
            case "quiz_forwards_button", "quiz_backwards_button" -> {
                return handleQuizNavigationButtons(callbackData, chatId, userData);
            }


            // Кнопки ответов
            case "A_button", "B_button", "C_button", "D_button" -> {
                return handleAnswerButtons(callbackData, chatId, userData);
            }


            // Кнопка перехода к первому вопросу
            case "at_the_top_button" -> {
                return handleAtTheTopButton(chatId, userData);
            }


            // Остальные кнопки
            default -> {
                switch (callbackData) {
                    case "quiz_button":
                        return startTopicSelection(chatId, userData);

                    case "play_button":
                        return startQuizWithSelectedTopic(chatId, userData);

                    case "menu_button":
                        userData.setState("menu");
                        userData.setCurrentQuiz(null);
                        userData.setTopicSelector(null);
                        return new Content[]{
                                new Content(true, chatId, MENU_MESSAGE, null, "menu")
                        };
                    case "generation_button":
                        return generationQuiz(chatId, userData);

                    case "end_quiz_button":
                        return handleQuizCompletion(chatId, userData);

                    default:
                        System.out.println("[PRODUCER] Неизвестный callback: " + callbackData);
                        return new Content[]{
                                new Content(true, chatId, "Неизвестное действие", null, "menu")
                        };
                }
            }
        }
    }

    /**
     * Обрабатывает генерацию викторины через ИИ
     */
    private Content[] generationQuiz(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Запуск генерации викторины для " + chatId);

        // Устанавливаем состояние ожидания темы
        userData.setState("waiting_generation_topic");

        return new Content[] {
                new Content(true, chatId, """
                        🚀 *Генерация викторины с помощью ИИ*
                        
                        Введите тему для викторины (например: "Программирование", "История", "Наука"):
                        
                        📝 *Рекомендации:*
                        • Будьте конкретны в выборе темы
                        • Избегайте слишком общих формулировок
                        • Примеры хороших тем: "Java ООП", "Великие открытия\"""")
        };
    }

    /**
     * Обрабатывает кнопку перехода к первому вопросу
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleAtTheTopButton(String chatId, UserData userData) {
        Quiz quiz = userData.getCurrentQuiz();
        if (quiz == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна", null, "menu")
            };
        }

        // Переходим к первому вопросу с сохраненными ответами
        quiz.goToFirstQuestion();
        String firstQuestion = quiz.getCurrentQuestionText();
        userData.setCurrentQuiz(quiz);

        return new Content[] {
                new Content(true, chatId, firstQuestion, null, "test_answer")
        };
    }

    /**
     * Обрабатывает навигационные кнопки в выборе темы
     * @param callbackData данные callback
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleTopicNavigationButtons(String callbackData, String chatId, UserData userData) {
        if (userData.getTopicSelector() != null && "topic_selection".equals(userData.getState())) {
            if (callbackData.equals("topic_forwards_button")) {
                userData.getTopicSelector().next();
            } else {
                userData.getTopicSelector().previous();
            }
            String displayMessage = userData.getTopicSelector().getDisplayMessage();
            return new Content[] {
                    new Content(true, chatId, displayMessage, null, "choice_quiz")
            };
        }
        return new Content[] {
                new Content(true, chatId, "❌ Навигация недоступна", null, "menu")
        };
    }

    /**
     * Обрабатывает навигационные кнопки в викторине
     * @param callbackData данные callback
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleQuizNavigationButtons(String callbackData, String chatId, UserData userData) {
        Quiz quiz = userData.getCurrentQuiz();
        if (quiz == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна", null, "menu")
            };
        }

        // Выполняем навигацию
        if (callbackData.equals("quiz_forwards_button")) {
            quiz.nextQuestion();
        } else {
            quiz.previousQuestion();
        }

        String message;
        String keyboardType;

        if (quiz.isOnFinalMessage()) {
            message = quiz.getFinalMessage();
            keyboardType = "final_quiz";
        } else {
            message = quiz.getCurrentQuestionText();
            keyboardType = "test_answer";
        }

        userData.setCurrentQuiz(quiz);

        return new Content[] {
                new Content(true, chatId, message, null, keyboardType)
        };
    }

    /**
     * Обрабатывает кнопки ответов (A, B, C, D) с обновлением сообщения
     * @param callbackData данные callback
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleAnswerButtons(String callbackData, String chatId, UserData userData) {
        if (userData.getCurrentQuiz() != null) {
            String cleanAnswer = callbackData.replace("_button", "");
            return processQuizAnswerWithUpdate(cleanAnswer, chatId, userData);
        } else {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна. Начните викторину сначала.", null, "menu")
            };
        }
    }

    /**
     * Обрабатывает ответ в викторине с обновлением сообщения
     * @param answerText текст ответа
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] processQuizAnswerWithUpdate(String answerText, String chatId, UserData userData) {
        Quiz quiz = userData.getCurrentQuiz();

        if (quiz == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна. Используйте /start для начала викторины.")
            };
        }

        String cleanAnswer = answerText.replace("_button", "");
        String resultMessage = quiz.processAnswer(cleanAnswer);

        String nextMessage;
        String keyboardType;

        if (quiz.isOnFinalMessage()) {
            // Это был ответ на последний вопрос - переходим к финальному сообщению
            nextMessage = quiz.getFinalMessage();
            keyboardType = "final_quiz";
        } else {
            // Показываем обычный вопрос
            nextMessage = quiz.getCurrentQuestionText();
            keyboardType = "test_answer";
        }

        userData.setCurrentQuiz(quiz);

        return new Content[] {
                new Content(true, chatId, resultMessage),
                new Content(true, chatId, nextMessage, null, keyboardType)
        };
    }

    /**
     * Обрабатывает команды пользователя
     * @param command текст команды
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleCommand(String command, String chatId, UserData userData) {
        return switch (command) {
            case "/start" -> handleStartCommand(chatId, userData);
            case "/help" -> handleHelpCommand(chatId);
            case "/leaderboard" -> handleLeaderboardCommand(chatId);
            case "/menu" -> handleMenuCommand(chatId, userData);
            default -> {
                System.out.println("[PRODUCER] Неизвестная команда: " + command);
                yield new Content[]{
                        new Content(true, chatId, "Неизвестная команда. Используйте /help для списка команд.")
                };
            }
        };
    }

    /**
     * Обрабатывает текстовые сообщения от пользователя
     * @param messageText текст сообщения
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleTextMessage(String messageText, String chatId, UserData userData) {
        if (userData.getCurrentQuiz() != null) {
            return processQuizAnswerWithUpdate(messageText, chatId, userData);
        }

        return new Content[] {
                new Content(true, chatId, "Викторина не активна. Используйте команды:\n/start - начать викторину\n/help - помощь")
        };
    }

    /**
     * Обрабатывает команду /start
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleStartCommand(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка команды /start");
        userData.setState("menu");
        return new Content[] {
                new Content(true, chatId, START_MESSAGE),
                new Content(true, chatId, MENU_MESSAGE, null, "menu")
        };
    }

    /**
     * Обрабатывает команду /help
     * @param chatId идентификатор чата
     * @return массив контента для ответа
     */
    private Content[] handleHelpCommand(String chatId) {
        System.out.println("[PRODUCER] Обработка команды помощи");
        return new Content[] { new Content(true, chatId, HELP_MESSAGE) };
    }

    /**
     * Обрабатывает команду /leaderboard
     * @param chatId идентификатор чата
     * @return массив контента для ответа
     */
    private Content[] handleLeaderboardCommand(String chatId) {
        System.out.println("[PRODUCER] Обработка команды лидерборда");
        String leaderboard = users.getFormattedLeaderboard();
        return new Content[] { new Content(true, chatId, leaderboard) };
    }

    /**
     * Обрабатывает команду /menu
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleMenuCommand(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка команды меню");
        userData.setState("menu");
        return new Content[] {
                new Content(true, chatId, MENU_MESSAGE, null, "menu")
        };
    }

    /**
     * Запускает выбор темы
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] startTopicSelection(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Запуск выбора темы для " + chatId);

        DiskMemory memory = new DiskMemory();
        memory.reConnect("/choose.json");
        memory.read();

        if (!memory.hasData() || memory.getData().length == 0) {
            System.err.println("[PRODUCER] Ошибка: choose.json не загружен или пуст");
            return new Content[] {
                    new Content(true, chatId, "❌ Нет доступных тем для викторины", null, "menu")
            };
        }

        TopicSelector topicSelector = new TopicSelector();
        topicSelector.initializeFromMemory(memory);
        userData.setTopicSelector(topicSelector);
        userData.setState("topic_selection");

        String displayMessage = topicSelector.getDisplayMessage();
        System.out.println("[PRODUCER] Пользователь " + chatId + " начал выбор темы, доступно тем: " + topicSelector.getTopicCount());

        return new Content[] {
                new Content(true, chatId, displayMessage, null, "choice_quiz")
        };
    }

    /**
     * Запускает викторину с выбранной темой
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] startQuizWithSelectedTopic(String chatId, UserData userData) {
        if (userData.getTopicSelector() == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Тема не выбрана", null, "menu")
            };
        }

        String selectedTopicFileName = userData.getTopicSelector().getCurrentTopic();
        String displayMessage = userData.getTopicSelector().getDisplayMessage();
        String[] lines = displayMessage.split("\n");
        String selectedTopicDisplayName = lines.length >= 3 ? lines[2].trim() : selectedTopicFileName;

        System.out.println("[PRODUCER] Запуск викторины по теме: " + selectedTopicFileName);

        DiskMemory memory = new DiskMemory();
        memory.reConnect("/" + selectedTopicFileName + ".json");
        memory.read();

        if (!memory.hasData() || memory.getData().length == 0) {
            return new Content[] {
                    new Content(true, chatId, "❌ Ошибка загрузки темы: " + selectedTopicDisplayName, null, "menu")
            };
        }

        userData.setCurrentQuiz(new Quiz(memory));
        userData.setState("quiz");
        userData.setTopicSelector(null);

        String firstQuestion = userData.getCurrentQuiz().getCurrentQuestionText();

        return new Content[] {
                new Content(true, chatId, "🎯 Выбрана тема: " + selectedTopicDisplayName),
                new Content(true, chatId, firstQuestion, null, "test_answer")
        };
    }

    /**
     * Завершает викторину и показывает результаты
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    private Content[] handleQuizCompletion(String chatId, UserData userData) {
        Quiz quiz = userData.getCurrentQuiz();
        if (quiz == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна", null, "menu")
            };
        }
        String results = quiz.getResults();
        users.updateUserScore(chatId, quiz.getScore());

        // Проверяем, установлено ли имя для лидерборда
        boolean hasLeaderboardName = userData.getLeaderboardName() != null && !userData.getLeaderboardName().isEmpty();

        userData.setCurrentQuiz(null);

        if (!hasLeaderboardName) {
            // Если имя не установлено - предлагаем добавиться в лидерборд
            userData.setState("waiting_leaderboard_name");
            return new Content[] {
                    new Content(true, chatId, results),
                    new Content(true, chatId, """
                            🏆 Поздравляем с завершением викторины!
                            
                            Чтобы попасть в таблицу лидеров, введите ваше имя (до 20 символов):""")
            };
        } else {
            // Если имя уже установлено - просто показываем результаты и лидерборд
            userData.setState("menu");
            return new Content[] {
                    new Content(true, chatId, results),
                    new Content(true, chatId, users.getFormattedLeaderboard(), "go_menu"),
            };
        }
    }

    /**
     * Получает сервис клавиатур
     * @return сервис клавиатур
     */
    public KeyboardService getKeyboardService() {
        return keyboardService;
    }
}