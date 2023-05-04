package com.chronology.bot.service;

import static com.chronology.bot.service.command.NotificationCommand.*;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.command.AddTodayCommand;
import com.chronology.bot.service.command.CreateVideoCommand;
import com.chronology.bot.model.UserActiveCommand;
import com.chronology.bot.service.handler.MessageHandler;
import com.chronology.bot.service.handler.MessageHandlerFactory;
import com.chronology.bot.service.command.AddMissedDayCommand;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


@Slf4j
public class ChronologyBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public static List<BotCommand> BOT_COMMANDS = Arrays.asList(
        AddTodayCommand.ADD_TODAY_COMMAND,
        AddMissedDayCommand.ADD_MISSED_DAY_COMMAND,
        SET_NOTIFICATIONS_COMMAND,
        CreateVideoCommand.CREATE_VIDEO_COMMAND
//        new BotCommand(SET_VIDEO_PARAMS, "Настроить параметры видео") // добавить разрешение видео
        );
    //TODO (medium): сделать функцию отправки сообщения всем клиентам бота, это нужно для уведомления о новых функциях
    //TODO (high): каждую неделю присылать сообщение с фотографиями за прошлую неделю - как она прошла
    //TODO (high): онлайн галерея с фотографиями
    //TODO (low): пользовательское соглашение
    public static final ConcurrentHashMap<UserId, UserActiveCommand> userActiveCommand = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<UserId, LocalDate> userDay = new ConcurrentHashMap<>();

    //TODO high: save as many users' info as can
    public void onUpdateReceived(Update update) {

        executorService.submit(() -> {
            try {
                Message message = update.getMessage();
                if (message != null) {
                    try {
                        handleIncomingMessage(message);
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
            } catch (Exception e) {
                log.error("error", e);
            }
        });
    }

    private void handleIncomingMessage(Message incomeMessage) {

        ChatId chatId = Optional.of(incomeMessage).map(msg -> new ChatId(msg.getChatId())).orElseThrow(() -> {
            throw new IllegalArgumentException("chatId can't be null");
        });
        UserId userId = Optional.of(incomeMessage).map(Message::getFrom).map(user -> new UserId(user.getId())).orElseThrow(() -> {
            throw new IllegalArgumentException("userId can't be null");
        });

        // process only case when chatId == userID
        if (chatId.getId() != userId.getId()) {
            throw new RuntimeException("Unpredictable case, chatId != userId");
        }

        MessageHandler messageHandler = MessageHandlerFactory.getMessageHandler(incomeMessage);
        messageHandler.handle(incomeMessage, chatId, userId);

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}