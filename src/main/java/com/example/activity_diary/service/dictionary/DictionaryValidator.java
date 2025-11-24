package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.exception.types.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DictionaryValidator {

    private static final int MAX_LEN = 100;

    // Простой emoji-фильтр (не идеальный, но рабочий)
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\p{So}\\p{Cn}]"
    );

    // Мусорные шаблоны
    private static final Pattern TRASH_PATTERN = Pattern.compile(
            "^[!\\-_.]+$"
    );

    public String validateName(String name) {

        if (name == null)
            throw new BadRequestException("Name must not be null");

        String normalized = name.trim();

        if (normalized.isEmpty())
            throw new BadRequestException("Name must not be empty");

        if (normalized.length() > MAX_LEN)
            throw new BadRequestException("Name is too long (max " + MAX_LEN + ")");

        if (normalized.contains("\n") || normalized.contains("\r"))
            throw new BadRequestException("Name must be on a single line");

        if (EMOJI_PATTERN.matcher(normalized).find())
            throw new BadRequestException("Name contains forbidden characters (emoji)");

        if (TRASH_PATTERN.matcher(normalized).matches())
            throw new BadRequestException("Name cannot consist only of punctuation");

        return normalized;
    }
}
