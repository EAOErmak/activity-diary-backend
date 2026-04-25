package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.diary.TagUpdateDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AdminTagService {

    TagDto create(TagCreateDto dto);

    TagDto update(Long id, TagUpdateDto dto);

    void delete(Long id);

    Slice<TagDto> getTags(String q, Pageable pageable);

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);
}
