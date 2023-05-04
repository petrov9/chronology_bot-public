package com.chronology.bot.service;

import com.chronology.bot.model.UserId;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class VideoService {

    private ContentService contentService;
    private VideoCreator videoCreator;

    public VideoService(ContentService contentService, VideoCreator videoCreator) {
        this.contentService = contentService;
        this.videoCreator = videoCreator;
    }

    public CompletableFuture<File> createVideo(UserId userId, int year) {

        CompletableFuture<File> completableFuture = CompletableFuture.supplyAsync(() -> {
            List<String> orderedMediaUrls = contentService.getAllOrderedByDateContentForYear(userId, year);
            File video = videoCreator.createVideo(userId, year, orderedMediaUrls);
            return video;
        });

        return completableFuture;
    }

    public void removeVideoFromServer(File videoFile) {
        boolean delete = videoFile.delete();
        if (!delete) {
            log.error("Can't delete video " + videoFile.getAbsolutePath());
        }
    }
}