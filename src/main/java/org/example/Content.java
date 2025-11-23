package org.example;

/**
 * Класс для хранения и передачи контента между компонентами системы.
 * Содержит информацию о сообщении, пользователе и параметрах ответа.
 */
public class Content {
    private final boolean out;      //ТЕПЕРЬ ФАЙНАЛ
    private final String chatId;    //ТЕПЕРЬ ФАЙНАЛ
    private final String text;      //ТЕПЕРЬ ФАЙНАЛ
    private final String userClick; //ТЕПЕРЬ ФАЙНАЛ
    private final String[] options; //ТЕПЕРЬ ФАЙНАЛ

    /**
     * Основной конструктор для создания полного контента.
     *
     * @param out флаг исходящего сообщения
     * @param chatId идентификатор чата
     * @param text текст сообщения
     * @param userClick данные callback
     * @param options варианты ответов (кнопки)
     */
    public Content(boolean out, String chatId, String text, String userClick, String[] options) {
        this.out = out;
        this.chatId = chatId;
        this.text = text;
        this.userClick = userClick;
        this.options = options != null ? options.clone() : null;
    }

    /**
     * Конструктор для текстового сообщения с кнопками.
     */
    public Content(boolean out, String chatId, String text, String[] options) {
        this(out, chatId, text, null, options);
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
     * Возвращает копию массива опций
     */
    public String[] getOptions() {
        return options != null ? options.clone() : null;
    }
}