package com.example.activity_diary.service.impl.diary;


import com.example.activity_diary.dto.mapper.DayTemplateMapper;
import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.day.DayTemplateUpdateDto;
import com.example.activity_diary.dto.template.day.DayTemplateViewDto;
import com.example.activity_diary.dto.template.day.DayTemplateItemDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.TemplateEntryItemRepository;
import com.example.activity_diary.service.diary.DayTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DayTemplateServiceImpl implements DayTemplateService {

    private final DayTemplateRepository dayTemplateRepository;
    private final DiaryEntryTemplateRepository entryTemplateRepository;
    private final TemplateEntryItemRepository templateEntryItemRepository;

    private final DayTemplateMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DayTemplateViewDto> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<DayTemplate> templatesPage = dayTemplateRepository.findAllByUser_Id(userId, pageable);
        Map<Long, List<TemplateEntryItem>> itemsByTemplateId = loadItemsByDayTemplateId(templatesPage.getContent());

        List<DayTemplateViewDto> content = templatesPage.getContent().stream()
                .map(template -> toView(template, itemsByTemplateId.getOrDefault(template.getId(), List.of())))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, templatesPage.getTotalElements());
    }

    @Override
    public DayTemplateViewDto create(Long userId, DayTemplateCreateDto dto) {
        String name = normalizeName(dto.getName());
        if (dayTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
            throw new BadRequestException("Day template name already exists");
        }

        DayTemplate tpl = DayTemplate.builder()
                .user(refUser(userId))
                .name(name)
                .build();

        applyItemsReplace(userId, tpl, dto.getItems());

        DayTemplate saved = dayTemplateRepository.save(tpl);
        return mapper.toView(saved);
    }

    @Override
    public DayTemplateViewDto update(Long userId, Long id, DayTemplateUpdateDto dto) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));

        if (dto.getName() != null) {
            String name = normalizeName(dto.getName());
            if (!name.equalsIgnoreCase(tpl.getName())
                    && dayTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
                throw new BadRequestException("Day template name already exists");
            }
            tpl.setName(name);
        }

        if (dto.getItems() != null) {
            applyItemsReplace(userId, tpl, dto.getItems());
        }

        if (dto.getItems() != null) {
            return mapper.toView(tpl);
        }

        return toView(
                tpl,
                loadItemsByDayTemplateId(List.of(tpl)).getOrDefault(tpl.getId(), List.of())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DayTemplateViewDto get(Long userId, Long id) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));
        return toView(
                tpl,
                loadItemsByDayTemplateId(List.of(tpl)).getOrDefault(tpl.getId(), List.of())
        );
    }

    @Override
    public void delete(Long userId, Long id) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));
        dayTemplateRepository.delete(tpl);
    }

    private void applyItemsReplace(Long userId, DayTemplate tpl, List<DayTemplateItemDto> items) {
        // 1) очистить старые items
        tpl.getItems().clear();

        if (items == null || items.isEmpty()) return;

        // 2) валидация positions (уникальные и >0)
        Set<Integer> posSet = new HashSet<>();
        for (var it : items) {
            if (it.getEntryTemplateId() == null) throw new BadRequestException("entryTemplateId is required");
            if (it.getPosition() == null || it.getPosition() < 1) throw new BadRequestException("position must be >= 1");
            if (!posSet.add(it.getPosition())) throw new BadRequestException("duplicate position in day template");
        }

        // 3) загрузить все DiaryEntryTemplate одним запросом (и только пользователя)
        List<Long> ids = items.stream().map(DayTemplateItemDto::getEntryTemplateId).toList();
        Map<Long, DiaryEntryTemplate> byId = entryTemplateRepository
                .findAllByIdInAndUser_Id(ids, userId)
                .stream().collect(Collectors.toMap(BaseEntity::getId, x -> x));

        for (var it : items) {
            DiaryEntryTemplate entryTpl = byId.get(it.getEntryTemplateId());
            if (entryTpl == null) throw new BadRequestException("Entry template not found: " + it.getEntryTemplateId());

            TemplateEntryItem e = TemplateEntryItem.builder()
                    .dayTemplate(tpl)
                    .entryTemplate(entryTpl)
                    .position(it.getPosition())
                    .build();

            tpl.getItems().add(e);
        }
    }

    private static String normalizeName(String name) {
        String v = name == null ? null : name.trim();
        if (v == null || v.isBlank()) throw new BadRequestException("name is required");
        if (v.length() > 120) throw new BadRequestException("name is too long");
        return v;
    }

    private static User refUser(Long userId) {
        User u = new User();
        u.setId(userId);
        return u;
    }

    private Map<Long, List<TemplateEntryItem>> loadItemsByDayTemplateId(List<DayTemplate> templates) {
        List<Long> templateIds = templates.stream()
                .map(BaseEntity::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (templateIds.isEmpty()) {
            return Map.of();
        }

        return templateEntryItemRepository.findAllByDayTemplateIdInWithEntryTemplate(templateIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getDayTemplate().getId(),
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private DayTemplateViewDto toView(DayTemplate template, List<TemplateEntryItem> items) {
        DayTemplateViewDto dto = new DayTemplateViewDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setItems(items.stream().map(mapper::toViewItem).toList());
        return dto;
    }
}

