package com.chronology.bot.service;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserContent;
import com.chronology.bot.model.UserId;
import com.chronology.bot.model.UserNotification;
import com.chronology.bot.repository.ContentRepo;
import com.chronology.bot.utils.Utils;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Service
public class ContentService {

    private ContentRepo contentRepo;
    private ChronologyBot chronologyBot;
    private MessageService messageService;
    private NotificationService notificationService;

    public ContentService(ContentRepo contentRepo, ChronologyBot chronologyBot, MessageService messageService, NotificationService notificationService) {
        this.contentRepo = contentRepo;
        this.chronologyBot = chronologyBot;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    public boolean hasContentForDay(UserId userId, LocalDate day) {
        return Optional.ofNullable(contentRepo.findAllByUserId(userId.getId()))
            .map((userContents) -> {
                return userContents.stream().anyMatch(e -> e.getDate().compareTo(day) == 0);
            })
            .orElse(false);
    }

    public void addTextForDay(UserId userId, LocalDate day, String description) {

        List<UserContent> userContents = contentRepo.findAllByUserId(userId.getId());

        if (CollectionUtils.isEmpty(userContents)) {
            userContents = new ArrayList<>();
            contentRepo.saveAll(userContents);
        }

        userContents.stream()
            .filter(e -> e.getDate().compareTo(day) == 0).findFirst()
            .ifPresentOrElse(
                (userContent) -> {
                    userContent.setDescription(description);
                    contentRepo.save(userContent);
                },
                () -> {
                    UserContent newUserContent = UserContent.builder()
                        .userId(userId.getId())
                        .date(day)
                        .description(description)
                        .build();

                    contentRepo.save(newUserContent);
                }
            );
    }

    public void savePhotoForDay(UserId userId, LocalDate day, Document document) {
        boolean haveContentForDay = hasContentForDay(userId, day);
        String fileUrl = downloadFile(document.getFileId(), day, userId, haveContentForDay);
        savePhotoForDay(userId, day, fileUrl);
    }

    public void savePhotoForDay(UserId userId, LocalDate day, List<PhotoSize> photoList) {

        PhotoSize photoSize = photoList.stream().max(Comparator.comparing(PhotoSize::getFileSize)).get();

        boolean haveContentForDay = hasContentForDay(userId, day);

        String fileUrl = downloadFile(photoSize.getFileId(), day, userId, haveContentForDay);
        savePhotoForDay(userId, day, fileUrl);
    }

    private String downloadFile(String fileId, LocalDate day, UserId userId, boolean needToReplace) {
        GetFile getFile = GetFile.builder()
            .fileId(fileId)
            .build();

        String fileNameWithoutType = day.format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));

        try {
            String filePath = chronologyBot.execute(getFile).getFilePath();
            //TODO (low): compress file
            //TODO (low): add file downloader class, unixdownloader and windowsdownloader

            String outputPhotoUrl = Utils.getOutputPhotoUrl(userId, fileNameWithoutType);
            File outputFile = new File(outputPhotoUrl);

            if (needToReplace) {
                if (!outputFile.delete()) {
                    log.error("Can't delete file");
                }
            }

            File file = chronologyBot.downloadFile(filePath, outputFile);

            return file.getAbsolutePath();

        } catch (TelegramApiException e) {
            throw new RuntimeException("Can't execute getFile");
        }
    }

    private void savePhotoForDay(UserId userId, LocalDate day, String mediaUrl) {

        List<UserContent> userContents = contentRepo.findAllByUserId(userId.getId());

        if (CollectionUtils.isEmpty(userContents)) {
            userContents = new ArrayList<>();
            contentRepo.saveAll(userContents);
        }

        userContents.stream()
            .filter(e -> e.getDate().compareTo(day) == 0).findFirst()
            .ifPresentOrElse(
                (userContent) -> {
                    userContent.setMediaUrl(mediaUrl);
                    contentRepo.save(userContent);
                },
                () -> {
                    UserContent newUserContent = UserContent.builder()
                        .userId(userId.getId())
                        .date(day)
                        .mediaUrl(mediaUrl)
                        .build();
                    contentRepo.save(newUserContent);
                }
            );
    }

    public List<Integer> getAllContentYears(UserId userId) {
        return Optional.ofNullable(contentRepo.findAllByUserId(userId.getId()))
            .map((userContents) -> {
                return userContents.stream().map(e -> e.getDate().getYear()).distinct().collect(Collectors.toList());
            })
            .orElse(Collections.emptyList());
    }

    public List<String> getAllOrderedByDateContentForYear(UserId userId, int year) {
        return Optional.ofNullable(contentRepo.findAllByUserId(userId.getId()))
            .map((userContents) -> {
                return userContents.stream()
                    .filter(e -> e.getDate().getYear() == year)
                    .sorted(Comparator.comparing(UserContent::getDate))
                    .map(UserContent::getMediaUrl)
                    .collect(Collectors.toList());
            })
            .orElse(Collections.emptyList());
    }

    public Map<UserId, List<UserContent>> getOrderedContentBetweenDates(Set<UserId> usersIds, LocalDate fromEq, LocalDate toEq) {

        if (fromEq.compareTo(toEq) > 0) {
            throw new IllegalArgumentException("'from date' can't be after 'to date'");
        }

        Set<Long> ids = usersIds.stream().map(UserId::getId).collect(Collectors.toSet());

        Map<UserId, List<UserContent>> result =
            Optional.ofNullable(contentRepo.findAllByUsersIdsAndDateBetweenAndOrderByDate(ids, fromEq, toEq))
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.groupingBy(UserContent::getUserId,  Collectors.mapping(e -> e, Collectors.toList())));

        return result;
    }

    private static final int MIN_ZONE = -12;
    private static final int MAX_ZONE = +14;
    private static final int WEEK_REPORT_NOTIFY_WISH_TIME = +12;

    /*
     * start from sunday at UTC+0 10:01 (24:00 - MAX_ZONE) and until monday up to UTC+0 12:01 (24:00 - MIN_ZONE)
     * but I want to notify not at  00:01 in local time, but in WEEK_REPORT_NOTIFY_WISH_TIME
     * */

    private static final DayOfWeek START_WEEK_DAY = DayOfWeek.MONDAY;
    private static final int START_WEEK_DAY_NUMBER = 1;

    private static final DayOfWeek END_WEEK_DAY = DayOfWeek.SUNDAY;
    private static final int END_WEEK_DAY_NUMBER = 7;

    private static final DayOfWeek FROM_WEEK_DAY = DayOfWeek.MONDAY;
    private static final DayOfWeek TO_WEEK_DAY = DayOfWeek.SUNDAY;

    private static final int START_NOTIFY_HOUR = 24 - MAX_ZONE + WEEK_REPORT_NOTIFY_WISH_TIME - 1;
    private static final int END_NOTIFY_HOUR = 24 + MIN_ZONE + WEEK_REPORT_NOTIFY_WISH_TIME - 1;

    private static final String LEFT_RANGE_CRON = "0 1 " + START_NOTIFY_HOUR +  "/1 * * " + END_WEEK_DAY_NUMBER;
    private static final String RIGHT_RANGE_CRON = "0 1 0-" + END_NOTIFY_HOUR + "/1 * * " + START_WEEK_DAY_NUMBER;

    // start at sunday each hour from 10:01
    @Scheduled(cron = LEFT_RANGE_CRON, zone = "Europe/London")
    private void scheduleWeekReportLeftRange() {
        LocalDate now = LocalDate.now();
        this.sendPreviousWeekResult(now);
        log.info("left range cron executed at time: " + OffsetDateTime.now());
    }

    // start at monday each hour up to 12:01
    @Scheduled(cron = RIGHT_RANGE_CRON, zone = "Europe/London")
    private void scheduleWeekReportRightRange() {
        LocalDate now = LocalDate.now();
        this.sendPreviousWeekResult(now);
        log.info("right range cron executed at time: " + OffsetDateTime.now());
    }

    private void sendPreviousWeekResult(LocalDate now) {

        DayOfWeek todayDayOfWeek = now.getDayOfWeek();
        if (todayDayOfWeek != START_WEEK_DAY && todayDayOfWeek != END_WEEK_DAY) {
            throw new IllegalStateException(String.format("Can be scheduled only on %s or %s", START_WEEK_DAY, END_WEEK_DAY));
        }

        if (FROM_WEEK_DAY.compareTo(TO_WEEK_DAY) > 0) {
            throw new IllegalArgumentException(String.format("fromWeekDay (%s) must be before toWeekDay (%s)", FROM_WEEK_DAY, TO_WEEK_DAY));
        }

        LocalDate fromDate = todayDayOfWeek == START_WEEK_DAY ? now.minusWeeks(1).with(FROM_WEEK_DAY): now.with(FROM_WEEK_DAY);
        LocalDate toDate = todayDayOfWeek == START_WEEK_DAY ? fromDate.with(TO_WEEK_DAY) : now;

        // take only users who have monday today
        Set<UserNotification> usersForWeeklyReport = notificationService.getUsersWithReportDateTodayOrEarlier();
        Set<UserId> usersIds = usersForWeeklyReport.stream().map(UserNotification::getUserId).collect(Collectors.toSet());

        //TODO: take only users, who turn on notifications for weekly report (by default turn on)
        // ability to turn off weekly report to to much later

        // take content for previous week from START_WEEK_DAY to END_WEEK_DAY
        Map<UserId, List<UserContent>> usersContents = this.getOrderedContentBetweenDates(usersIds, fromDate, toDate);

        usersForWeeklyReport.forEach(userNotification -> {
            UserId userId = userNotification.getUserId();
            ChatId chatId = new ChatId(userId.getId());

            messageService.sendMessage(chatId, "Вот так прошла ваша неделя:");
            List<UserContent> contents = usersContents.getOrDefault(userId, Collections.emptyList());

            if (CollectionUtils.isEmpty(contents)) {
                messageService.sendMessage(chatId, "Вы ничего не добавляли");
            } else {
                messageService.sendMultiplePhotoMessage(chatId, contents);
            }
        });

        // set next report date + 1 week
        notificationService.updateNextWeekReportDateForUsers(usersForWeeklyReport);
    }
}


