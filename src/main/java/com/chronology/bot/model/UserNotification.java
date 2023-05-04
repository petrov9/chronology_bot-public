package com.chronology.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

@Entity
@Table(name="user_notifications")
public class UserNotification {

    @Id
    @Column(name = "user_id")
    private long userId;
    @Column(name = "is_notify")
    private boolean isNotify;
    @Column(name = "zone_offset")
    private ZoneOffset zoneOffset;
    @Column(name = "time", columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime time;
    @Column(name = "date", columnDefinition = "DATE WITHOUT TIME ZONE")
    private LocalDate date;

    @Column(name = "week_report_date", columnDefinition = "DATE WITHOUT TIME ZONE")
    private LocalDate weekReportDate;


    public UserId getUserId() {
        return new UserId(userId);
    }

    public void setUserId(UserId userId) {
        this.userId = userId.getId();
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset == null ? ZoneOffset.UTC : zoneOffset;
    }
}
