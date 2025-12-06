package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.service.diary.EntryStatusResolver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EntryStatusResolverImpl implements EntryStatusResolver {

    @Override
    public EntryStatus resolve(LocalDateTime whenStarted, LocalDateTime whenEnded) {

        LocalDateTime now = LocalDateTime.now();

        if (whenEnded.isBefore(now)) {
            return EntryStatus.FINISHED;
        }

        if (whenStarted.isAfter(now)) {
            return EntryStatus.PLANNED;
        }

        return EntryStatus.ACTIVE;
    }
}
