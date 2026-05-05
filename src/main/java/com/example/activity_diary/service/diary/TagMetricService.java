package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.Role;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagMetricService {

    List<DictionaryOptionDto> getMetricsByTagId(Long tagId, Long userId, Role role);

    PageResponseDto<DictionaryOptionDto> getMetricsByTagIds(
            Collection<Long> tagIds,
            Long userId,
            Role role,
            String q,
            Pageable pageable
    );

    void validateMetricTypesAllowedForTags(Set<Tag> tags, Collection<Long> metricTypeIds);
}
