package com.chronology.bot.model;

import com.chronology.bot.service.steps.StepType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class UserActiveCommand {

    private UserId userId;
    private String command;

    @Setter
    private StepType stepType;

    public void setCommand(String command) {
        this.command = command;
    }
}
