package com.chronology.bot.service.steps;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.CommandService;
import com.chronology.bot.service.ContentService;
import com.chronology.bot.service.MessageService;
import com.chronology.bot.service.VideoService;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.Message;


public class CreateVideoSteps implements Step {

    private CommandService commandService;
    private MessageService messageService;
    private ContentService contentService;

    private VideoService videoService;


    public CreateVideoSteps(CommandService commandService, MessageService messageService, ContentService contentService, VideoService videoService) {
        this.commandService = commandService;
        this.messageService = messageService;
        this.contentService = contentService;
        this.videoService = videoService;
    }

    public void executeStep(ChatId chatId, UserId userId, Message message) {
        StepType step = commandService.getActiveCommandStep(userId);

        if (CreateVideoStepTypes.ASK_CHOOSE_YEAR == step) {

            List<Integer> contentYears = contentService.getAllContentYears(userId);
            boolean hasAnyContent = !CollectionUtils.isEmpty(contentYears);

            if (!hasAnyContent) {
                messageService.sendMessage(chatId, "У вас нет добавленных фотографий");
                finish(chatId, userId);
                return;
            }

            List<String> contentYearsString = contentYears.stream().map(String::valueOf).collect(Collectors.toList());

            messageService.sendMessageWithReplyKeyboard(chatId, "Выберите год, для которого нужно создать видео, из списка", contentYearsString);
            commandService.setNextStep(userId, CreateVideoStepTypes.ANSWER_CHOOSE_YEAR);

        } else if (CreateVideoStepTypes.ANSWER_CHOOSE_YEAR == step) {

            List<Integer> contentYears = contentService.getAllContentYears(userId);
            List<String> contentYearsString = contentYears.stream().map(String::valueOf).collect(Collectors.toList());

            if (!message.hasText() || !contentYearsString.contains(message.getText())) {
                messageService.sendMessage(chatId, "Неверный формат, попробуйте еще раз");
                return;
            }

            int year = Integer.parseInt(message.getText());

            messageService.sendMessageWithRemoveKeyboard(chatId, String.format("Создаю видео за %s год. Это может занять время", year));

            CompletableFuture<File> fileCompletableFuture = videoService.createVideo(userId, year);
            fileCompletableFuture.thenAccept(videoFile -> {
                if (videoFile == null) {
                    messageService.sendMessageWithRemoveKeyboard(chatId, "Не получилось создать видео");
                    finish(chatId, userId);
                    return;
                }
                messageService.sendVideoMessage(chatId, videoFile);
                videoService.removeVideoFromServer(videoFile);
                finish(chatId, userId);
            });
        }
    }

    public void finish(ChatId chatId, UserId userId) {
        commandService.removeActiveCommandForUser(userId);
        messageService.sendMessageWithRemoveKeyboard(chatId, "Команда завершена");
    }
}


