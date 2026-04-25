package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.admin.AdminTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private static final int MAX_TAG_NAME_LENGTH = 64;

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDto create(TagCreateDto dto) {
        String name = normalizeAdminTagName(dto.getName());
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

    private String normalizeAdminTagName(String raw) {
        if (raw == null) return null;
        return raw.trim().toLowerCase(Locale.ROOT);
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

    private Tag get(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Tag name is required");
        }
        if (name.startsWith("#")
                || name.length() > MAX_TAG_NAME_LENGTH
                || name.chars().anyMatch(Character::isWhitespace)) {
            throw new BadRequestException(
                    "Tag name must not start with '#' and must not contain spaces"
            );
        }
    }
}
