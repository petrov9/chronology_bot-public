package com.chronology.bot.service.handler;

import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.SpringContext;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageHandlerFactory {

    public static MessageHandler getMessageHandler(Message message) {
        if (message.hasText() && !StringUtils.isEmpty(message.getText()) && message.getText().startsWith("/")) {
            return new CommandHandler();
        } else {
            MessageService messageService = SpringContext.getBean(MessageService.class);
            CommandService commandService = SpringContext.getBean(CommandService.class);
            return new UserAnswerHandler(commandService, messageService);
        }
    }
}


