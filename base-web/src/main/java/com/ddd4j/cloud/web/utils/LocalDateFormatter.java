package com.ddd4j.cloud.web.utils;

import org.springframework.format.Formatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateFormatter implements Formatter<LocalDate> {
    public final DateTimeFormatter FORMATTER;

    public LocalDateFormatter(String pattern) {
        FORMATTER = DateTimeFormatter.ofPattern(pattern, Locale.CHINESE);
    }

    public LocalDate parse(String text, Locale locale) {
        return LocalDate.parse(text, FORMATTER);
    }

    public String print(LocalDate object, Locale locale) {
        return FORMATTER.format(object);
    }

}
