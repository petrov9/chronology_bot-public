package com.chronology.bot.service.handler;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import org.telegram.telegrambots.meta.api.objects.Message;


public interface MessageHandler {

    void handle(Message message, ChatId chatId, UserId userId);
}


