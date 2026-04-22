package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagMetricService {

    List<DictionaryOptionDto> getMetricsByTagId(Long tagId, Long userId, Role role);

    List<DictionaryOptionDto> getMetricsByTagIds(Collection<Long> tagIds, Long userId, Role role);

    void validateMetricTypesAllowedForTags(Set<Tag> tags, Collection<Long> metricTypeIds);
}
