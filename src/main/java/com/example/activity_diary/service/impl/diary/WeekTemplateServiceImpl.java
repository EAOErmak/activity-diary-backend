package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.mapper.WeekTemplateMapper;
import com.example.activity_diary.dto.template.week.WeekTemplateDayItemDto;
import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateUpdateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateViewDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.TemplateDayItem;
import com.example.activity_diary.entity.template.WeekTemplate;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.TemplateDayItemRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import com.example.activity_diary.service.diary.WeekTemplateService;
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
public class WeekTemplateServiceImpl implements WeekTemplateService {

    private final WeekTemplateRepository weekTemplateRepository;
    private final DayTemplateRepository dayTemplateRepository;
    private final TemplateDayItemRepository templateDayItemRepository;

    private final WeekTemplateMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<WeekTemplateViewDto> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WeekTemplate> templatesPage = weekTemplateRepository.findAllByUser_Id(userId, pageable);
        Map<Long, List<TemplateDayItem>> itemsByTemplateId = loadItemsByWeekTemplateId(templatesPage.getContent());

        List<WeekTemplateViewDto> content = templatesPage.getContent().stream()
                .map(template -> toView(template, itemsByTemplateId.getOrDefault(template.getId(), List.of())))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, templatesPage.getTotalElements());
    }

    @Override
    public WeekTemplateViewDto create(Long userId, WeekTemplateCreateDto dto) {
        String name = normalizeName(dto.getName());
        if (weekTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
            throw new BadRequestException("Week template name already exists");
        }

        WeekTemplate tpl = WeekTemplate.builder()
                .user(refUser(userId))
                .name(name)
                .build();

        applyItemsReplace(userId, tpl, dto.getItems());

        WeekTemplate saved = weekTemplateRepository.save(tpl);
        return mapper.toView(saved);
    }

    @Override
    public WeekTemplateViewDto update(Long userId, Long id, WeekTemplateUpdateDto dto) {
        WeekTemplate tpl = weekTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));

        if (dto.getName() != null) {
            String name = normalizeName(dto.getName());
            if (!name.equalsIgnoreCase(tpl.getName())
                    && weekTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
                throw new BadRequestException("Week template name already exists");
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
                loadItemsByWeekTemplateId(List.of(tpl)).getOrDefault(tpl.getId(), List.of())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WeekTemplateViewDto get(Long userId, Long id) {
        WeekTemplate tpl = weekTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));
        return toView(
                tpl,
                loadItemsByWeekTemplateId(List.of(tpl)).getOrDefault(tpl.getId(), List.of())
        );
    }

    @Override
    public void delete(Long userId, Long id) {
        WeekTemplate tpl = weekTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));
        weekTemplateRepository.delete(tpl);
    }

    private void applyItemsReplace(Long userId, WeekTemplate tpl, List<WeekTemplateDayItemDto> items) {
        tpl.getItems().clear();
        if (items == null || items.isEmpty()) return;

        // max 7, dayOfWeek 1..7, уникальные dayOfWeek
        if (items.size() > 7) throw new BadRequestException("Week template can't have more than 7 days");

        Set<Integer> dowSet = new HashSet<>();
        for (var it : items) {
            if (it.getDayTemplateId() == null) throw new BadRequestException("dayTemplateId is required");
            if (it.getDayOfWeek() == null || it.getDayOfWeek() < 1 || it.getDayOfWeek() > 7) {
                throw new BadRequestException("dayOfWeek must be 1..7");
            }
            if (!dowSet.add(it.getDayOfWeek())) throw new BadRequestException("duplicate dayOfWeek in week template");
        }

        List<Long> ids = items.stream().map(WeekTemplateDayItemDto::getDayTemplateId).toList();
        Map<Long, DayTemplate> byId = dayTemplateRepository.findAllById(ids).stream()
                .filter(d -> d.getUser().getId().equals(userId))
                .collect(Collectors.toMap(BaseEntity::getId, x -> x));

        for (var it : items) {
            DayTemplate dayTpl = byId.get(it.getDayTemplateId());
            if (dayTpl == null) throw new BadRequestException("Day template not found: " + it.getDayTemplateId());

            TemplateDayItem w = TemplateDayItem.builder()
                    .weekTemplate(tpl)
                    .dayTemplate(dayTpl)
                    .dayOfWeek(it.getDayOfWeek())
                    .build();

            tpl.getItems().add(w);
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

    private Map<Long, List<TemplateDayItem>> loadItemsByWeekTemplateId(List<WeekTemplate> templates) {
        List<Long> templateIds = templates.stream()
                .map(BaseEntity::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (templateIds.isEmpty()) {
            return Map.of();
        }

        return templateDayItemRepository.findAllByWeekTemplateIdInWithDayTemplate(templateIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getWeekTemplate().getId(),
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private WeekTemplateViewDto toView(WeekTemplate template, List<TemplateDayItem> items) {
        WeekTemplateViewDto dto = new WeekTemplateViewDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setItems(items.stream().map(mapper::toViewItem).toList());
        return dto;
    }
}

