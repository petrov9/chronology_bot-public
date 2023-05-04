package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.steps.AddDayStepTypes;
import com.chronology.bot.service.steps.AddDaySteps;
import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


public class AddTodayCommand implements ICommand {

    public static final BotCommand ADD_TODAY_COMMAND = new BotCommand("/add_today", "Добавить этот день");


    protected CommandService commandService;
    protected AddDaySteps addDaySteps;

    public AddTodayCommand(CommandService commandService, AddDaySteps addDaySteps) {
        this.commandService = commandService;
        this.addDaySteps = addDaySteps;
    }

    public void start(ChatId chatId, UserId userId) {
        commandService.setActiveCommandToUser(chatId, userId, getCommand(), AddDayStepTypes.ASK_SEND_MEDIA);
        firstStep(chatId, userId);
    }

    protected void firstStep(ChatId chatId, UserId userId) {
        addDaySteps.executeStep(chatId, userId, null);
    }

    public void handleUserAnswer(ChatId chatId, UserId userId, Message message) {
        addDaySteps.executeStep(chatId, userId, message);
    }

    @Override
    public String getCommand() {
        return ADD_TODAY_COMMAND.getCommand();
    }

    @Override
    public void cancel(UserId userId) {
        throw new NotImplementedException("Not implemented");
    }
}
