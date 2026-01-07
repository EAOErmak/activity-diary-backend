package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {
    public Set<Tag> resolveTags(List<String> rawTags);

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);

    public List<TagDto> getAllTags();

    public List<TagDto> searchTags(String query);
}
