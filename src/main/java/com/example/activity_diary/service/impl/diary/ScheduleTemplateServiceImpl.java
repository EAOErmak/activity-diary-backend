package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.dto.mapper.ScheduleTemplateMapper;
import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.enums.TemplateType;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.Template;
import com.example.activity_diary.entity.template.TemplateDayItem;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.TemplateRepository;
import com.example.activity_diary.service.diary.ScheduleTemplateService;
import com.example.activity_diary.service.diary.TemplateGoalsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleTemplateServiceImpl implements ScheduleTemplateService {

    private final TemplateRepository templateRepository;
    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final UserRepository userRepository;
    private final TemplateGoalsService templateGoalsService;

    private final ScheduleTemplateMapper mapper;

    @Override
    public TemplateViewDto createDayTemplate(Long userId, DayTemplateCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String name = normalizeName(dto.getName());

        List<DiaryEntryTemplate> entryTemplates =
                loadEntryTemplatesForUser(userId, dto.getEntryTemplateIds());

        Template day = Template.builder()
                .user(user)
                .type(TemplateType.DAY)
                .name(name)
                .build();

        int pos = 1;
        for (DiaryEntryTemplate et : entryTemplates) {
            TemplateEntryItem item = new TemplateEntryItem();
            item.setTemplate(day);
            item.setEntryTemplate(et);
            item.setPosition(pos++);
            day.getDayItems().add(item);
        }

        Template saved = templateRepository.save(day);
        templateGoalsService.recalcGoals(userId, saved.getId());

        // Возвращаем уже DTO с нормальной загрузкой связей
        return getTemplate(userId, saved.getId());
    }

    @Override
    public TemplateViewDto createWeekTemplate(Long userId, WeekTemplateCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String name = normalizeName(dto.getName());

        List<Template> dayTemplates =
                loadDayTemplatesForUser(userId, dto.getDayTemplateIds());

        Template week = Template.builder()
                .user(user)
                .type(TemplateType.WEEK)
                .name(name)
                .build();

        int pos = 1;
        for (Template day : dayTemplates) {
            TemplateDayItem wi = new TemplateDayItem();
            wi.setTemplate(week);
            wi.setDayTemplate(day);
            wi.setPosition(pos++);
            week.getWeekItems().add(wi);
        }

        Template saved = templateRepository.save(week);
        templateGoalsService.recalcGoals(userId, saved.getId());

        return getTemplate(userId, saved.getId());
    }

    @Override
    public void updateDayTemplateItems(Long userId, Long templateId, TemplateItemsUpdateDto dto) {
        Template day = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        if (day.getType() != TemplateType.DAY) {
            throw new BadRequestException("Template type must be DAY");
        }

        List<DiaryEntryTemplate> entryTemplates =
                loadEntryTemplatesForUser(userId, dto.getIds());

        day.getDayItems().clear();

        int pos = 1;
        for (DiaryEntryTemplate et : entryTemplates) {
            TemplateEntryItem item = new TemplateEntryItem();
            item.setTemplate(day);
            item.setEntryTemplate(et);
            item.setPosition(pos++);
            day.getDayItems().add(item);
        }

        templateRepository.save(day);
        templateGoalsService.recalcGoals(userId, templateId);
    }

    @Override
    public void updateWeekTemplateItems(Long userId, Long templateId, TemplateItemsUpdateDto dto) {
        Template week = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        if (week.getType() != TemplateType.WEEK) {
            throw new BadRequestException("Template type must be WEEK");
        }

        List<Template> dayTemplates =
                loadDayTemplatesForUser(userId, dto.getIds());

        week.getWeekItems().clear();

        int pos = 1;
        for (Template day : dayTemplates) {
            TemplateDayItem wi = new TemplateDayItem();
            wi.setTemplate(week);
            wi.setDayTemplate(day);
            wi.setPosition(pos++);
            week.getWeekItems().add(wi);
        }

        templateRepository.save(week);
        templateGoalsService.recalcGoals(userId, templateId);
    }

    @Override
    public TemplateViewDto getTemplate(Long userId, Long templateId) {
        // 1) Узнаём тип без загрузки коллекций
        TemplateType type = templateRepository.findTypeByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        // 2) Грузим нужный граф (DAY или WEEK)
        Template t = (type == TemplateType.DAY)
                ? templateRepository.findDayViewByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"))
                : templateRepository.findWeekViewByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        // 3) Маппинг
        TemplateViewDto dto = mapper.toViewDto(t);

        if (type == TemplateType.DAY) {
            dto.setDayItems(t.getDayItems().stream().map(mapper::toDto).toList());
            dto.setWeekItems(List.of());
        } else {
            dto.setWeekItems(t.getWeekItems().stream().map(mapper::toDto).toList());
            dto.setDayItems(List.of());
        }

        // goals лучше грузить отдельными запросами (не раздувать граф)
        dto.setGoalTags(templateRepository.findGoalTagsByTemplateId(templateId, userId)
                .stream().map(mapper::toDto).toList());
        dto.setGoalMetrics(templateRepository.findGoalMetricsByTemplateId(templateId, userId)
                .stream().map(mapper::toDto).toList());

        return dto;
    }

    @Override
    public Page<TemplateListItemDto> listTemplates(Long userId, Pageable pageable) {
        Page<Template> page = templateRepository.findAllByUserId(userId, pageable);

        List<Long> ids = page.getContent().stream().map(Template::getId).toList();

        // bulk counts (один запрос на dayItems, один на weekItems, goals tags, goals metrics)
        Map<Long, Integer> dayCounts = templateRepository.countDayItems(ids);
        Map<Long, Integer> weekCounts = templateRepository.countWeekItems(ids);
        Map<Long, Integer> goalTagCounts = templateRepository.countGoalTags(ids);
        Map<Long, Integer> goalMetricCounts = templateRepository.countGoalMetrics(ids);

        return page.map(t -> {
            TemplateListItemDto dto = new TemplateListItemDto();
            dto.setId(t.getId());
            dto.setType(t.getType());
            dto.setName(t.getName());
            dto.setUpdatedAt(t.getUpdatedAt());

            dto.setDayItemsCount(dayCounts.getOrDefault(t.getId(), 0));
            dto.setWeekItemsCount(weekCounts.getOrDefault(t.getId(), 0));
            dto.setGoalsTagsCount(goalTagCounts.getOrDefault(t.getId(), 0));
            dto.setGoalsMetricsCount(goalMetricCounts.getOrDefault(t.getId(), 0));
            return dto;
        });
    }

    @Override
    public void deleteTemplate(Long userId, Long templateId) {
        Template t = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));
        templateRepository.delete(t);
    }

    // ----------------- helpers -----------------

    private String normalizeName(String name) {
        if (name == null) throw new BadRequestException("name is required");
        String n = name.trim();
        if (n.isEmpty()) throw new BadRequestException("name is blank");
        if (n.length() > 120) throw new BadRequestException("name too long");
        return n;
    }

    private List<DiaryEntryTemplate> loadEntryTemplatesForUser(Long userId, List<Long> ids) {
        if (ids == null) ids = List.of();

        List<Long> cleaned = ids.stream().filter(Objects::nonNull).distinct().toList();

        List<DiaryEntryTemplate> loaded = cleaned.isEmpty()
                ? List.of()
                : diaryEntryTemplateRepository.findAllByIdInAndUserId(cleaned, userId);

        if (loaded.size() != cleaned.size()) {
            throw new BadRequestException("Some entry templates not found or not owned by user");
        }

        Map<Long, DiaryEntryTemplate> map = loaded.stream()
                .collect(Collectors.toMap(DiaryEntryTemplate::getId, x -> x));

        List<DiaryEntryTemplate> ordered = new ArrayList<>(cleaned.size());
        for (Long id : cleaned) ordered.add(map.get(id));
        return ordered;
    }

    private List<Template> loadDayTemplatesForUser(Long userId, List<Long> ids) {
        if (ids == null) ids = List.of();

        List<Long> cleaned = ids.stream().filter(Objects::nonNull).distinct().toList();

        List<Template> loaded = cleaned.isEmpty()
                ? List.of()
                : templateRepository.findAllByIdInAndUserIdAndType(cleaned, userId, TemplateType.DAY);

        if (loaded.size() != cleaned.size()) {
            throw new BadRequestException("Some day templates not found or not owned by user");
        }

        Map<Long, Template> map = loaded.stream()
                .collect(Collectors.toMap(Template::getId, x -> x));

        List<Template> ordered = new ArrayList<>(cleaned.size());
        for (Long id : cleaned) ordered.add(map.get(id));
        return ordered;
    }
}
