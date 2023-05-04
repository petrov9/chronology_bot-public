package com.chronology.bot.service.handler;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.command.ICommand;
import java.util.Optional;
import org.telegram.telegrambots.meta.api.objects.Message;


public class UserAnswerHandler implements MessageHandler {

    private final CommandService commandService;
    private final MessageService messageService;

    public UserAnswerHandler(CommandService commandService, MessageService messageService) {
        this.commandService = commandService;
        this.messageService = messageService;
    }

    public void handle(Message message, ChatId chatId, UserId userId) {
        Optional<ICommand> activeCommand = commandService.getActiveCommand(chatId, userId);

        activeCommand.ifPresentOrElse(iCommand -> iCommand.handleUserAnswer(chatId, userId, message), () -> {
            messageService.sendMessage(chatId, "Нет активной команды");
        });
    }
}


