package com.library.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {
    public static final String dateTimeFormat = "dd-MM-yyyy HH:mm:ss";
    public static final String dateFormat = "dd-MM-yyyy";
    public static final long yearMS = 31536000000L;

    public static final DateTimeFormatter datePickerEditorFormatter = DateTimeFormatter.ofPattern("d.MM.yyyy");

    public static String getCurrentFormattedDate(long timestamp) {
        return new SimpleDateFormat(DateTimeUtils.dateTimeFormat).format(new Date(timestamp * 1000));
    }

    public static LocalDate timestampToLocalDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static String timestampToFormattedString(long timestamp) {
        return timestampToLocalDate(timestamp).format(DateTimeFormatter.ofPattern(dateFormat));
    }
}
