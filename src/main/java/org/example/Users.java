package org.example;

import java.util.HashMap;
import java.util.Map;

public class Users {
    private Map<String, UserData> users;

    public Users() {
        users = new HashMap<>();
        loadFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
    }

    public void add(String chatId, UserData userData) {
        users.put(chatId, userData);
    }

    public void remove(String chatId) {
        users.remove(chatId);
    }

    public boolean has(String chatId) {
        return users.containsKey(chatId);
    }

    public UserData get(String chatId) {
        return users.get(chatId);
    }

    public Map<String, UserData> getAllUsers() {
        return users;
    }

    public int size() {
        return users.size();
    }

    /**
     * заглушка для сохранения всех пользователей на диск
     */
    public void saveToDisk() {
        // TODO: Реализовать сохранение users на диск
        System.out.println("call saveToDisk()");
    }

    /**
     * заглушка для загрузки пользователей с диска при старте
     */
    private void loadFromDisk() {
        // TODO: Реализовать чтение данных с диска и заполнение users
        System.out.println("call loadFromDisk()");
    }
}
