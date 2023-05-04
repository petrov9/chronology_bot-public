package com.chronology.bot.service;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserContent;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Service
public class MessageService {

    private ChronologyBot chronologyBot;

    public MessageService(ChronologyBot chronologyBot) {
        this.chronologyBot = chronologyBot;
    }

    public void sendMessage(ChatId chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.getId());
        sendMessage.setText(message);

        try {
            chronologyBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send message", e);
        }
    }

    public void sendMessageWithInline(ChatId chatId, String text, List<String> keyboards) {
        ReplyKeyboard keyboard = createInlineKeyboard(keyboards);
        sendMessageWithReplyKeyBoard(chatId, text, keyboard);
    }

    public void sendMessageWithReplyKeyboard(ChatId chatId, String text, List<String> keyboards) {
        ReplyKeyboard keyboard = createReplyKeyboard(keyboards);
        sendMessageWithReplyKeyBoard(chatId, text, keyboard);
    }

    public void sendMessageWithRemoveKeyboard(ChatId chatId, String text) {
        ReplyKeyboard keyboard = createRemoveKeyboard();
        sendMessageWithReplyKeyBoard(chatId, text, keyboard);
    }

    private void sendMessageWithReplyKeyBoard(ChatId chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage responseMessage = SendMessage.builder()
            .chatId(chatId.getId())
            .text(text)
            .replyMarkup(replyKeyboard)
            .build();

        try {
            chronologyBot.execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send message", e);
        }
    }

    private ReplyKeyboard createReplyKeyboard(List<String> options) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setKeyboard(keyboard);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboard.add(keyboardRow);

        options.forEach(opt -> {
            keyboardRow.add(opt);
        });

        return replyKeyboardMarkup;
    }

    private ReplyKeyboard createInlineKeyboard(List<String> options) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setKeyboard(keyboard);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        keyboard.add(buttons);


        options.forEach(opt -> {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(opt)
                .callbackData(opt)
                .build();
            buttons.add(button);
        });

        return replyKeyboardMarkup;
    }

    private ReplyKeyboard createRemoveKeyboard() {
        return ReplyKeyboardRemove.builder().removeKeyboard(true).build();
    }

    public void sendVideoMessage(ChatId chatId, File videoFile) {

        InputFile inputFile = new InputFile(videoFile);
        SendVideo sendVideo = SendVideo.builder().chatId(chatId.getId()).video(inputFile)
//                .thumb(thumbBytes)
//                .caption(caption)
//                .duration(duration)
//                .parseMode(ParseMode.HTML)
//                .height(1)
//                .width(2)
//                .supportsStreaming(true)
            .build();

        try {
            //TODO (low): executeAsync
            chronologyBot.execute(sendVideo);
        } catch (TelegramApiException e) {
            log.error("Can't send video", e);
        }

    }

    public void sendPhotoMessage(ChatId chatId, UserContent content) {
        String caption = getCaption(content);

        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(chatId.getId())
            .photo(new InputFile(new File(content.getMediaUrl())))
            .caption(caption)
            .parseMode(ParseMode.HTML)
            .build();

        try {
            chronologyBot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Can't send photo message", e);
        }
    }

    public void sendMultiplePhotoMessage(ChatId chatId, List<UserContent> contents) {

        if (CollectionUtils.isEmpty(contents)) {
            return;
        }

        int contentSize = contents.size();

        if (contentSize == 1) {
            this.sendPhotoMessage(chatId, contents.get(0));
        } else if (contentSize >= 2 && contentSize <= 10){
            this.sendMediaGroup(chatId, contents);
        } else {
            throw new NotImplementedException("Can't proceed more than 10 photos");
        }
    }

    private void sendMediaGroup(ChatId chatId, List<UserContent> contents) {
        List<InputMedia> medias = contents.stream()
            .map(userContent -> {

                String mediaName = UUID.randomUUID().toString();
                String caption = getCaption(userContent);

                InputMedia inputMedia = InputMediaPhoto.builder()
                    .media("attach://" + mediaName)
                    .mediaName(mediaName)
                    .isNewMedia(true)
                    .newMediaFile(new File(userContent.getMediaUrl()))
                    .caption(caption)
                    .parseMode(ParseMode.HTML)
                    .build();

                return inputMedia;
            }).collect(Collectors.toList());

            SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId.getId())
                .medias(medias)
                .build();

        try {
            chronologyBot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            log.error("Can't send photos with media group", e);
        }
    }

    private static final Map<Month, String> RUS_MONTH_SHORT_NAME = new HashMap<>() {{
        put(Month.JANUARY, "янв");
        put(Month.FEBRUARY, "фев");
        put(Month.MARCH, "мар");
        put(Month.APRIL, "апр");
        put(Month.MAY, "май");
        put(Month.JUNE, "июн");
        put(Month.JULY, "июл");
        put(Month.AUGUST, "авг");
        put(Month.SEPTEMBER, "сен");
        put(Month.OCTOBER, "окт");
        put(Month.NOVEMBER, "ноя");
        put(Month.DECEMBER, "дек");
    }};

    private static final Map<DayOfWeek, String> RUS_WEEK_SHORT_NAME = Map.of(
        DayOfWeek.MONDAY, "пн",
        DayOfWeek.TUESDAY, "вт",
        DayOfWeek.WEDNESDAY, "ср",
        DayOfWeek.THURSDAY, "чт",
        DayOfWeek.FRIDAY, "пт",
        DayOfWeek.SATURDAY, "сб",
        DayOfWeek.SUNDAY, "вс"
    );

    private String getCaption(UserContent userContent) {

        LocalDate contentDate = userContent.getDate();
        String description = StringUtils.defaultString(userContent.getDescription(), "");

        int date = contentDate.getDayOfMonth();
        String shortMonthName = RUS_MONTH_SHORT_NAME.get(contentDate.getMonth());
        String shortWeekName = RUS_WEEK_SHORT_NAME.get(contentDate.getDayOfWeek());


        String caption = String.format("%s %s (%s). %s", date, shortMonthName, shortWeekName, description);
        return caption;
    }
}
