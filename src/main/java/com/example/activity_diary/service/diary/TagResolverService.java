package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.Tag;

import java.util.Collection;
import java.util.Set;

public interface TagResolverService {
    Set<Tag> resolveForUser(Long userId, Collection<String> rawNames);
    Set<Tag> resolveFromDescription(Long userId, String description);
}
