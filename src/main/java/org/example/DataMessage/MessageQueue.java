package org.example.DataMessage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * Очередь для хранения сообщений, которые нужно отправить асинхронно.
 * Реализует паттерн Singleton для обеспечения единственного экземпляра в системе.
 * Использует потокобезопасную ConcurrentLinkedQueue для хранения сообщений.
 */
public class MessageQueue {
    private static MessageQueue instance;
    private final ConcurrentLinkedQueue<Content> messageQueue = new ConcurrentLinkedQueue<>();

    /**
     * Приватный конструктор для реализации паттерна Singleton.
     */
    private MessageQueue() {}

    /**
     * Возвращает единственный экземпляр MessageQueue.
     * Синхронизирован для безопасности в многопоточной среде.
     *
     * @return единственный экземпляр MessageQueue
     */
    public static synchronized MessageQueue getInstance() {
        if (instance == null) {
            instance = new MessageQueue();
        }
        return instance;
    }

    /**
     * Добавляет сообщение в очередь.
     * Сообщение будет обработано при следующей проверке очереди.
     *
     * @param content объект Content для добавления в очередь
     */
    public void addMessage(Content content) {
        messageQueue.add(content);
    }

    /**
     * Извлекает и удаляет сообщение из начала очереди.
     * Если очередь пуста, возвращает null.
     *
     * @return первый элемент очереди или null если очередь пуста
     */
    public Content pollMessage() {
        return messageQueue.poll();
    }

    /**
     * Проверяет, есть ли сообщения в очереди.
     *
     * @return true если в очереди есть сообщения, false если очередь пуста
     */
    public boolean hasMessages() {
        return !messageQueue.isEmpty();
    }

    /**
     * Получает все сообщения из очереди и очищает ее.
     * Этот метод извлекает все сообщения за один вызов, что полезно
     * для пакетной обработки.
     *
     * @return список всех сообщений из очереди (может быть пустым)
     */
    public List<Content> getAllMessages() {
        List<Content> messages = new ArrayList<>();
        while (hasMessages()) {
            Content message = pollMessage();
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    /**
     * Полностью очищает очередь сообщений.
     * Удаляет все сообщения, которые еще не были обработаны.
     */
    public void clear() {
        messageQueue.clear();
    }
}