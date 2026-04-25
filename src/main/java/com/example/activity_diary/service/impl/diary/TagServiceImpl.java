package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final DiaryRepository diaryRepository;
    private final TagChartTypeLinkRepository tagChartTypeLinkRepository;
    private final TagMetricLinkRepository tagMetricLinkRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getVisibleTags(Long userId, Role role, String q) {
        String query = normalizeQuery(q);
        List<Tag> tags;
        if (role == Role.ADMIN) {
            tags = query == null
                    ? tagRepository.findAllForAdmin()
                    : tagRepository.searchAllForAdmin(query);
        } else {
            tags = query == null
                    ? tagRepository.findAllVisible(userId)
                    : tagRepository.searchVisible(userId, query);
        }

        return tagMapper.toDtoList(tags);
    }

    @Override
    @Transactional
    public void approve(Long tagId) {
        Tag tag = get(tagId);
        tag.setStatus(TagStatus.APPROVED);
    }

    @Override
    @Transactional
    public void reject(Long tagId) {
        Tag tag = get(tagId);
        tag.setStatus(TagStatus.REJECTED);
    }

    @Override
    @Transactional
    public void deprecate(Long tagId) {
        Tag tag = get(tagId);
        tag.setStatus(TagStatus.DEPRECATED);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = get(tagId);
        ensureCanDelete(tagId);

        try {
            tagRepository.delete(tag);
            tagRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Tag cannot be deleted because it is in use");
        }
    }

    private void ensureCanDelete(Long tagId) {
        if (diaryRepository.existsByTags_Id(tagId)) {
            throw new BadRequestException("Tag cannot be deleted because it is used by diary entries");
        }

        if (tagChartTypeLinkRepository.existsByTagId(tagId)) {
            throw new BadRequestException("Tag cannot be deleted because it has chart type links");
        }

        if (tagMetricLinkRepository.existsByTagId(tagId)) {
            throw new BadRequestException("Tag cannot be deleted because it has metric links");
        }
    }

    private Tag get(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private String normalizeQuery(String q) {
        String s = DiaryDescriptionTagPolicy.normalizeCanonicalTagName(q);
        if (s == null) return null;
        if (s.isEmpty()) return null;
        return s;
    }
}
