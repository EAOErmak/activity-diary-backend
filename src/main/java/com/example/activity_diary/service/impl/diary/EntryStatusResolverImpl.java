package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.EntryStatusPolicy;
import com.example.activity_diary.service.diary.EntryStatusResolver;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EntryStatusResolverImpl implements EntryStatusResolver {

    @Override
    public EntryStatus resolve(Instant whenStarted, Instant whenEnded, Instant now) {
        return EntryStatusPolicy.resolveAutomaticStatus(whenStarted, whenEnded, now);
    }
}
