package com.chronology.bot.repository;

import com.chronology.bot.model.UserNotification;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepo extends CrudRepository<UserNotification, Long> {

    List<UserNotification> findAllByIsNotify(boolean isNotify);
}
