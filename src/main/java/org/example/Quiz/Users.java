package org.example.Quiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Класс для управления пользователями викторины.
 * Обеспечивает хранение, загрузку и сохранение данных пользователей в JSON формате.
 * Автоматически сохраняет данные при завершении работы приложения.
 */
public class Users {
    private Map<String, UserData> users;
    private static final String filePath = "users_data.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Конструктор класса Users.
     * Инициализирует коллекцию пользователей, настраивает ObjectMapper
     * и загружает данные с диска. Регистрирует shutdown hook для автоматического сохранения.
     */
    public Users() {
        users = new HashMap<>();

        // Настройка ObjectMapper
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
    }

    /**
     * Проверяет существование пользователя с указанным chatId.
     * @param chatId идентификатор чата пользователя
     * @return true если пользователь существует, false в противном случае
     */
    public boolean has(String chatId) {
        return users.containsKey(chatId);
    }

    /**
     * Получает пользователя по chatId. Если пользователь не существует - создает нового.
     * @param chatId идентификатор чата пользователя
     * @return объект UserData для указанного пользователя
     */
    public UserData getOrCreate(String chatId) {
        if (!has(chatId)) {
            UserData newUser = new UserData();
            users.put(chatId, newUser);
            System.out.println("[USERS] Создан новый пользователь: " + chatId);
        }
        return users.get(chatId);
    }

    /**
     * Получает пользователя по chatId.
     * @param chatId идентификатор чата пользователя
     * @return объект UserData или null если пользователь не найден
     */
    public UserData get(String chatId) {
        return users.get(chatId);
    }

    /**
     * Устанавливает имя пользователя для отображения в лидерборде.
     *
     * @param chatId идентификатор чата пользователя
     * @param name   имя для лидерборда
     */
    public void setLeaderboardName(String chatId, String name) {
        UserData user = users.get(chatId);
        if (user != null) {
            user.setLeaderboardName(name);
            System.out.println("[USERS] Установлено имя для лидерборда: " + chatId + " -> " + name);
        }
    }

    /**
     * Обновляет счет пользователя после завершения викторины.
     * Увеличивает общий счет и добавляет запись в историю результатов.
     * @param chatId идентификатор чата пользователя
     * @param quizScore количество баллов, полученных в викторине
     */
    public void updateUserScore(String chatId, int quizScore) {
        UserData user = users.get(chatId);
        if (user != null) {
            int oldScore = user.getScore();
            user.setScore(oldScore + quizScore);

            String quizId = "quiz_" + System.currentTimeMillis();
            user.getAllScore().put(quizId, quizScore);

            System.out.println("[USERS] Обновлен счет пользователя " + chatId +
                    ": +" + quizScore + " баллов (было: " + oldScore + ", стало: " + user.getScore() + ")");
        }
    }

    /**
     * Получает топ-5 пользователей по общему количеству баллов.
     * В рейтинг попадают только пользователи с установленным именем для лидерборда.
     * @return список UserData отсортированный по убыванию счета (максимум 5 элементов)
     */
    public List<UserData> getLeaderboard() {
        List<UserData> result = new ArrayList<>();

        // Собираем пользователей с установленными именами
        for (UserData user : users.values()) {
            if (user.getLeaderboardName() != null && !user.getLeaderboardName().isEmpty()) {
                result.add(user);
            }
        }

        // Сортируем по убыванию счета
        result.sort((user1, user2) -> Integer.compare(user2.getScore(), user1.getScore()));

        return result.size() > 5 ? result.subList(0, 5) : result;
    }

    /**
     * Форматирует лидерборд для отображения пользователю.
     * Включает только пользователей с установленными именами.
     * @return отформатированная строка с лидербордом
     */
    public String getFormattedLeaderboard() {
        List<UserData> leaderboard = getLeaderboard();

        if (leaderboard.isEmpty()) {
            return """
                    🏆 **ТОП-5 ЛИДЕРОВ**
                    
                    Пока никто не установил имя для лидерборда!
                    
                    Чтобы попасть в лидерборд, завершите викторину и установите имя через команду /leaderboard""";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 **ТОП-5 ЛИДЕРОВ** 🏆\n\n");

        String[] medals = {"🥇", "🥈", "🥉", "4️⃣", "5️⃣"};

        for (int i = 0; i < leaderboard.size(); i++) {
            UserData user = leaderboard.get(i);
            String medal = i < medals.length ? medals[i] : (i + 1) + "️⃣";

            sb.append(medal).append(" **").append(user.getLeaderboardName())
                    .append("** - ").append(user.getScore())
                    .append(" баллов\n");
        }

        return sb.toString();
    }

    /**
     * Сохраняет всех пользователей на диск в JSON формате.
     * Выполняет проверку данных перед сохранением и логирует процесс.
     * В случае ошибки выводит подробную информацию в консоль.
     */
    public void saveToDisk() {
        try {
            System.out.println("[USERS-DEBUG] Начало сохранения, users size: " + users.size());

            if (users.isEmpty()) {
                System.out.println("[USERS-DEBUG] Нет пользователей для сохранения");
                return;
            }

            for (Map.Entry<String, UserData> entry : users.entrySet()) {
                UserData user = entry.getValue();
                System.out.println("[USERS-DEBUG] " + entry.getKey() +
                        " -> state: " + user.getState() +
                        ", score: " + user.getScore());
            }

            String json = mapper.writeValueAsString(users);
            System.out.println("[USERS-DEBUG] Сгенерированный JSON: " + json);

            // Простая запись в файл
            try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
                writer.print(json);
            }

            System.out.println("[USERS] Данные успешно сохранены");

        } catch (Exception e) {
            System.err.println("[USERS-ERROR] Критическая ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает пользователей с диска из JSON файла.
     * В случае повреждения файла автоматически создает новую коллекцию
     * и удаляет поврежденный файл.
     */
    private void loadFromDisk() {
        try {
            if (Files.exists(Paths.get(filePath))) {
                String json = new String(Files.readAllBytes(Paths.get(filePath)));

                // Проверяем валидность JSON
                if (json.trim().isEmpty()) {
                    System.out.println("[USERS] Файл пуст, создана новая коллекция");
                    users = new HashMap<>();
                    return;
                }

                users = mapper.readValue(json, new TypeReference<Map<String, UserData>>() {});
                System.out.println("[USERS] Данные пользователей загружены из файла: " + filePath +
                        ", пользователей: " + users.size());
            } else {
                System.out.println("[USERS] Файл с данными пользователей не найден, создана новая коллекция");
                users = new HashMap<>();
            }
        } catch (Exception e) {
            System.err.println("[USERS] Ошибка при загрузке данных пользователей: " + e.getMessage());
            System.out.println("[USERS] Удаляю поврежденный файл и создаю новую коллекцию");

            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException deleteError) {
                System.err.println("[USERS] Не удалось удалить поврежденный файл: " + deleteError.getMessage());
            }

            users = new HashMap<>();
        }
    }
}