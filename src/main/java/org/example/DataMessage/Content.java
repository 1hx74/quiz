package org.example.DataMessage;

/**
 * Класс для хранения и передачи контента между компонентами системы.
 * Содержит информацию о сообщении, пользователе и параметрах ответа, и кнопках.
 */
public class Content {
    private final boolean out;
    private final String chatId;
    private final String text;
    private final String userClick;
    private final String keyboardType;

    /**
     * Основной конструктор для создания полного контента.
     *
     * @param out флаг исходящего сообщения
     * @param chatId идентификатор чата
     * @param text текст сообщения
     * @param userClick данные callback
     * @param keyboardType тип клавиатуры
     */
    public Content(boolean out, String chatId, String text, String userClick, String keyboardType) {
        this.out = out;
        this.chatId = chatId;
        this.text = text;
        this.userClick = userClick;
        this.keyboardType = keyboardType;
    }

    /**
     * Конструктор для текстового сообщения с клавиатурой.
     */
    public Content(boolean out, String chatId, String text, String keyboardType) {
        this(out, chatId, text, null, keyboardType);
    }

    /**
     * Конструктор для простого текстового сообщения.
     */
    public Content(boolean out, String chatId, String text) {
        this(out, chatId, text, null, null);
    }

    /**
     * Конструктор для создания контента только с флагом направления.
     */
    public Content(boolean out) {
        this(out, null, null, null, null);
    }

    public boolean isOut() {
        return out;
    }

    public String getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }

    public String getClick() {
        return userClick;
    }

    /**
     * Возвращает тип клавиатуры
     */
    public String getKeyboardType() {
        return keyboardType;
    }

    /**
     * Проверяет, есть ли клавиатура в сообщении
     */
    public boolean hasKeyboard() {
        return keyboardType != null && !keyboardType.isEmpty();
    }
}