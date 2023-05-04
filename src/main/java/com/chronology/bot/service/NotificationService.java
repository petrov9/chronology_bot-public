package com.chronology.bot.service;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserId;
import com.chronology.bot.model.UserNotification;
import com.chronology.bot.repository.NotificationRepo;
import com.chronology.bot.utils.Utils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NotificationService {

    private RestTemplate restTemplate;

    private NotificationRepo notificationRepo;

    private MessageService messageService;

    public NotificationService(RestTemplate restTemplate, NotificationRepo notificationRepo, MessageService messageService) {
        this.restTemplate = restTemplate;
        this.notificationRepo = notificationRepo;
        this.messageService = messageService;
    }

    public void enableUserNotification(UserId userId, boolean isNotify) {
        long id = userId.getId();

        notificationRepo.findById(id)
            .ifPresentOrElse(
                (notification) -> {
                    notification.setNotify(isNotify);
                    notificationRepo.save(notification);
                },
                () -> {
                    notificationRepo.save(UserNotification.builder().userId(id).isNotify(isNotify).build());
                }
            );
    }

    public UserNotification findNotification(UserId userId) {
        return notificationRepo.findById(userId.getId()).orElse(null);
    }

    private LocalDateTime getUtcTime() {
        HashMap map = null;
        boolean isGood = false;
        int retryTimes = -1;

        while (!isGood) {
            try {
                retryTimes++;
                map = restTemplate.getForObject("http://worldtimeapi.org/api/timezone/Europe/London", HashMap.class);
                isGood = true;
            } catch (RestClientException e) {

                if (retryTimes >= 3) {
                    log.error("Cant' proceed rest template", e);
                    break;
                } else {
                    try {
                        Thread.sleep(1000 * Math.max(retryTimes, 1));
                    } catch (InterruptedException ex) {
                        log.error("interruption", ex);
                        break;
                    }
                }

            } catch (Exception e) {
                log.error("Cant' proceed rest template", e);
                break;
            }
        }

        if (map == null) {
            throw new RuntimeException();
        }

        String datetime = (String) map.get("utc_datetime");
        datetime = datetime.substring(0, 16);
        LocalDateTime utcDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        return utcDateTime;
    }

    public String setUserTimezone(UserId userId, LocalDateTime userDateTime) {

        String utcZone = notificationRepo.findById(userId.getId()).map(
                (notification) -> {
                    //TODO (low): check future date, and case if wrong user date for 1 min

                    LocalDateTime utcDateTime = getUtcTime();
                    int hours = (int) (userDateTime.toEpochSecond(ZoneOffset.UTC) - utcDateTime.toEpochSecond(ZoneOffset.UTC)) / 3600;

                    ZoneOffset zoneOffset = ZoneOffset.ofHours(hours);
                    notification.setZoneOffset(zoneOffset);
                    notification.setDate(utcDateTime.toLocalDate());
                    notificationRepo.save(notification);

                    return "UTC" + zoneOffset.toString();

                })
            .orElseThrow(() -> {
                throw new IllegalStateException("User don't have notification info");
            });

        return utcZone;

    }

    public void setNotificationTime(UserId userId, LocalTime desireTime) {
        notificationRepo.findById(userId.getId())
            .ifPresentOrElse(
                (notification) -> {
                    notification.setTime(desireTime);
                    notificationRepo.save(notification);
                },
                () -> {
                    throw new IllegalStateException("User don't have notification info");
                }
            );
    }

    //TODO (high): don't send notification if user already added this day
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void sendNotification() {
        Iterable<UserNotification> all = notificationRepo.findAllByIsNotify(true);

        LocalDateTime utcTime = getUtcTime();

        all.forEach(notification -> {
            LocalTime time = notification.getTime();
            LocalDate date = notification.getDate();
            ZoneOffset zoneOffset = notification.getZoneOffset();

            LocalDateTime userTimeInUtc = date.atTime(time).atOffset(zoneOffset).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            if (userTimeInUtc.compareTo(utcTime) <= 0) {
                // Work only when chatId == userId
                ChatId chatId = new ChatId(notification.getUserId().getId());
                notifyUser(chatId);
                notification.setDate(date.plusDays(1));
                notificationRepo.save(notification);
            }

        });
    }

    private void notifyUser(ChatId chatId) {
        messageService.sendMessage(chatId, "Время добавить запись");
    }

    /*
     * Take users with userReportDate <= todayDate
     * */
    public Set<UserNotification> getUsersWithReportDateTodayOrEarlier() {
        Set<UserNotification> usersIds = StreamSupport.stream(notificationRepo.findAll().spliterator(), false).filter(userNotification -> {
            ZoneOffset userZoneOffset = userNotification.getZoneOffset();
            LocalDate userWeekReportDate = userNotification.getWeekReportDate();
            LocalDate todayInUserZone = Utils.getSystemDateInOffsetZone(userZoneOffset);

            if (userWeekReportDate != null && userWeekReportDate.isAfter(todayInUserZone)) {
                return false;
            }

            return true;

        }).collect(Collectors.toSet());

        return usersIds;
    }

    public void updateNextWeekReportDateForUsers(Set<UserNotification> users) {

        users.forEach(userNotification -> {
            LocalDate today = Utils.getSystemDateInOffsetZone(userNotification.getZoneOffset());
            userNotification.setWeekReportDate(today.plusWeeks(1));
        });

        notificationRepo.saveAll(users);
    }
}
