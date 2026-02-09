package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.TemplateType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.TemplateEntryItemRepository;
import com.example.activity_diary.repository.template.TemplateGoalMetricRepository;
import com.example.activity_diary.repository.template.TemplateGoalTagRepository;
import com.example.activity_diary.repository.template.TemplateRepository;
import com.example.activity_diary.service.diary.TemplateGoalsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateGoalsServiceImpl implements TemplateGoalsService {

    private final TemplateRepository templateRepository;
    private final TemplateEntryItemRepository templateEntryItemRepository;
    private final TemplateGoalTagRepository goalTagRepository;
    private final TemplateGoalMetricRepository goalMetricRepository;

    /** Внутренний ключ для агрегации метрик: (metricTypeId, unitId) */
    private record MetricKey(Long metricTypeId, Long unitId) {}

    @Override
    public void recalcGoals(Long userId, Long templateId) {
        Template t = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        // 1) Определяем dayIds, откуда тянем entryTemplates
        List<Long> dayIds;
        if (t.getType() == TemplateType.DAY) {
            dayIds = List.of(t.getId());
        } else if (t.getType() == TemplateType.WEEK) {
            Template week = templateRepository.findWeekWithDays(templateId, userId)
                    .orElseThrow(() -> new NotFoundException("Week template graph not found"));

            dayIds = week.getWeekItems().stream()
                    .map(TemplateDayItem::getDayTemplate)
                    .map(Template::getId)
                    .distinct()
                    .toList();
        } else {
            throw new BadRequestException("Unknown template type");
        }

        // 2) Тянем граф dayItems -> entryTemplate(tags, metrics, values, dict refs)
        List<TemplateEntryItem> dayItemsGraph = dayIds.isEmpty()
                ? List.of()
                : templateEntryItemRepository.findDayItemsGraph(dayIds);

        // 3) Агрегация + сбор ссылок (чтобы не делать find* O(n^2))
        Map<Long, Integer> tagCount = new HashMap<>();
        Map<MetricKey, Integer> metricSum = new HashMap<>();

        Map<Long, Tag> tagRefById = new HashMap<>();
        Map<Long, DictionaryItem> dictRefById = new HashMap<>();

        for (TemplateEntryItem di : dayItemsGraph) {
            DiaryEntryTemplate et = di.getEntryTemplate();
            if (et == null) continue;

            // tags: +1 за появление тега в entryTemplate item
            if (et.getTags() != null) {
                for (Tag tag : et.getTags()) {
                    Long tagId = tag.getId();
                    if (tagId == null) continue;
                    tagRefById.putIfAbsent(tagId, tag);
                    tagCount.merge(tagId, 1, Integer::sum);
                }
            }

            // metrics: sum по (typeId, unitId)
            if (et.getMetrics() != null) {
                for (EntryTemplateMetric m : et.getMetrics()) {
                    DictionaryItem metricType = m.getMetricType();
                    Long typeId = (metricType == null) ? null : metricType.getId();
                    if (typeId == null) continue;

                    dictRefById.putIfAbsent(typeId, metricType);

                    if (m.getValues() == null) continue;
                    for (EntryTemplateMetricValue mv : m.getValues()) {
                        DictionaryItem unit = mv.getUnit();
                        Long unitId = (unit == null) ? null : unit.getId();
                        if (unitId == null) continue;

                        dictRefById.putIfAbsent(unitId, unit);

                        Integer value = mv.getValue();
                        if (value == null) continue;

                        metricSum.merge(new MetricKey(typeId, unitId), value, Integer::sum);
                    }
                }
            }
        }

        // 4) Перезапись goals (delete -> insert)
        goalTagRepository.deleteByTemplateId(templateId);
        goalMetricRepository.deleteByTemplateId(templateId);

        // 5) Save tag goals
        if (!tagCount.isEmpty()) {
            List<TemplateGoalTag> tagGoals = new ArrayList<>(tagCount.size());
            for (var e : tagCount.entrySet()) {
                Long tagId = e.getKey();
                Integer count = e.getValue();

                Tag tagRef = tagRefById.get(tagId);
                if (tagRef == null) continue;

                tagGoals.add(
                        TemplateGoalTag.builder()
                                .id(new TemplateGoalTagId(templateId, tagId))
                                .template(t)
                                .tag(tagRef)
                                .usageCount(count)
                                .build()
                );
            }
            goalTagRepository.saveAll(tagGoals);
        }

        // 6) Save metric goals
        if (!metricSum.isEmpty()) {
            List<TemplateGoalMetric> metricGoals = new ArrayList<>(metricSum.size());
            for (var e : metricSum.entrySet()) {
                MetricKey k = e.getKey();
                Integer sum = e.getValue();

                DictionaryItem typeRef = dictRefById.get(k.metricTypeId());
                DictionaryItem unitRef = dictRefById.get(k.unitId());
                if (typeRef == null || unitRef == null) continue;

                metricGoals.add(
                        TemplateGoalMetric.builder()
                                .id(new TemplateGoalMetricId(templateId, k.metricTypeId(), k.unitId()))
                                .template(t)
                                .metricType(typeRef)
                                .unit(unitRef)
                                .sumValue(sum)
                                .build()
                );
            }
            goalMetricRepository.saveAll(metricGoals);
        }
    }
}
