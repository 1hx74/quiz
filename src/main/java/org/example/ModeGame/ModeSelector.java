package org.example.ModeGame;

import org.example.DataMessage.Content;

/**
 * Интерфейс для обработки выбора режима игры.
 * Только один метод - обработка выбора режима.
 */
public interface ModeSelector {

    /**
     * Обрабатывает выбор режима игры.
     * @return контент для ответа
     */
    Content[] handleModeSelection();

    /**
     * Получает тип режима.
     * @return "solo" или "duel"
     */
    String getModeType();
}