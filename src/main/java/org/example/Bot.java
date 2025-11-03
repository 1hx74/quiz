package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final Producer produser = new Producer();
    private final TelegramClient telegramClient;
    private final String botToken;

    public Bot(String botToken) {
        this.botToken = botToken;
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    private void registerBotCommands() {
        try {
            boolean success = telegramClient.execute(
                    SetMyCommands.builder()
                            .commands(Arrays.asList(
                                    new BotCommand("start", "start_message"),
                                    new BotCommand("help", "help_message")
                            ))
                            .scope(new BotCommandScopeDefault())
                            .build()
            );

            System.out.println(success
                    ? "commands registration already success"
                    : "failed commands registration!"
            );

        } catch (Exception e) {
            System.err.println("[ERROR REG COMMAND]");
            e.printStackTrace(System.err);
        }
    }

    private Content parse(Update update) {
        Content content = new Content(false);

        if (update.hasMessage()) {
            Message msg = update.getMessage();
            content.setChatId(String.valueOf(msg.getChatId()));

            if (msg.hasText()) {
                content.setText(msg.getText());
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            content.setChatId(String.valueOf(query.getMessage().getChatId()));
            content.setClick(query.getData());
        }

        return content;
    }

    private SendMessage toMessage(Content content) {
        if (!content.isOut()) {
            return null;
        }

        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                .chatId(content.getChatId());

        if (content.getText() != null && !content.getText().isEmpty()) {
            builder.text(content.getText());
        }

        if (content.getOptions() != null && content.getOptions().length > 0) {
            List<KeyboardRow> rows = new ArrayList<>();
            for (String option : content.getOptions()) {
                KeyboardRow row = new KeyboardRow();
                row.add(option);
                rows.add(row);
            }

            ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                    .keyboard(rows)
                    .resizeKeyboard(true)
                    .oneTimeKeyboard(false)
                    .build();

            builder.replyMarkup(keyboard);
        }

        return builder.build();
    }

    public void sendMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("[ERROR SEND]");
            e.printStackTrace(System.err);
        }
    }

    public void consume(Update update) {
        Content content = parse(update);
        sendMessage(toMessage(produser.produce(content)));
    }

    public void start() {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(botToken, this);
            registerBotCommands();
            System.out.println("bot started");

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[ERROR BOT]");
            e.printStackTrace(System.err);
        }
    }
}