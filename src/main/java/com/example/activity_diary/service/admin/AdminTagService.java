package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.diary.TagDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AdminTagService {

    Slice<TagDto> getTags(String q, Pageable pageable);

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);
}
