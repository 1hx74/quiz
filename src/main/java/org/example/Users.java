package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Класс для управления пользователями викторины.
 * Обеспечивает хранение, добавление, удаление и поиск пользователей.
 * Автоматически сохраняет данные при завершении работы приложения.
 */
public class Users {
    private Map<String, UserData> users;
    private static final String filePath = "users_data.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public Users() {
        users = new HashMap<>();
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Проверяет существует ли пользователь с указанным chatId
     */
    public boolean has(String chatId) {
        return users.containsKey(chatId);
    }

    /**
     * Получает пользователя по chatId, если пользователя нет - создает нового
     */
    public UserData getOrCreate(String chatId) {
        if (!has(chatId)) {
            UserData newUser = new UserData(chatId); // Используем новый конструктор
            users.put(chatId, newUser);
            System.out.println("[USERS] Создан новый пользователь: " + chatId);
        }
        return users.get(chatId);
    }

    public UserData get(String chatId) {
        return users.get(chatId);
    }

    /**
     * Получает топ-5 пользователей по общему количеству баллов
     */
    public List<UserData> getLeaderboard() {
        List<UserData> result = new ArrayList<>();

        // Собираем пользователей с именами
        for (UserData user : users.values()) {
            if (user.getLeaderboardName() != null) {
                result.add(user);
            }
        }

        // Сортируем по общему счету от большего к меньшему
        result.sort((user1, user2) -> user2.getScore() - user1.getScore());

        // Возвращаем не больше 5
        return result.size() > 5 ? result.subList(0, 5) : result;
    }

    /**
     * Проверяет, попадает ли пользователь в топ-5 по общему счету
     */
    public boolean canEnterLeaderboard(int totalScore) {
        List<UserData> leaderboard = getLeaderboard();
        if (leaderboard.size() < 5) {
            return true;
        }
        return totalScore > leaderboard.get(4).getScore();
    }

    /**
     * Устанавливает имя пользователя для лидерборда
     */
    public boolean setLeaderboardName(String chatId, String name) {
        UserData user = users.get(chatId);
        if (user != null) {
            user.setLeaderboardName(name);
            return true;
        }
        return false;
    }

    /**
     * Получает форматированный лидерборд для отображения
     */
    public String getFormattedLeaderboard() {
        List<UserData> leaderboard = getLeaderboard();

        if (leaderboard.isEmpty()) {
            return "🏆 Лидерборд пуст\nПока никто не установил имя для лидерборда!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 **ТОП-5 ЛИДЕРОВ** 🏆\n\n");

        String[] medals = {"🥇", "🥈", "🥉", "4️⃣", "5️⃣"};

        for (int i = 0; i < leaderboard.size(); i++) {
            UserData user = leaderboard.get(i);
            String medal = i < medals.length ? medals[i] : (i + 1) + "️⃣";

            sb.append(medal).append(" **").append(user.getLeaderboardName())
                    .append("** - ").append(user.getScore()).append(" баллов\n");
        }

        return sb.toString();
    }

    /**
     * Сохраняет всех пользователей на диск в JSON формате
     */
    public void saveToDisk() {
        try {
            String json = mapper.writeValueAsString(users);
            Files.write(Paths.get(filePath), json.getBytes());
            System.out.println("[USERS] Данные пользователей сохранены в файл: " + filePath +
                    ", пользователей: " + users.size());
        } catch (IOException e) {
            System.err.println("[USERS] Ошибка при сохранении данных пользователей: " + e.getMessage());
        }
    }

    /**
     * Загружает пользователей с диска из JSON файла
     */
    private void loadFromDisk() {
        try {
            if (Files.exists(Paths.get(filePath))) {
                String json = new String(Files.readAllBytes(Paths.get(filePath)));
                users = mapper.readValue(json, new TypeReference<Map<String, UserData>>() {});
                System.out.println("[USERS] Данные пользователей загружены из файла: " + filePath +
                        ", пользователей: " + users.size());
            } else {
                System.out.println("[USERS] Файл с данными пользователей не найден, создана новая коллекция");
                users = new HashMap<>();
            }
        } catch (IOException e) {
            System.err.println("[USERS] Ошибка при загрузке данных пользователей: " + e.getMessage());
            users = new HashMap<>();
        }
    }
}