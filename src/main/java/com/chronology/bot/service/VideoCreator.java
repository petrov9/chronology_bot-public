package com.chronology.bot.service;

import com.chronology.bot.model.UserId;
import java.io.File;
import java.util.List;

public interface VideoCreator {
    File createVideo(UserId userId, int year, List<String> photoUrls);
}