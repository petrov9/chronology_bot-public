package com.chronology.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "user_content")
public class UserContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "date", columnDefinition = "DATE WITHOUT TIME ZONE")
    private LocalDate date;
    @Column(name = "media_url")
    private String mediaUrl;
    @Column(name = "description")
    private String description;

    public UserId getUserId() {
        return new UserId(userId);
    }

    public void setUserId(UserId userId) {
        this.userId = userId.getId();
    }
}
