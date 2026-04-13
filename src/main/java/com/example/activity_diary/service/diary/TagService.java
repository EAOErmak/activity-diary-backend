package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.enums.Role;

import java.util.List;

public interface TagService {

    List<TagDto> getVisibleTags(Long userId, Role role, String q);

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);
}
