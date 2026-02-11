package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.diary.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);
}
