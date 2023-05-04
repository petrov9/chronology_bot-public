package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.steps.CreateVideoStepTypes;
import com.chronology.bot.service.steps.CreateVideoSteps;
import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


public class CreateVideoCommand implements ICommand {
    public static final BotCommand CREATE_VIDEO_COMMAND= new BotCommand("/create_video", "Получить видео");
    private final CommandService commandService;
    private final CreateVideoSteps createVideoSteps;

    public CreateVideoCommand(CommandService commandService, CreateVideoSteps createVideoSteps) {
        this.commandService = commandService;
        this.createVideoSteps = createVideoSteps;
    }

    public void start(ChatId chatId, UserId userId) {
        commandService.setActiveCommandToUser(chatId, userId, getCommand(), CreateVideoStepTypes.ASK_CHOOSE_YEAR);

        firstStep(chatId, userId);
    }

    private void firstStep(ChatId chatId, UserId userId) {
        createVideoSteps.executeStep(chatId, userId, null);
    }

    public void handleUserAnswer(ChatId chatId, UserId userId, Message message) {
        createVideoSteps.executeStep(chatId, userId, message);
    }

    @Override
    public String getCommand() {
        return CREATE_VIDEO_COMMAND.getCommand();
    }

    @Override
    public void cancel(UserId userId) {
        throw new NotImplementedException("Not implemented");
    }
}

