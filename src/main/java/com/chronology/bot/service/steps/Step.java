package com.chronology.bot.service.steps;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import org.telegram.telegrambots.meta.api.objects.Message;


interface Step {
    void executeStep(ChatId chatId, UserId userId, Message message);
    void finish(ChatId chatId, UserId userId);
}


