package com.chronology.bot.service.steps;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.model.UserNotification;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.NotificationService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.Message;


public class NotificationSteps implements Step {

    private CommandService commandService;
    private MessageService messageService;
    private NotificationService notificationService;


    public NotificationSteps(CommandService commandService, MessageService messageService, NotificationService notificationService) {
        this.commandService = commandService;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    public void executeStep(ChatId chatId, UserId userId, Message message) {
        StepType step = commandService.getActiveCommandStep(userId);
        List<String> yesOrNot = List.of("Да", "Нет");

        if (SetNotificationStepTypes.ASK_IF_WANT_TO_CHANGE == step) {

            UserNotification notification = notificationService.findNotification(userId);
            if (notification == null) {
                askTurnOnNotifications(chatId, userId, yesOrNot);
            } else {
                String msg = notification.isNotify() ? String.format("У вас уже настроены уведомления на каждый день в %s, часовой пояс UTC%s. Хотите изменить?",
                    notification.getTime(),
                    notification.getZoneOffset().toString()) :
                    "Вы отключили уведомления. Хотите изменить ?";

                messageService.sendMessageWithReplyKeyboard(chatId, msg, yesOrNot);
                commandService.setNextStep(userId, SetNotificationStepTypes.ANSWER_IF_WANT_TO_CHANGE);
            }

        } else if (SetNotificationStepTypes.ANSWER_IF_WANT_TO_CHANGE == step) {
            if (!yesOrNot.contains(message.getText())) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            boolean wantToChange = "Да".equals(message.getText());
            if (!wantToChange) {
                finish(chatId, userId);
                return;
            }

            askTurnOnNotifications(chatId, userId, yesOrNot);

        } else if (SetNotificationStepTypes.ASK_TURN_ON_NOTIFICATIONS == step) {
            askTurnOnNotifications(chatId, userId, yesOrNot);
        } else if (SetNotificationStepTypes.ANSWER_TURN_ON_NOTIFICATIONS == step) {

            if (!yesOrNot.contains(message.getText())) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            boolean isEnableNotifications = "Да".equals(message.getText());
            notificationService.enableUserNotification(userId, isEnableNotifications);

            if (!isEnableNotifications) {
                messageService.sendMessageWithRemoveKeyboard(chatId, "Уведомления успешно отключены");
                finish(chatId, userId);
            } else {
                messageService.sendMessageWithRemoveKeyboard(chatId, "Введите текущее время в формате dd.mm.yyyy hh:mm чтобы мы определили ваш часовой пояс");
                commandService.setNextStep(userId, SetNotificationStepTypes.ANSWER_CURRENT_DATE);
            }
            //TODO: (medium): check current date can be in future only in +14 hours
        } else if (SetNotificationStepTypes.ANSWER_CURRENT_DATE == step) {

            LocalDateTime userDateTime = null;

            try {
                userDateTime = LocalDateTime.parse(message.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            } catch (Exception e) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            String timezone = notificationService.setUserTimezone(userId, userDateTime);
            messageService.sendMessage(chatId, "Спасибо. Ваш часовой пояс " + timezone);
            messageService.sendMessage(chatId, "Введите время в формате hh:mm когда вы хотите получать напоминание");
            commandService.setNextStep(userId, SetNotificationStepTypes.ANSWER_NOTIFICATION_TIME);

        } else if (SetNotificationStepTypes.ANSWER_NOTIFICATION_TIME == step) {
            LocalTime desireTime = null;

            try {
                desireTime = LocalTime.parse(message.getText(), DateTimeFormatter.ofPattern("HH:mm"));

            } catch (Exception e) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            notificationService.setNotificationTime(userId, desireTime);
            messageService.sendMessage(chatId, "Спасибо. Будем напоминать вам каждый день в " + desireTime);

            finish(chatId, userId);
        } else {
            messageService.sendMessage(chatId, "У этой команды нет активных шагов");
            finish(chatId, userId);
        }
    }

    private void askTurnOnNotifications(ChatId chatId, UserId userId, List<String> yesOrNot) {
        messageService.sendMessageWithReplyKeyboard(chatId, "Включить уведомления раз в день?", yesOrNot);
        commandService.setNextStep(userId, SetNotificationStepTypes.ANSWER_TURN_ON_NOTIFICATIONS);
    }

    public void finish(ChatId chatId, UserId userId) {
        commandService.removeActiveCommandForUser(userId);
        messageService.sendMessageWithRemoveKeyboard(chatId, "Настройка завершена");
    }
}


