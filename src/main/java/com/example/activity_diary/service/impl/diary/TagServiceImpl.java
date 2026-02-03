package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.TagRepository;
import com.example.activity_diary.service.diary.TagService;
import com.example.activity_diary.service.sync.GlobalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final GlobalSyncService globalSyncService;
    private final TagMapper tagMapper;

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

    private Tag findOrCreate(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    Tag tag = tagRepository.save(
                            Tag.builder()
                                    .name(name)
                                    .status(TagStatus.PROPOSED)
                                    .build()
                    );

                    globalSyncService.bump(GlobalSyncEntityType.TAG);

                    return tag;
                });
    }

    private Tag get(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private String normalize(String raw) {
        return raw.trim().toLowerCase();
    }
}
