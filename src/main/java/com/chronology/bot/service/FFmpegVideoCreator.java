package com.chronology.bot.service;

import com.chronology.bot.model.UserId;
import com.chronology.bot.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FFmpegVideoCreator implements VideoCreator {

    public File createVideo(UserId userId, int year, List<String> photoUrls) {

        String outputVideoUrl = Utils.getOutputVideoUrl(userId, year);
        String ffmpegCommand = getFfmpegCmdCreateVideoCommand(outputVideoUrl, photoUrls);

        File videoFile = executeCommand(outputVideoUrl, ffmpegCommand);

        String filterFileName = outputVideoUrl.replaceAll("\\.mp4", "_filter.txt");
        File file = new File(filterFileName);
        file.delete();

        File resultVideo = videoFile;

        boolean addSubtitles = false;

        if (addSubtitles) {
            String subtitlesUrl = Utils.getSubtitlesUrl(userId, year);
            String addSubtitlesCommand = getFfmpegCmdAddSubtitlesCommand(subtitlesUrl,videoFile.getAbsolutePath());
            File videoFileWithSubtitles = executeCommand(outputVideoUrl, addSubtitlesCommand);
            resultVideo = videoFileWithSubtitles;
        }

        return resultVideo;
    }

    private File executeCommand(String outputVideUrl, String ffmpegCommand) {
        try {

            final Runtime r = Runtime.getRuntime();
            final Process p = r.exec(ffmpegCommand);

            int returnCode = p.waitFor();
            log.info("return code:" + returnCode);

            int timeout = 10;
            boolean waitFor = p.waitFor(timeout, TimeUnit.MINUTES);
            if (!waitFor) {
                log.error("Command was executing too much, more than " + timeout + " mins");
                return null;
            }

            final BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = is.readLine()) != null) {
                log.info(line);
            }
            final BufferedReader is2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = is2.readLine()) != null) {
                log.info(line);
            }

        } catch (Exception e) {
            log.error("Can't create video", e);
            return null;
        }

        return new File(outputVideUrl);
    }

    private String getFfmpegCmdCreateVideoCommand(String outputUrl, List<String> photoUrls) {
        String musicUrl = Utils.getMusicUrl("music");
        String ffmpegUrl = Utils.getFFmpegUrl();
        String cmdLineSeparator = Utils.getCmdSeparator();

        String videoCommand = generateCreateVideoCommand(ffmpegUrl, musicUrl, outputUrl, cmdLineSeparator, photoUrls);
        log.info("Video command: " + videoCommand);

        return videoCommand;
    }

    private String generateCreateVideoCommand(String ffmpegUrl, String musicUrl, String outputUrl, String cmdLineSeparator, List<String> photoUrls) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ffmpegUrl).append(cmdLineSeparator);

        StringBuilder filterComplexBuilder = new StringBuilder();
        StringBuilder filterConcatBuilder = new StringBuilder();

        String filterFileName = outputUrl.replaceAll("\\.mp4", "_filter.txt");

        for (int i = 0; i < photoUrls.size(); i++) {

            String photoUrl = photoUrls.get(i);

            stringBuilder.append("-loop 1 -t 2 -i ").append(photoUrl).append(cmdLineSeparator);

            String label = "[v" + i + "]";

            filterComplexBuilder
                .append("[").append(i)

                .append(":v]scale=%SCALE%")
                .append(":force_original_aspect_ratio=%ASPECT_RATIO%,")

                // https://ffmpeg.org/ffmpeg-all.html#pad-1
                .append("pad=%SCALE%").append(":(ow-iw)/2:(oh-ih)/2,")

                .append("setsar=1,")
                .append(i == 0 ? "" : "fade=t=in:st=0:d=1,") // для первой картинки нет fade in
                .append("fade=t=out:st=4:d=1").append(label).append(";");
//                .append(cmdLineSeparator);

            filterConcatBuilder.append(label);
        }

        stringBuilder.append("-i ").append(musicUrl).append(cmdLineSeparator);

        stringBuilder.append("-filter_complex_script").append(cmdLineSeparator);

        stringBuilder.append(filterFileName).append(cmdLineSeparator);

        String filterContent = filterComplexBuilder.append(filterConcatBuilder).append("concat=n=").append(photoUrls.size()).append(":v=1:a=0,format=yuv420p[v]").toString();
        filterContent = applyVarsToPattern(filterContent);
        log.info("filter content: " + filterContent);

        File file = new File(filterFileName);
        try {
            boolean isFileCreated = file.createNewFile();

            if (isFileCreated) {
                writeToFile(filterFileName, filterContent);
            } else {
                log.error("File wasn't created");
            }

        } catch (IOException e) {
            log.error("Can't create filter file", e);
        }

        stringBuilder.append("-map [v] -map ").append(photoUrls.size()).append(":a -shortest ").append(outputUrl).append(" -loglevel error");

        String resultCmdCommand = applyVarsToPattern(stringBuilder.toString());

        return resultCmdCommand;
    }

    private void writeToFile(String fileName, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
            writer.write(text);

            writer.close();
        } catch (IOException e) {
            log.error("Can't write to filter file", e);
        }
    }

    private String applyVarsToPattern(String pattern) {
        Map<String, String> vars = new HashMap<>();
        vars.put("%ASPECT_RATIO%", "decrease");
        vars.put("%SCALE%", "1080:1920");

        for (Map.Entry<String, String> entry: vars.entrySet()) {
            pattern = pattern.replaceAll(entry.getKey(), entry.getValue());
        }

        return pattern;
    }

    private String getFfmpegCmdAddSubtitlesCommand(String subtitlesUrl, String videoUrl) {
        String ffmpegUrl = Utils.getFFmpegUrl();
        String cmdLineSeparator = Utils.getCmdSeparator();

        return generateAddSubtitlesCommand(ffmpegUrl, videoUrl, subtitlesUrl, cmdLineSeparator);
    }

    private String generateAddSubtitlesCommand(String ffmpegUrl, String videoUrl, String subtitlesUrl, String cmdLineSeparator) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ffmpegUrl).append(cmdLineSeparator);

        stringBuilder.append("-i ").append(videoUrl).append(cmdLineSeparator);
        stringBuilder.append("-vf \"subtitles=").append(subtitlesUrl).append(":force_style='Fontname=Futura,PrimaryColour=&HFFFFFF,BackColour=&H80000000,BorderStyle=4'\"")
            .append(cmdLineSeparator);

        return stringBuilder.toString();
    }

}