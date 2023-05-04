package com.chronology.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "user_info")
public class UserInfo {

    @Id
    @Column(name = "user_id")
    @NonNull
    private Long userId;

    @Column(name = "chat_id")
    @NonNull
    private Long chatId;

    public UserId getUserId() {
        return new UserId(userId);
    }

    public ChatId getChatId() {
        return chatId != 0 ? new ChatId(chatId) : null;
    }

    public void setChatId(ChatId chatId) {
        this.chatId = chatId.getId();
    }
}
