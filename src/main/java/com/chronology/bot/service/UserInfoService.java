package com.chronology.bot.service;

import com.chronology.bot.model.UserId;
import com.chronology.bot.model.UserNotification;
import com.chronology.bot.repository.NotificationRepo;
import com.chronology.bot.repository.UserInfoRepo;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserInfoService {

    private UserInfoRepo userInfoRepo;

    //TODO (high): UserNotification.zoneOffset need to move to UserInfo or somewhere else, and after that to delete notification repo from here
    //TODO (high): after /start user has to write date time in order to get user timezone, he can skip this step, in that case user zone will be consider as
    // utc+0
    private NotificationRepo notificationRepo;

    public UserInfoService(UserInfoRepo userInfoRepo, NotificationRepo notificationRepo) {
        this.userInfoRepo = userInfoRepo;
        this.notificationRepo = notificationRepo;
    }

    /*
     * If user doesn't have zone offset, it will be considered as utc+0
     * */
    public ZoneOffset getUserZoneOffset(UserId userId) {
        ZoneOffset defaultZoneForNoZoneUsers = ZoneOffset.ofHours(0);
        ZoneOffset zoneOffset = notificationRepo.findById(userId.getId()).map(UserNotification::getZoneOffset).orElse(defaultZoneForNoZoneUsers);

        return zoneOffset;
    }
}

