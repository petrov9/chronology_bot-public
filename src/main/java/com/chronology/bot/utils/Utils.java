package com.chronology.bot.utils;

import com.chronology.bot.model.UserId;
import com.chronology.bot.service.SpringContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.core.env.Environment;

public class Utils {

    public static final String UNIX_HOME = getUnixHome();
    public static final String WIN_HOME = getWinHome();

    private static final String MUSIC_FOLDER = "music";
    private static final String VIDEO_FOLDER = "video";
    private static final String PHOTO_FOLDER = "photo";

    private static final String SUBTITLES_FOLDER = "subtitles";
    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };// Operating systems.

    private static OS getOS() {
        String operSys = System.getProperty("os.name").toLowerCase();
        if (operSys.contains("win")) {
            return OS.WINDOWS;
        } else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
            return OS.LINUX;
        } else if (operSys.contains("mac")) {
            return OS.MAC;
        } else if (operSys.contains("sunos")) {
            return OS.SOLARIS;
        } else {
            throw new IllegalStateException("Unknown OS");
        }
    }

    public static boolean isUnix() {
        boolean isUnix = Utils.getOS() == OS.LINUX;
        return isUnix;
    }

    public static String getOsHome() {
        String osHome = Utils.getOS() == OS.LINUX ? UNIX_HOME : WIN_HOME;
        return osHome;
    }

    public static String getOutputVideoUrl(UserId userId, int year) {
        String videoUrl = String.format("%s/%s/%s/%s_%s.mp4", getOsHome(), PHOTO_FOLDER, userId.getId(), year, UUID.randomUUID());
        return videoUrl;
    }

    public static String getOutputPhotoUrl(UserId userId, String fileNameWothoutType) {
        String photoUrl = String.format("%s/%s/%s/%s.jpg", getOsHome(), PHOTO_FOLDER, userId.getId(), fileNameWothoutType);
        return photoUrl;
    }

    public static String getSubtitlesUrl(UserId userId, int year) {
        String subtitlesUrl = String.format("%s/%s/%s/%s.srt", getOsHome(), SUBTITLES_FOLDER, userId.getId(), year);
        return subtitlesUrl;
    }

    public static String getMusicUrl(String musicNameWithoutType) {
        String photoUrl = String.format("%s/%s/%s.wav", getOsHome(), MUSIC_FOLDER, musicNameWithoutType);
        return photoUrl;
    }

    public static String getFFmpegUrl() {
        String ffmpegWindowsUrl = getProp("ffmpeg.url");
        String ffmpegUnixUrl = "sudo ffmpeg";
        return Utils.isUnix() ? ffmpegUnixUrl : ffmpegWindowsUrl;
    }

    public static String getCmdSeparator() {
        String cmdWindowsLineSeparator = " ";
        String cmdUnixLineSeparator = " ";
        return Utils.isUnix() ? cmdUnixLineSeparator : cmdWindowsLineSeparator;
    }

    private static String getWinHome() {
        return getProp("home.windows");
    }

    private static String getUnixHome() {
        return getProp("home.unix");
    }

    private static String getProp(String key) {
        Environment env = SpringContext.getBean(Environment.class);
        return env.getProperty(key);
    }

    public static LocalDate getSystemDateInOffsetZone(ZoneOffset userOffsetZone) {
        OffsetDateTime systemNow = OffsetDateTime.now();
        ZoneOffset systemOffset = systemNow.getOffset();

        int secondsDiff = userOffsetZone.getTotalSeconds() - systemOffset.getTotalSeconds();
        LocalDate systemDateInUserZone = systemNow.plusSeconds(secondsDiff).toLocalDate();

        return systemDateInUserZone;
    }
}
