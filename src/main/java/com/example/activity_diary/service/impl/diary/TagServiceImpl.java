package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.diary.TagService;
import com.example.activity_diary.service.sync.GlobalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final GlobalSyncService globalSyncService;

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
        globalSyncService.bump(GlobalSyncEntityType.TAG);
    }

    @Override
    @Transactional
    public void reject(Long tagId) {
        Tag tag = get(tagId);
        tag.setStatus(TagStatus.REJECTED);
        globalSyncService.bump(GlobalSyncEntityType.TAG);
    }

    @Override
    @Transactional
    public void deprecate(Long tagId) {
        Tag tag = get(tagId);
        tag.setStatus(TagStatus.DEPRECATED);
        globalSyncService.bump(GlobalSyncEntityType.TAG);
    }

    private Tag get(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private String normalizeQuery(String q) {
        if (q == null) return null;
        String s = q.trim();
        if (s.isEmpty()) return null;
        return s.toLowerCase();
    }
}
