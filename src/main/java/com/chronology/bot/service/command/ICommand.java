package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommand {

    String getCommand();

    void start(ChatId chatId, UserId userId);

    void cancel(UserId userId);

    void handleUserAnswer(ChatId chatId, UserId userId, Message message);
}


