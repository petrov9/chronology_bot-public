package com.chronology.bot.service.steps;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.ChronologyBot;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.ContentService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.UserInfoService;
import com.chronology.bot.utils.Utils;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.telegram.telegrambots.meta.api.objects.Message;


public class AddDaySteps implements Step {

    protected CommandService commandService;
    protected MessageService messageService;
    protected ContentService contentService;
    protected UserInfoService userInfoService;


    public AddDaySteps(CommandService commandService, MessageService messageService, ContentService contentService, UserInfoService userInfoService) {
        this.commandService = commandService;
        this.messageService = messageService;
        this.contentService = contentService;
        this.userInfoService = userInfoService;
    }

    public void executeStep(ChatId chatId, UserId userId, Message message) {
        StepType step = commandService.getActiveCommandStep(userId);
        List<String> yesOrNot = List.of("Да", "Нет");

        ZoneOffset userZoneOffset = userInfoService.getUserZoneOffset(userId);
        LocalDate systemNowInOffsetZone = Utils.getSystemDateInOffsetZone(userZoneOffset);

        LocalDate addDay = Optional.ofNullable(ChronologyBot.userDay.get(userId)).orElse(systemNowInOffsetZone);

        if (AddDayStepTypes.ASK_SEND_MEDIA == step) {

            boolean haveContent = contentService.hasContentForDay(userId, addDay);

            if (haveContent) {
                String dayString = addDay.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                if (addDay.compareTo(systemNowInOffsetZone) == 0) {
                    dayString = "сегодня";
                }

                messageService.sendMessageWithReplyKeyboard(chatId, String.format("Вы уже добавили фото на %s, хотите изменить ?", dayString), yesOrNot);
                commandService.setNextStep(userId, AddDayStepTypes.ANSWER_CHANGE_MEDIA);
            } else {
                messageService.sendMessage(chatId, "Отправьте одну фотографию, которая описывает этот день наилучшим образом");
                commandService.setNextStep(userId, AddDayStepTypes.ANSWER_SEND_MEDIA);
            }

            // TODO (medium): обрабатывать кейс если придет больше одной фотки
        } else if (AddDayStepTypes.ANSWER_CHANGE_MEDIA == step) {

            if (!message.hasText() || !yesOrNot.contains(message.getText())) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            boolean wantToChangeContent = "Да".equals(message.getText());

            if (!wantToChangeContent) {
                finish(chatId, userId);
            } else {
                messageService.sendMessageWithRemoveKeyboard(chatId, "Отправьте одну фотографию, которая описывает этот день наилучшим образом");
                commandService.setNextStep(userId, AddDayStepTypes.ANSWER_SEND_MEDIA);
            }

        } else if (AddDayStepTypes.ANSWER_SEND_MEDIA == step) {

            if (!message.hasPhoto() && !message.hasDocument()) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            if (message.hasPhoto()) {
                contentService.savePhotoForDay(userId, addDay, message.getPhoto());
            } else {
                contentService.savePhotoForDay(userId, addDay, message.getDocument());
            }

            messageService.sendMessageWithReplyKeyboard(chatId, "Опишите этот день вкратце (max: 100 символов)", List.of("Пропустить"));
            commandService.setNextStep(userId, AddDayStepTypes.ANSWER_SEND_DESCRIPTION);


        } else if (AddDayStepTypes.ANSWER_SEND_DESCRIPTION == step) {

            if (!message.hasText()) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            String text = message.getText();
            int maxSymbols = 100;
            if (text.length() > maxSymbols) {
                messageService.sendMessage(chatId, String.format("Привышено кол-во символов: %s/%s", text.length(), maxSymbols));
                return;
            }

            String description = text;
            if ("Пропустить".equals(text)) {
                description = null;
            }

            contentService.addTextForDay(userId, addDay, description);

            messageService.sendMessageWithRemoveKeyboard(chatId, "Спасибо. День добавлен");
            finish(chatId, userId);
        }
    }

    public void finish(ChatId chatId, UserId userId) {
        ChronologyBot.userDay.remove(userId);
        commandService.removeActiveCommandForUser(userId);
        messageService.sendMessageWithRemoveKeyboard(chatId, "Добавление завершено");
    }
}


