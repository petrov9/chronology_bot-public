package com.chronology.bot.service.handler;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.repository.UserInfoRepo;
import com.chronology.bot.service.ChronologyBot;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.ContentService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.NotificationService;
import com.chronology.bot.service.SpringContext;
import com.chronology.bot.service.UserInfoService;
import com.chronology.bot.service.VideoService;
import com.chronology.bot.service.command.AddMissedDayCommand;
import com.chronology.bot.service.command.AddTodayCommand;
import com.chronology.bot.service.command.CreateVideoCommand;
import com.chronology.bot.service.command.ICommand;
import com.chronology.bot.service.command.NotificationCommand;
import com.chronology.bot.service.command.StartCommand;
import com.chronology.bot.service.steps.AddDaySteps;
import com.chronology.bot.service.steps.AddMissedDaySteps;
import com.chronology.bot.service.steps.CreateVideoSteps;
import com.chronology.bot.service.steps.NotificationSteps;
import org.telegram.telegrambots.meta.api.objects.Message;


public class CommandHandler implements MessageHandler {

    private MessageService messageService = SpringContext.getBean(MessageService.class);

    public void handle(Message message, ChatId chatId, UserId userId) {

        if (!message.hasText()) {
            String errorMessage = "Неизвестная команда";
            messageService.sendMessage(chatId, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        ICommand command = getConcreteCommand(chatId, message.getText());
        command.start(chatId, userId);
    }

    public static ICommand getConcreteCommand(ChatId chatId, String command) {
        MessageService messageService = SpringContext.getBean(MessageService.class);
        CommandService commandService = SpringContext.getBean(CommandService.class);
        ContentService contentService = SpringContext.getBean(ContentService.class);
        UserInfoService userInfoService = SpringContext.getBean(UserInfoService.class);
        NotificationService notificationService = SpringContext.getBean(NotificationService.class);
        VideoService videoService = SpringContext.getBean(VideoService.class);
        UserInfoRepo userInfoRepo = SpringContext.getBean(UserInfoRepo.class);
        ChronologyBot chronologyBot = SpringContext.getBean(ChronologyBot.class);

        if (StartCommand.START.equals(command)) {
            return new StartCommand(commandService, messageService, chronologyBot, userInfoRepo);

        } else if (NotificationCommand.SET_NOTIFICATIONS_COMMAND.getCommand().equals(command)) {
            return new NotificationCommand(commandService, new NotificationSteps(commandService, messageService, notificationService));

        } else if (AddTodayCommand.ADD_TODAY_COMMAND.getCommand().equals(command)) {
            return new AddTodayCommand(commandService, new AddDaySteps(commandService, messageService, contentService, userInfoService));

        } else if (AddMissedDayCommand.ADD_MISSED_DAY_COMMAND.getCommand().equals(command)) {
            return new AddMissedDayCommand(commandService, new AddMissedDaySteps(commandService, messageService, contentService, userInfoService));

        } else if (CreateVideoCommand.CREATE_VIDEO_COMMAND.getCommand().equals(command)) {
            return new CreateVideoCommand(commandService, new CreateVideoSteps(commandService, messageService, contentService, videoService));

        } else {
            String errorMessage = "Неизвестная команда";
            messageService.sendMessage(chatId, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}


