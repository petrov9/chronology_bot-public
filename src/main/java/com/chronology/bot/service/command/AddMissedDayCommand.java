package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.steps.AddDayStepTypes;
import com.chronology.bot.service.steps.AddMissedDaySteps;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;


public class AddMissedDayCommand extends AddTodayCommand {

    public static final BotCommand ADD_MISSED_DAY_COMMAND = new BotCommand("/add_missed_day", "Добавить пропущенный день");

    public AddMissedDayCommand(CommandService commandService, AddMissedDaySteps addTodaySteps) {
        super(commandService, addTodaySteps);
    }

    @Override
    public void start(ChatId chatId, UserId userId) {
        commandService.setActiveCommandToUser(chatId, userId, getCommand(), AddDayStepTypes.ASK_SET_DATE);
        super.firstStep(chatId, userId);
    }

    @Override
    public String getCommand() {
        return ADD_MISSED_DAY_COMMAND.getCommand();
    }
}


