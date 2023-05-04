package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.steps.NotificationSteps;
import com.chronology.bot.service.steps.SetNotificationStepTypes;
import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


public class NotificationCommand implements ICommand {
    public static final BotCommand SET_NOTIFICATIONS_COMMAND = new BotCommand("/set_notifications", "Настроить уведомления");
    private final CommandService commandService;
    private final NotificationSteps notificationSteps;

    public NotificationCommand(CommandService commandService, NotificationSteps notificationSteps) {
        this.commandService = commandService;
        this.notificationSteps = notificationSteps;
    }

    public void start(ChatId chatId, UserId userId) {

        commandService.setActiveCommandToUser(chatId, userId, getCommand(), SetNotificationStepTypes.ASK_IF_WANT_TO_CHANGE);

        firstStep(chatId, userId);
    }

    private void firstStep(ChatId chatId, UserId userId) {
        notificationSteps.executeStep(chatId, userId, null);
    }

    public void handleUserAnswer(ChatId chatId, UserId userId, Message message) {
        notificationSteps.executeStep(chatId, userId, message);
    }

    @Override
    public String getCommand() {
        return SET_NOTIFICATIONS_COMMAND.getCommand();
    }

    @Override
    public void cancel(UserId userId) {
        throw new NotImplementedException("Not implemented");
    }
}


