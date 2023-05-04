package com.chronology.bot.service;

import com.chronology.bot.model.ChatId;
import com.chronology.bot.model.UserActiveCommand;
import com.chronology.bot.model.UserId;
import com.chronology.bot.service.handler.CommandHandler;
import com.chronology.bot.service.steps.StepType;
import com.chronology.bot.service.command.ICommand;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CommandService {

    private MessageService messageService;

    public CommandService(MessageService messageService) {
        this.messageService = messageService;
    }

    public Optional<ICommand> getActiveCommand(ChatId chatId, UserId userId) {
        Optional<ICommand> concreteCommand = Optional.ofNullable(ChronologyBot.userActiveCommand.get(userId)).map(userActiveCommand -> {
            return CommandHandler.getConcreteCommand(chatId, userActiveCommand.getCommand());
        });

        return concreteCommand;
    }

    private boolean userHasActiveCommand(UserId userId) {
        return ChronologyBot.userActiveCommand.containsKey(userId);
    }

    public void setActiveCommandToUser(ChatId chatId, UserId userId, String command, StepType stepType) {

        if (userHasActiveCommand(userId)) {
            messageService.sendMessage(chatId, "Сначала завершите текущую команду");
            return;
        }

        Optional<UserActiveCommand> activeCommand = Optional.ofNullable(ChronologyBot.userActiveCommand.get(userId));

        activeCommand.ifPresentOrElse(
            (aCommand) -> {
                aCommand.setCommand(command);
            },
            () -> {
                UserActiveCommand newCommand = new UserActiveCommand(userId, command, stepType);
                ChronologyBot.userActiveCommand.put(userId, newCommand);
            }
        );
    }

    public void removeActiveCommandForUser(UserId userId) {
        ChronologyBot.userActiveCommand.remove(userId);
    }

    public StepType getActiveCommandStep(UserId userId) {
        return Optional.ofNullable(ChronologyBot.userActiveCommand.get(userId))
            .map(UserActiveCommand::getStepType)
            .orElseThrow(() -> {
                throw new IllegalStateException("User doesn't have active command");
            });
    }

    public void setNextStep(UserId userId, StepType type) {
        Optional.ofNullable(ChronologyBot.userActiveCommand.get(userId))
            .ifPresentOrElse(
                (command) -> {
                    command.setStepType(type);
                },
                () -> {
                    throw new IllegalStateException("User don't have active command");
                }
            );
    }
}


