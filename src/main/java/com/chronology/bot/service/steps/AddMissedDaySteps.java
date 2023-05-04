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
import org.telegram.telegrambots.meta.api.objects.Message;


public class AddMissedDaySteps extends AddDaySteps {

    public AddMissedDaySteps(CommandService commandService, MessageService messageService, ContentService contentService, UserInfoService userInfoService) {
        super(commandService, messageService, contentService, userInfoService);
    }

    @Override
    public void executeStep(ChatId chatId, UserId userId, Message message) {
        StepType step = commandService.getActiveCommandStep(userId);
        if (AddDayStepTypes.ASK_SET_DATE == step) {
            messageService.sendMessageWithRemoveKeyboard(chatId, "Введите день, который вы хотите добавить, в формате dd.mm.yyyy");
            commandService.setNextStep(userId, AddDayStepTypes.ANSWER_SET_DATE);
        } else if (AddDayStepTypes.ANSWER_SET_DATE == step) {

            try {
                LocalDate missedDay = LocalDate.parse(message.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                ZoneOffset userZoneOffset = userInfoService.getUserZoneOffset(userId);
                LocalDate systemNowInOffsetZone = Utils.getSystemDateInOffsetZone(userZoneOffset);

                if (systemNowInOffsetZone.minusDays(1).compareTo(missedDay) < 0) {
                    messageService.sendMessage(chatId, "Дата может быть только в прошлом, попробуйте еще раз");
                    return;
                }

                ChronologyBot.userDay.put(userId, missedDay);
                commandService.setNextStep(userId, AddDayStepTypes.ASK_SEND_MEDIA);

            } catch (Exception e) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }
        }

        super.executeStep(chatId, userId, message);
    }
}


