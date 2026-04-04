package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.admin.AdminTagService;

import com.example.activity_diary.service.sync.GlobalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final TagRepository tagRepository;
    private final GlobalSyncService globalSyncService;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDto create(TagCreateDto dto) {
        String name = normalize(dto.getName());
        validateName(name);

        if (tagRepository.findByName(name).isPresent()) {
            throw new BadRequestException("Tag already exists");
        }

        Tag saved = tagRepository.save(
                Tag.builder()
                        .name(name)
                        .status(TagStatus.APPROVED)
                        .build()
        );

        globalSyncService.bump(GlobalSyncEntityType.TAG);

        return tagMapper.toDto(saved);
    }

    @Override
    public Slice<TagDto> getTags(String q, Pageable pageable) {
        String query = normalizeQuery(q);

        Slice<Tag> slice = (query == null)
                ? tagRepository.findAllSlice(pageable)
                : tagRepository.searchSlice(query, pageable);

        return slice.map(tagMapper::toDto);
    }

    private String normalizeQuery(String q) {
        if (q == null) return null;
        String s = q.trim();
        if (s.isEmpty()) return null;
        return s.toLowerCase();
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

    private String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        return s.replaceAll("[^\\p{L}\\p{N}_\\-]+", "");
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Tag name is required");
        }
        if (name.length() < 2 || name.length() > 32) {
            throw new BadRequestException(
                    "Tag name must be 2..32 characters and contain only letters, digits, '_' or '-'"
            );
        }
    }
}

