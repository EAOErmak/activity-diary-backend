package com.example.activity_diary.service.diary;

import java.util.Collection;
import java.util.Set;

import com.example.activity_diary.entity.diary.Tag;

public interface TagResolverService {
    Set<Tag> resolveForUser(Long userId, Collection<String> rawNames);
    Set<Tag> resolveFromDescription(Long userId, String description);
}
