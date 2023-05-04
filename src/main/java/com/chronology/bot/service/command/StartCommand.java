package com.chronology.bot.service.command;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.repository.UserInfoRepo;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.ChronologyBot;
import com.chronology.bot.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
public class StartCommand implements ICommand {

    public static final String START = "/start";

    private CommandService commandService;
    private MessageService messageService;
    private ChronologyBot chronologyBot;
    private UserInfoRepo userInfoRepo;

    public StartCommand(CommandService commandService, MessageService messageService, ChronologyBot chronologyBot, UserInfoRepo userInfoRepo) {
        this.commandService = commandService;
        this.messageService = messageService;
        this.chronologyBot = chronologyBot;
        this.userInfoRepo = userInfoRepo;
    }

    @Override
    public String getCommand() {
        return START;
    }

    public void start(ChatId chatId, UserId userId) {
//        commandService.setActiveCommandToUser(chatId, userId, getCommand(), );

        firstStep(chatId, userId);

        finishCommand(userId);
    }

    private void firstStep(ChatId chatId, UserId userId) {
        requestSetBotCommands(chatId, userId);
        requestSetBotMenu(chatId);

        boolean isNewUser = !userInfoRepo.existsById(userId.getId());

        String welcomeText =
            isNewUser ? "Добро пожаловать в бот хронологии. Бот будет уведомлять вас раз в день. Вы можете загрузить одно фото и описание к " + "нему. И в "
                + "конце" + " года бот сделает для вас видео из фотографий которые вы загружали" : "Рады что вы снова с нами";

        if (isNewUser) {
            userInfoRepo.save(new UserInfo(userId.getId(), chatId.getId()));
        }

        messageService.sendMessage(chatId, welcomeText);
    }

    private void requestSetBotMenu(ChatId chatId) {
        SetChatMenuButton chatMenuButton = SetChatMenuButton.builder()
            .chatId(chatId.getId())
            .menuButton(
                MenuButtonCommands.builder()
                    .build()
            ).build();

        try {
            chronologyBot.execute(chatMenuButton);
        } catch (TelegramApiException e) {
            log.error("Can't set chat menu button");
        }
    }

    private void requestSetBotCommands(ChatId chatId, UserId userId) {

        SetMyCommands setMyCommands = new SetMyCommands(ChronologyBot.BOT_COMMANDS, BotCommandScopeChat.builder().chatId(chatId.getId()).build(), "en");
//        SetMyCommands setMyCommands = new SetMyCommands(ChronologyBot.BOT_COMMANDS, BotCommandScopeChatMember.builder().chatId(chatId).userId(userId).build(), "en");
//        SetMyCommands setMyCommands = new SetMyCommands(ChronologyBot.BOT_COMMANDS, BotCommandScopeAllPrivateChats.builder().build(), "ru");
        try {
            chronologyBot.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error("Can't set bot commands", e);
        }
    }

    private void finishCommand(UserId userId) {
        commandService.removeActiveCommandForUser(userId);
    }

    @Override
    public void handleUserAnswer(ChatId chatId, UserId userId, Message message) {
        throw new NotImplementedException("Start command doesn't wait user answer");
    }

    @Override
    public void cancel(UserId userId) {
        throw new NotImplementedException("Start command can't be canceled");
    }
}


