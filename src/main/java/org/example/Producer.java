package org.example;

import org.example.DataMessage.Content;
import org.example.DataMessage.KeyboardService;
import org.example.DataMessage.MessageQueue;
import org.example.ModeGame.Duel.DuelMatchmaker;
import org.example.ModeGame.Duel.DuelPair;
import org.example.ModeGame.Duel.Timer.DuelTimeoutManager;
import org.example.ModeGame.Duel.Timer.TimeoutNotifier;
import org.example.ModeGame.DuelMode;
import org.example.ModeGame.ModeSelector;
import org.example.ModeGame.SoloMode;
import org.example.Quiz.*;
import org.example.Quiz.Memory.AiMemory;
import org.example.Quiz.Memory.DiskMemory;
import org.example.TopicSelector.TopicSelector;
import org.example.GenerationQuiz.CreateQuiz;
import org.example.OpenRouter.OpenRouterClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.DataMessage.Constants.*;

/**
 * Класс-производитель для обработки пользовательского контента и управления состоянием пользователей.
 * Обрабатывает входящие сообщения и возвращает соответствующий контент для ответа.
 * Управляет состояниями пользователей, обработкой команд и навигацией по викторине.
 */
public class Producer {
    private final Users users;
    private final KeyboardService keyboardService;
    private final CreateQuiz createQuiz;
    private final MessageQueue messageQueue;
    private DuelTimeoutManager timeoutManager;
    private DuelMatchmaker matchmaker;

    // Кеш для вопросов дуэли: duelId -> AiMemory
    private final Map<String, AiMemory> duelQuestionsCache = new ConcurrentHashMap<>();

    private static final String DUEL_START_TEMPLATE =
            "⚔️ *ДУЭЛЬ НАЧАЛАСЬ!*\n" +
                    "Отвечайте быстро и точно! Удачи! 🚀";

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
            🚀 *Создай свою уникальную викторину* с помощью нейросети
            📚 *Или выбери готовые темы* из нашей коллекции
            
            🎯 Что будем делать?""";


    private static final String START_MENU_MASSAGE = """
            🎪 *Вы находитесь в главном меню!*
            
            С кем вы сразитесь сегодня?
            ⚔️ Дуэль — чтобы победить другого.
            🧠 Соло — чтобы победить себя.
            """;

    private static final String REGISTRATION_MESSAGE = """
                ⚠️ *Требуется регистрация!*
                
                Чтобы пользоваться ботом, необходимо зарегистрироваться.
                Введите ваше имя (до 20 символов):
                📝 Пример: "Алексей", "QuizMaster", "Гений_2024"
                
                После регистрации вы сможете участвовать в викторинах и лидерборде!
                """;

    /**
     * Конструктор по умолчанию.
     * Инициализирует менеджер пользователей и сервис клавиатур.
     */
    public Producer(Users users, String OpenRouterToken) {
        this.users = users;
        this.keyboardService = new KeyboardService();
        this.messageQueue = new MessageQueue();

        // Инициализация генератора викторин
        OpenRouterClient openRouterClient = new OpenRouterClient(OpenRouterToken);
        this.createQuiz = new CreateQuiz(openRouterClient);

        System.out.println("[PRODUCER] Producer создан с поддержкой очереди сообщений");
    }

    public void startInitTimeoutNotifier() {
        // Инициализация нотификатора таймаутов
        initTimeoutNotifier();
    }

    public void setDuelTimeoutManager(DuelTimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    public void setDuelMatchmaker(DuelMatchmaker matchmaker) {
        this.matchmaker = matchmaker;
    }

    public DuelTimeoutManager getDuelTimeoutManager() {
        return this.timeoutManager;
    }

    public DuelMatchmaker getDuelMatchmaker() {
        return this.matchmaker;
    }

    /**
     * Инициализирует нотификатор для обработки таймаутов.
     */
    private void initTimeoutNotifier() {
        timeoutManager.setNotifier(new TimeoutNotifier() {
            @Override
            public void notifySearchTimeout(String chatId, String topic) {
                System.out.println("[PRODUCER] Таймаут поиска для " + chatId + ", тема: " + topic);

                UserData userData = users.getOrCreate(chatId);
                userData.setState("menu");
                userData.clearDuelData();

                // Cообщение о таймауте поиска
                String message = "⏰ *Поиск оппонента отменен по таймауту*\n\n" +
                        "Не удалось найти оппонента в течение 2 минут.\n" +
                        "🎯 Тема: " + topic + "\n\n" +
                        "Попробуйте поискать чуть позже или выберите другую тему.";

                Content content = new Content(true, chatId, message, null, "go_menu");
                messageQueue.addMessage(content);

                System.out.println("[PRODUCER] Сообщение о таймауте поиска добавлено в очередь для " + chatId);
            }

            @Override
            public void notifyDuelTimeout(String duelId, String player1ChatId, String player2ChatId) {
                System.out.println("[PRODUCER] Таймаут дуэли: " + duelId);

                // Обрабатываем таймаут дуэли
                handleDuelTimeout(duelId, player1ChatId, player2ChatId);
            }
        });
    }

    /**
     * Обрабатывает таймаут дуэли (ожидание второго игрока).
     */
    private void handleDuelTimeout(String duelId, String player1ChatId, String player2ChatId) {
        try {
            UserData player1Data = users.getOrCreate(player1ChatId);
            UserData player2Data = users.getOrCreate(player2ChatId);

            DuelPair pair = matchmaker.getPairForPlayer(player1ChatId);

            if (pair == null) {
                System.out.println("[PRODUCER] Дуэль не найдена: " + duelId);
                return;
            }

            // Получаем результаты игрока, который завершил первым
            org.example.ModeGame.Duel.PlayerResults player1Results = pair.getPlayerResults(player1ChatId);

            if (player1Results == null || !player1Results.hasResults()) {
                System.out.println("[PRODUCER] Результаты игрока 1 не найдены для дуэли: " + duelId);
                return;
            }

            String player1Name = player1Data.getLeaderboardName();
            String player2Name = player2Data.getLeaderboardName();

            // Сообщение для игрока 1 (победитель по таймауту)
            String player1Message = String.format(
                    """
                            ⏰ *ПРОТИВНИК НЕ УСПЕЛ!*
                            
                            Ваш оппонент (%s) не завершил дуэль в течение 2-х минут.
                            Ваши результаты:
                            • Правильных ответов: %d/5
                            • Время: %.1f сек
                            
                            ⚔️ *Вы получаете победу по умолчанию!*
                            🏆 *Начислено баллов в общий счет: %d*""",
                    player2Name,
                    player1Results.getScore(),
                    player1Results.getTime() / 1000.0,
                    player1Results.getScore()
            );

            // Добавляем очки в общий счет
            users.updateUserScore(player1ChatId, player1Results.getScore());

            // Сообщение для игрока 2 (проигравший по таймауту)
            String player2Message = String.format(
                    """
                            💀⏰ *ВЫ НЕ УСПЕЛИ!*
                            
                            Вы не завершили дуэль в течение 2-х минут.
                            Ваш оппонент (%s) уже давно закончил и получил победу.
                            
                            😞 *Вы получаете 0 баллов за эту дуэль*
                            📉 *В следующий раз отвечайте быстрее!*""",
                    player1Name
            );

            // Добавляем сообщения в очередь
            Content content1 = new Content(true, player1ChatId, player1Message, null, "go_menu");
            Content content2 = new Content(true, player2ChatId, player2Message, null, "go_menu");

            messageQueue.addMessage(content1);
            messageQueue.addMessage(content2);

            // Очищаем данные дуэли
            player1Data.clearDuelData();
            player2Data.clearDuelData();
            player1Data.setState("menu");
            player2Data.setState("menu");

            // Очищаем кеш вопросов и удаляем пару
            clearDuelCache(duelId);
            matchmaker.removeTimedOutDuel(duelId);

            System.out.println("[PRODUCER] Сообщения о таймауте дуэли добавлены в очередь для обоих игроков");

        } catch (Exception e) {
            System.err.println("[PRODUCER] Ошибка обработки таймаута дуэли: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Возвращает все сообщения, накопленные в очереди (от таймаутов).
     * @return массив сообщений для отправки
     */
    public Content[] getQueuedMessages() {
        java.util.List<Content> messages = messageQueue.getAllMessages();
        System.out.println("[PRODUCER] Получено " + messages.size() + " сообщений из очереди");
        return messages.toArray(new Content[0]);
    }

    /**
     * Проверяет, есть ли сообщения в очереди.
     * @return true если есть сообщения для отправки
     */
    public boolean hasQueuedMessages() {
        return messageQueue.hasMessages();
    }

    /**
     * Обрабатывает ситуацию, когда поиск дуэли завершился по таймауту.
     */
    private Content[] handleSearchTimeout(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка таймаута поиска для " + chatId);

        userData.setState("menu");
        userData.clearDuelData();

        String message =
                """
                        ⏰ *Поиск оппонента отменен по таймауту*
                        
                        Не удалось найти оппонента в течение 2 минут.
                        Попробуйте поискать чуть позже или выберите другую тему.""";

        return new Content[] {
                new Content(true, chatId, message, null,"go_menu"),
        };
    }

    /**
     * Обрабатывает пользовательское сообщение в зависимости от состояния пользователя.
     */
    private Content[] processUserMessage(Content content, String chatId, UserData userData) {
        String messageText = content.getText();

        // Проверка на просроченный поиск дуэли
        if ("duel_searching".equals(userData.getState())) {
            if (!timeoutManager.hasActiveSearch(chatId)) {
                // Таймаут поиска истек
                return handleSearchTimeout(chatId, userData);
            }
        }

        // Обработка состояния ожидания имени для лидерборда
        if ("waiting_leaderboard_name".equals(userData.getState())) {
            return handleLeaderboardName(chatId, messageText, userData);
        }

        // Обработка состояния ожидания темы для генерации
        if ("waiting_generation_topic".equals(userData.getState())) {
            ModeSelector currentMode = userData.getCurrentMode();

            if (currentMode instanceof DuelMode) {
                return handleDuelGenerationTopic(chatId, messageText, userData);
            } else {
                return handleGenerationTopic(chatId, messageText, userData);
            }
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
                    new Content(true, chatId, "✅ Имя \"" + name + "\" успешно установлено!"),
                    new Content(true, chatId, START_MENU_MASSAGE, null, "mode_selection")
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
            Quiz generatedQuiz = new Quiz(generatedMemory);
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
     * Обрабатывает ввод темы для генерации дуэли
     */
    private Content[] handleDuelGenerationTopic(String chatId, String topic, UserData userData) {
        topic = topic.trim();

        if (topic.isEmpty() || topic.length() > 100) {
            return new Content[] {
                    new Content(true, chatId, """
                        ❌ Неверная тема!
                        Тема должна быть от 1 до 100 символов.
                        Пожалуйста, введите тему еще раз:""")
            };
        }

        // Получаем режим дуэли
        ModeSelector currentMode = userData.getCurrentMode();
        if (currentMode instanceof DuelMode duelMode) {

            // Сразу начинаем поиск
            return duelMode.startGeneratedDuelSearch(topic);
        }

        return new Content[] {
                new Content(true, chatId, "❌ Ошибка режима", null, "menu")
        };
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
     * Проверяет доступ пользователя к функциям бота.
     * Если имя не установлено - блокирует все действия кроме регистрации.
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return null если доступ разрешен, массив Content с сообщением о регистрации если имя не установлено
     */
    private Content[] checkUserRegistration(String chatId, UserData userData) {
        String leaderboardName = userData.getLeaderboardName();
        if (leaderboardName == null || leaderboardName.isEmpty()) {
            userData.setState("waiting_leaderboard_name");
            return new Content[] {
                    new Content(true, chatId, REGISTRATION_MESSAGE)
            };
        }
        return null;
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

        // ВАЖНО: Обновляем время активности для дуэли
        // Если пользователь в состоянии ожидания оппонента и нажимает любую кнопку
        if ("duel_waiting_opponent".equals(userData.getState())) {
            DuelPair pair = matchmaker.getPairForPlayer(chatId);
            if (pair != null) {
                pair.updateLastActivityTime(chatId);
                System.out.println("[PRODUCER] Обновлено время активности для игрока " + chatId +
                        " в дуэли " + pair.getDuelId());
            }
        }

        // Проверяем регистрацию для всех действий кроме /start
        if (!"menu".equals(userData.getState()) && !"waiting_leaderboard_name".equals(userData.getState())) {
            Content[] registrationCheck = checkUserRegistration(chatId, userData);
            if (registrationCheck != null) {
                return registrationCheck;
            }
        }

        // Навигационные кнопки в выборе темы
        switch (callbackData) {
            case TOPIC_FORWARDS_BUTTON, TOPIC_BACKWARDS_BUTTON -> {
                return handleTopicNavigationButtons(callbackData, chatId, userData);
            }

            // Навигационные кнопки в викторине
            case QUIZ_FORWARDS_BUTTON, QUIZ_BACKWARDS_BUTTON -> {
                return handleQuizNavigationButtons(callbackData, chatId, userData);
            }

            // Кнопки ответов
            case A_BUTTON, B_BUTTON, C_BUTTON, D_BUTTON -> {
                return handleAnswerButtons(callbackData, chatId, userData);
            }

            // Кнопка перехода к первому вопросу
            case AT_THE_TOP_BUTTON -> {
                return handleAtTheTopButton(chatId, userData);
            }

            // Остальные кнопки
            default -> {
                switch (callbackData) {
                    case QUIZ_BUTTON:
                        // Проверяем регистрацию
                        Content[] check = checkUserRegistration(chatId, userData);
                        if (check != null) return check;
                        return startTopicSelection(chatId, userData);

                    case PLAY_BUTTON:
                        check = checkUserRegistration(chatId, userData);
                        if (check != null) return check;
                        return startQuizWithSelectedTopic(chatId, userData);

                    case MENU_BUTTON:
                        userData.setState("menu");
                        userData.setCurrentQuiz(null);
                        userData.setTopicSelector(null);
                        userData.clearDuelData();
                        return handleStartMenuCommand(chatId, userData);

                    case DUEL_BUTTON:
                        check = checkUserRegistration(chatId, userData);
                        if (check != null) return check;
                        return handleDuelButton(chatId, userData);

                    case SOLO_BUTTON:
                        check = checkUserRegistration(chatId, userData);
                        if (check != null) return check;
                        return handleSoloButton(chatId, userData);

                    case GENERATION_BUTTON:
                        check = checkUserRegistration(chatId, userData);
                        if (check != null) return check;
                        return generationQuiz(chatId, userData);

                    case END_QUIZ_BUTTON:
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
     * Обрабатывает выбор режима "Соло".
     *
     * @param chatId ID чата пользователя
     * @param userData данные пользователя
     * @return контент для ответа
     */
    private Content[] handleSoloButton(String chatId, UserData userData) {
        ModeSelector soloMode = new SoloMode(this, chatId, userData);
        userData.setCurrentMode(soloMode);
        return soloMode.handleModeSelection();
    }

    /**
     * Обрабатывает выбор режима "Дуэль".
     *
     * @param chatId ID чата пользователя
     * @param userData данные пользователя
     * @return контент для ответа
     */
    private Content[] handleDuelButton(String chatId, UserData userData) {
        ModeSelector duelMode = new DuelMode(this, chatId, userData);
        userData.setCurrentMode(duelMode);
        return duelMode.handleModeSelection();
    }

    /**
     * Запускает генерацию викторины через ИИ.
     * Переводит пользователя в состояние ожидания ввода темы.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа с инструкциями по вводу темы
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
     * Обрабатывает кнопку перехода к первому вопросу викторины.
     * Возвращает пользователя к началу викторины с сохраненными ответами.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа с первым вопросом викторины
     */
    private Content[] handleAtTheTopButton(String chatId, UserData userData) {
        Quiz quiz = userData.getCurrentQuiz();
        if (quiz == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Викторина не активна", null, null)
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
            if (callbackData.equals(TOPIC_FORWARDS_BUTTON)) {
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
        if (callbackData.equals(QUIZ_FORWARDS_BUTTON)) {
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
            case "/menu" -> handleStartMenuCommand(chatId, userData);
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
        // Если пользователь в состоянии ожидания имени - обрабатываем как имя
        if ("waiting_leaderboard_name".equals(userData.getState())) {
            return handleLeaderboardName(chatId, messageText, userData);
        }

        // Проверяем регистрацию для остальных действий
        Content[] registrationCheck = checkUserRegistration(chatId, userData);
        if (registrationCheck != null) {
            return registrationCheck;
        }

        if (userData.getCurrentQuiz() != null) {
            return processQuizAnswerWithUpdate(messageText, chatId, userData);
        }

        return new Content[] {
                new Content(true, chatId, "Используйте кнопки меню для навигации.")
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
        userData.clearDuelData();

        Content[] response = new Content[] {
                new Content(true, chatId, START_MESSAGE),
                new Content(true, chatId, START_MENU_MASSAGE, null, "mode_selection")
        };

        String leaderboardName = userData.getLeaderboardName();
        if (leaderboardName == null || leaderboardName.isEmpty()) {
            // Если имени нет - запрашиваем регистрацию
            userData.setState("waiting_leaderboard_name");
            return new Content[] {
                    new Content(true, chatId, START_MESSAGE),
                    new Content(true, chatId, REGISTRATION_MESSAGE)
            };
        }

        return response;
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

        // Показываем лидерборд только если пользователь зарегистрирован
        UserData userData = users.getOrCreate(chatId);
        String leaderboardName = userData.getLeaderboardName();

        if (leaderboardName == null || leaderboardName.isEmpty()) {
            userData.setState("waiting_leaderboard_name");
            return new Content[] {
                    new Content(true, chatId, REGISTRATION_MESSAGE)
            };
        }

        String leaderboard = users.getFormattedLeaderboard();
        return new Content[] { new Content(true, chatId, leaderboard) };
    }

    /**
     * Обрабатывает команду /menu
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа
     */
    public Content[] handleStartMenuCommand(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка команды меню");

        // Проверяем регистрацию
        String leaderboardName = userData.getLeaderboardName();
        if (leaderboardName == null || leaderboardName.isEmpty()) {
            // Если не авторизован - просим авторизацию
            userData.setState("waiting_leaderboard_name");
            return new Content[] {
                    new Content(true, chatId,REGISTRATION_MESSAGE)
            };
        }

        // Если авторизован - показываем меню
        userData.setState("menu");
        userData.clearDuelData();
        return new Content[] {
                new Content(true, chatId, START_MENU_MASSAGE, null, "mode_selection")
        };
    }

    /**
     * Запускает выбор темы викторины из доступных вариантов.
     * Инициализирует навигацию по темам для пользователя.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа со списком тем
     */
    public Content[] startTopicSelection(String chatId, UserData userData) {
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
     * Запускает викторину с выбранной пользователем темой.
     * Загружает вопросы по выбранной теме и начинает викторину.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа с первым вопросом викторины
     */
    public Content[] startQuizWithSelectedTopic(String chatId, UserData userData) {
        if (userData.getTopicSelector() == null) {
            return new Content[] {
                    new Content(true, chatId, "❌ Тема не выбрана", null, "menu")
            };
        }

        // Проверяем режим
        ModeSelector currentMode = userData.getCurrentMode();

        if (currentMode instanceof DuelMode duelMode) {
            // ДУЭЛЬ: начинаем поиск по выбранной теме
            String selectedTopicFileName = userData.getTopicSelector().getCurrentTopic();
            return duelMode.startLocalDuelSearch(selectedTopicFileName);
        }

        // СОЛО: обычная логика
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

        // Проверяем режим викторины
        if ("duel".equals(userData.getQuizMode())) {
            // Завершаем дуэль
            return handleDuelCompletion(chatId, userData, quiz);
        } else {
            // Соло режим
            return handleSoloCompletion(chatId, userData, results);
        }
    }

    /**
     * Завершает соло викторину и обрабатывает результаты.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @param results текстовые результаты викторины
     * @return массив контента для ответа с результатами
     */
    private Content[] handleSoloCompletion(String chatId, UserData userData, String results) {
        userData.setCurrentQuiz(null);
        userData.setState("menu");

        return new Content[] {
                new Content(true, chatId, results, null, "go_menu")
        };
    }

    /**
     * Завершает дуэль и обрабатывает результаты.
     * Вычисляет финальное время прохождения и передает результаты в режим дуэли.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @param quiz объект викторины с результатами игрока
     * @return массив контента для ответа с результатами дуэли
     */
    private Content[] handleDuelCompletion(String chatId, UserData userData, Quiz quiz) {
        int playerScore = quiz.getScore();

        long playerTime = userData.markDuelCompletion();

        System.out.println("[PRODUCER] Дуэль завершена для " + chatId +
                ": score=" + playerScore + "/5, time=" + playerTime + "мс (" + (playerTime/1000.0) + "сек)");

        String results = quiz.getResults();

        // Получаем DuelMode для обработки результатов
        ModeSelector currentMode = userData.getCurrentMode();
        if (currentMode instanceof DuelMode) {
            DuelMode duelMode = (DuelMode) currentMode;

            userData.setCurrentQuiz(null);
            userData.setState("duel_results");

            // Передаем СКОР и ФИНАЛЬНОЕ ВРЕМЯ ЭТОГО игрока
            return duelMode.finishDuel(playerScore, playerTime);
        } else {
            userData.setState("menu");
            return new Content[] {
                    new Content(true, chatId, "🏆 *ДУЭЛЬ ЗАВЕРШЕНА!*\n\n" + results),
                    handleMenuCommand(chatId, userData)[0]
            };
        }
    }

    /**
     * Запускает дуэль с локальной темой для обоих игроков.
     * Создает отдельные экземпляры викторины для каждого игрока из одной темы.
     *
     * @param player1ChatId идентификатор чата первого игрока
     * @param player1Data данные первого игрока
     * @param player2ChatId идентификатор чата второго игрока
     * @param player2Data данные второго игрока
     * @param topic название темы викторины
     * @param duelId уникальный идентификатор дуэли
     * @return массив контента для ответа обоим игрокам с началом дуэли
     */
    public Content[] startDuelQuizWithTopicForBothPlayers(String player1ChatId, UserData player1Data,
                                                          String player2ChatId, UserData player2Data,
                                                          String topic, String duelId) {
        System.out.println("[PRODUCER] Запуск дуэли для обоих игроков, тема: " + topic + ", duelId: " + duelId);

        DiskMemory memory = new DiskMemory();
        memory.reConnect("/" + topic + ".json");
        memory.read();

        if (!memory.hasData() || memory.getData().length == 0) {
            return new Content[] {
                    new Content(true, player1ChatId, "❌ Ошибка загрузки темы для дуэли: " + topic),
                    new Content(true, player2ChatId, "❌ Ошибка загрузки темы для дуэли: " + topic)
            };
        }

        // Создаем ОТДЕЛЬНЫЕ объекты памяти для каждого игрока
        DiskMemory memory1 = new DiskMemory();
        memory1.reConnect("/" + topic + ".json");
        memory1.read();

        DiskMemory memory2 = new DiskMemory();
        memory2.reConnect("/" + topic + ".json");
        memory2.read();

        // Создаем ОТДЕЛЬНЫЕ квизы для обоих игроков
        Quiz duelQuiz1 = new Quiz(memory1);
        Quiz duelQuiz2 = new Quiz(memory2);

        // Настраиваем первого игрока
        player1Data.setCurrentQuiz(duelQuiz1);
        player1Data.setState("quiz");
        player1Data.setQuizMode("duel");
        player1Data.setDuelId(duelId);
        player1Data.markDuelStartTime(); // Старт таймера дуэли

        // Настраиваем второго игрока
        player2Data.setCurrentQuiz(duelQuiz2);
        player2Data.setState("quiz");
        player2Data.setQuizMode("duel");
        player2Data.setDuelId(duelId);
        player2Data.markDuelStartTime(); // Старт таймера дуэли

        String firstQuestion = duelQuiz1.getCurrentQuestionText();
        String startMessage = DUEL_START_TEMPLATE;

        return new Content[] {
                new Content(true, player1ChatId, startMessage),
                new Content(true, player1ChatId, firstQuestion, null, "test_answer"),

                new Content(true, player2ChatId, startMessage),
                new Content(true, player2ChatId, firstQuestion, null, "test_answer")
        };
    }

    /**
     * Запускает дуэль с генерацией викторины ИИ для обоих игроков.
     * Использует кеш для хранения сгенерированных вопросов и создания отдельных викторин.
     *
     * @param player1ChatId идентификатор чата первого игрока
     * @param player1Data данные первого игрока
     * @param player2ChatId идентификатор чата второго игрока
     * @param player2Data данные второго игрока
     * @param topic тема для генерации викторины
     * @param duelId уникальный идентификатор дуэли
     * @return массив контента для ответа обоим игрокам с началом дуэли
     */
    public Content[] startDuelQuizGenerationForBothPlayers(String player1ChatId, UserData player1Data,
                                                           String player2ChatId, UserData player2Data,
                                                           String topic, String duelId) {
        try {
            System.out.println("[PRODUCER] Запуск дуэли с генерацией для обоих игроков, тема: " + topic + ", duelId: " + duelId);

            AiMemory generatedMemory = duelQuestionsCache.get(duelId);

            if (generatedMemory == null) {
                // Генерируем викторину с помощью ИИ (ОДИН РАЗ!)
                generatedMemory = createQuiz.generateQuiz(topic);
                duelQuestionsCache.put(duelId, generatedMemory);
                System.out.println("[PRODUCER] Шаблон вопросов сгенерирован и закеширован, duelId: " + duelId);
            } else {
                System.out.println("[PRODUCER] Используем закешированный шаблон вопросов, duelId: " + duelId);
            }

            // СОЗДАЕМ ГЛУБОКИЕ КОПИИ для каждого игрока вплодь до квиз даты
            AiMemory memory1 = generatedMemory.copy();
            AiMemory memory2 = generatedMemory.copy();

            // Проверяем, что это разные объекты
            System.out.println("[PRODUCER] memory1 == memory2: " + (memory1 == memory2));
            System.out.println("[PRODUCER] memory1.getData() == memory2.getData(): " +
                    (memory1.getData() == memory2.getData()));

            if (memory1.getData().length > 0 && memory2.getData().length > 0) {
                System.out.println("[PRODUCER] memory1.getData()[0] == memory2.getData()[0]: " +
                        (memory1.getData()[0] == memory2.getData()[0]));
            }

            // Создаем ОТДЕЛЬНЫЕ квизы для обоих игроков
            Quiz duelQuiz1 = new Quiz(memory1); // Квиз 1 со своей копией памяти
            Quiz duelQuiz2 = new Quiz(memory2); // Квиз 2 со своей копией памяти

            // Настраиваем первого игрока
            player1Data.setCurrentQuiz(duelQuiz1);
            player1Data.setState("quiz");
            player1Data.setQuizMode("duel");
            player1Data.setDuelId(duelId);
            player1Data.markDuelStartTime();

            // Настраиваем второго игрока
            player2Data.setCurrentQuiz(duelQuiz2);
            player2Data.setState("quiz");
            player2Data.setQuizMode("duel");
            player2Data.setDuelId(duelId);
            player2Data.markDuelStartTime();

            String startMessage = DUEL_START_TEMPLATE;
            String firstQuestion1 = duelQuiz1.getCurrentQuestionText();
            String firstQuestion2 = duelQuiz2.getCurrentQuestionText();

            System.out.println("[PRODUCER] Дуэль начата: " +
                    player1ChatId + " и " + player2ChatId +
                    ", шаблон один, но ответы изолированы");

            return new Content[] {
                    new Content(true, player1ChatId, startMessage),
                    new Content(true, player1ChatId, firstQuestion1, null, "test_answer"),
                    new Content(true, player2ChatId, startMessage),
                    new Content(true, player2ChatId, firstQuestion2, null, "test_answer")
            };

        } catch (Exception e) {
            System.err.println("[PRODUCER] Ошибка генерации викторины для дуэли: " + e.getMessage());
            e.printStackTrace();

            // Сбрасываем состояния обоих игроков в случае ошибки
            player1Data.setState("menu");
            player2Data.setState("menu");
            player1Data.clearDuelData();
            player2Data.clearDuelData();

            // Очищаем кеш, если был сохранен
            clearDuelCache(duelId);

            return new Content[] {
                    new Content(true, player1ChatId, "❌ *Ошибка генерации викторины для дуэли*\n\n" +
                            "Не удалось создать викторину по теме: " + topic + "\n" +
                            "Попробуйте начать поиск заново.", null, "menu"),
                    new Content(true, player2ChatId, "❌ *Ошибка генерации викторины для дуэли*\n\n" +
                            "Не удалось создать викторину по теме: " + topic + "\n" +
                            "Попробуйте начать поиск заново.", null, "menu")
            };
        }
    }

    /**
     * Очищает кеш вопросов дуэли по указанному идентификатору.
     * Используется для освобождения памяти после завершения дуэли.
     *
     * @param duelId уникальный идентификатор дуэли, кеш которой нужно очистить
     */
    public void clearDuelCache(String duelId) {
        if (duelId != null) {
            duelQuestionsCache.remove(duelId);
            System.out.println("[PRODUCER] Кеш вопросов дуэли очищен, duelId: " + duelId);
        }
    }

    /**
     * Получает данные пользователя по идентификатору чата.
     * Если пользователь не существует, создает новую запись.
     *
     * @param chatId идентификатор чата пользователя
     * @return объект UserData с данными пользователя
     */
    public UserData getUserData(String chatId) {
        return users.getOrCreate(chatId);
    }

    /**
     * Возвращает менеджер пользователей для управления всеми пользовательскими данными.
     *
     * @return объект Users для управления пользователями
     */
    public Users getUsers() {
        return users;
    }

    /**
     * Получает сервис клавиатур
     * @return сервис клавиатур
     */
    public KeyboardService getKeyboardService() {
        return keyboardService;
    }

    /**
     * Метод для отображения обычного меню (без проверки авторизации).
     * Используется в дуэльном режиме после завершения матча.
     *
     * @param chatId идентификатор чата
     * @param userData данные пользователя
     * @return массив контента для ответа с меню
     */
    public Content[] handleMenuCommand(String chatId, UserData userData) {
        System.out.println("[PRODUCER] Обработка команды меню");
        userData.setState("menu");
        userData.clearDuelData();
        return new Content[] {
                new Content(true, chatId, MENU_MESSAGE, null, "menu")
        };
    }
}