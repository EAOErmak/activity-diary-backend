package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.TemplateType;
import com.example.activity_diary.repository.*;
import com.example.activity_diary.service.diary.TemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateEntryItemRepository templateEntryItemRepository;
    private final TemplateDayItemRepository templateDayItemRepository;

    private final TemplateGoalTagRepository goalTagRepository;
    private final TemplateGoalMetricRepository goalMetricRepository;

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    /** Внутренний ключ для агрегации метрик: (metricTypeId, unitId) */
    private record MetricKey(Long metricTypeId, Long unitId) {}

    // ------------------------------------------------------------------
    // 1) CREATE DAY TEMPLATE
    // ------------------------------------------------------------------
    @Transactional
    public Template createDayTemplate(User user, String name, List<Long> entryTemplateIds) {
        Objects.requireNonNull(user, "user is required");
        name = normalizeName(name);

        // 1) Загружаем entry-шаблоны пачкой и проверяем что все принадлежат user
        List<DiaryEntryTemplate> entryTemplates = loadEntryTemplatesForUser(user.getId(), entryTemplateIds);

        // 2) Создаём Template(DAY)
        Template day = Template.builder()
                .user(user)
                .type(TemplateType.DAY)
                .name(name)
                .build();

        // 3) Создаём items с позициями
        int pos = 1;
        for (DiaryEntryTemplate et : entryTemplates) {
            TemplateEntryItem item = new TemplateEntryItem();
            item.setTemplate(day);
            item.setEntryTemplate(et);
            item.setPosition(pos++);
            day.getDayItems().add(item);
        }

        // 4) Сохраняем day (каскад сохранит dayItems)
        Template saved = templateRepository.save(day);

        // 5) Пересчитываем goals (для DAY)
        recalcGoals(saved.getId(), user.getId());

        return saved;
    }

    // ------------------------------------------------------------------
    // 2) CREATE WEEK TEMPLATE
    // ------------------------------------------------------------------
    @Transactional
    public Template createWeekTemplate(User user, String name, List<Long> dayTemplateIds) {
        Objects.requireNonNull(user, "user is required");
        name = normalizeName(name);

        // 1) Загружаем day-templates пачкой, проверяем что они DAY и принадлежат user
        List<Template> dayTemplates = loadDayTemplatesForUser(user.getId(), dayTemplateIds);

        // 2) Создаём Template(WEEK)
        Template week = Template.builder()
                .user(user)
                .type(TemplateType.WEEK)
                .name(name)
                .build();

        // 3) Создаём weekItems с позициями (1..7 или сколько передали)
        int pos = 1;
        for (Template day : dayTemplates) {
            TemplateDayItem wi = new TemplateDayItem();
            wi.setTemplate(week);
            wi.setDayTemplate(day);
            wi.setPosition(pos++);
            week.getWeekItems().add(wi);
        }

        Template saved = templateRepository.save(week);

        // 4) Пересчёт целей для недели
        recalcGoals(saved.getId(), user.getId());

        return saved;
    }

    // ------------------------------------------------------------------
    // 3) UPDATE ITEMS (DAY)
    // ------------------------------------------------------------------
    @Transactional
    public void updateDayTemplateItems(Long templateId, Long userId, List<Long> entryTemplateIds) {
        Template day = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NoSuchElementException("Day template not found"));

        if (day.getType() != TemplateType.DAY) {
            throw new IllegalStateException("Template is not DAY");
        }

        List<DiaryEntryTemplate> entryTemplates = loadEntryTemplatesForUser(userId, entryTemplateIds);

        // Очистка коллекции dayItems -> orphanRemoval=true удалит старые строки
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

        recalcGoals(templateId, userId);
    }

    // ------------------------------------------------------------------
    // 4) UPDATE ITEMS (WEEK)
    // ------------------------------------------------------------------
    @Transactional
    public void updateWeekTemplateItems(Long templateId, Long userId, List<Long> dayTemplateIds) {
        Template week = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NoSuchElementException("Week template not found"));

        if (week.getType() != TemplateType.WEEK) {
            throw new IllegalStateException("Template is not WEEK");
        }

        List<Template> dayTemplates = loadDayTemplatesForUser(userId, dayTemplateIds);

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

        recalcGoals(templateId, userId);
    }

    // ------------------------------------------------------------------
    // 5) UNIVERSAL GOALS RECALC (DAY or WEEK)
    // ------------------------------------------------------------------
    @Transactional
    public void recalcGoals(Long templateId, Long userId) {
        Template t = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NoSuchElementException("Template not found"));

        // 1) Собираем список dayIds, из которых будем вытягивать entryTemplates
        List<Long> dayIds;

        if (t.getType() == TemplateType.DAY) {
            // Для DAY goals считаются по самому себе
            dayIds = List.of(t.getId());
        } else if (t.getType() == TemplateType.WEEK) {
            // Для WEEK goals считаются по всем dayTemplate внутри weekItems
            Template week = templateRepository.findWeekWithDays(templateId, userId)
                    .orElseThrow(() -> new NoSuchElementException("Week template graph not found"));

            dayIds = week.getWeekItems().stream()
                    .map(TemplateDayItem::getDayTemplate)
                    .map(Template::getId)
                    .distinct()
                    .toList();
        } else {
            throw new IllegalStateException("Unknown template type");
        }

        // 2) Вытягиваем полный граф dayItems -> entryTemplate(tags, metrics, values)
        //    (если DAY — тут будет один dayId = templateId)
        List<TemplateEntryItem> dayItemsGraph = dayIds.isEmpty()
                ? List.of()
                : templateEntryItemRepository.findDayItemsGraph(dayIds);

        // 3) Агрегация
        Map<Long, Integer> tagCount = new HashMap<>();
        Map<MetricKey, Integer> metricSum = new HashMap<>();

        for (TemplateEntryItem di : dayItemsGraph) {
            DiaryEntryTemplate et = di.getEntryTemplate();

            // 3.1 Теги: считаем появление тега в каждом entryTemplate как +1
            if (et.getTags() != null) {
                for (Tag tag : et.getTags()) {
                    if (tag.getId() == null) continue;
                    tagCount.merge(tag.getId(), 1, Integer::sum);
                }
            }

            // 3.2 Метрики: суммируем значения по ключу (metricTypeId, unitId)
            if (et.getMetrics() != null) {
                for (EntryTemplateMetric m : et.getMetrics()) {
                    DictionaryItem metricType = m.getMetricType();
                    Long typeId = metricType != null ? metricType.getId() : null;
                    if (typeId == null) continue;

                    if (m.getValues() == null) continue;
                    for (EntryTemplateMetricValue mv : m.getValues()) {
                        DictionaryItem unit = mv.getUnit();
                        Long unitId = unit != null ? unit.getId() : null;
                        if (unitId == null) continue;

                        Integer value = mv.getValue();
                        if (value == null) continue;

                        metricSum.merge(new MetricKey(typeId, unitId), value, Integer::sum);
                    }
                }
            }
        }

        // 4) Перезаписываем goals (delete -> insert)
        goalTagRepository.deleteByTemplateId(templateId);
        goalMetricRepository.deleteByTemplateId(templateId);

        // 5) Сбор сущностей goals и saveAll
        List<TemplateGoalTag> tagGoals = new ArrayList<>(tagCount.size());
        for (Map.Entry<Long, Integer> e : tagCount.entrySet()) {
            Long tagId = e.getKey();
            Integer count = e.getValue();

            // важно: Tag берём из уже загруженного графа (оно managed), но проще создать ссылку через существующий Tag объект:
            // здесь мы можем найти Tag объект в dayItemsGraph, чтобы не делать отдельный запрос.
            Tag tagRef = findTagRef(dayItemsGraph, tagId);
            if (tagRef == null) continue;

            TemplateGoalTag g = TemplateGoalTag.builder()
                    .id(new TemplateGoalTagId(templateId, tagId))
                    .template(t)
                    .tag(tagRef)
                    .usageCount(count)
                    .build();
            tagGoals.add(g);
        }
        goalTagRepository.saveAll(tagGoals);

        List<TemplateGoalMetric> metricGoals = new ArrayList<>(metricSum.size());
        for (Map.Entry<MetricKey, Integer> e : metricSum.entrySet()) {
            MetricKey k = e.getKey();
            Integer sum = e.getValue();

            DictionaryItem typeRef = findMetricTypeRef(dayItemsGraph, k.metricTypeId);
            DictionaryItem unitRef = findUnitRef(dayItemsGraph, k.unitId);
            if (typeRef == null || unitRef == null) continue;

            TemplateGoalMetric g = TemplateGoalMetric.builder()
                    .id(new TemplateGoalMetricId(templateId, k.metricTypeId, k.unitId))
                    .template(t)
                    .metricType(typeRef)
                    .unit(unitRef)
                    .sumValue(sum)
                    .build();
            metricGoals.add(g);
        }
        goalMetricRepository.saveAll(metricGoals);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String normalizeName(String name) {
        if (name == null) throw new IllegalArgumentException("name is required");
        String n = name.trim();
        if (n.isEmpty()) throw new IllegalArgumentException("name is blank");
        if (n.length() > 120) throw new IllegalArgumentException("name too long");
        return n;
    }

    private List<DiaryEntryTemplate> loadEntryTemplatesForUser(Long userId, List<Long> ids) {
        if (ids == null) ids = List.of();
        // убираем null/дубликаты, сохраняя порядок
        List<Long> cleaned = ids.stream().filter(Objects::nonNull).distinct().toList();

        List<DiaryEntryTemplate> loaded = cleaned.isEmpty()
                ? List.of()
                : diaryEntryTemplateRepository.findAllByIdInAndUserId(cleaned, userId);

        if (loaded.size() != cleaned.size()) {
            throw new IllegalArgumentException("Some entry templates not found or not owned by user");
        }

        // восстановить исходный порядок по id
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
            throw new IllegalArgumentException("Some day templates not found or not owned by user");
        }

        Map<Long, Template> map = loaded.stream().collect(Collectors.toMap(Template::getId, x -> x));
        List<Template> ordered = new ArrayList<>(cleaned.size());
        for (Long id : cleaned) ordered.add(map.get(id));
        return ordered;
    }

    private Tag findTagRef(List<TemplateEntryItem> items, Long tagId) {
        for (TemplateEntryItem di : items) {
            DiaryEntryTemplate et = di.getEntryTemplate();
            if (et.getTags() == null) continue;
            for (Tag t : et.getTags()) {
                if (tagId.equals(t.getId())) return t;
            }
        }
        return null;
    }

    private DictionaryItem findMetricTypeRef(List<TemplateEntryItem> items, Long typeId) {
        for (TemplateEntryItem di : items) {
            DiaryEntryTemplate et = di.getEntryTemplate();
            if (et.getMetrics() == null) continue;
            for (EntryTemplateMetric m : et.getMetrics()) {
                DictionaryItem mt = m.getMetricType();
                if (mt != null && typeId.equals(mt.getId())) return mt;
            }
        }
        return null;
    }

    private DictionaryItem findUnitRef(List<TemplateEntryItem> items, Long unitId) {
        for (TemplateEntryItem di : items) {
            DiaryEntryTemplate et = di.getEntryTemplate();
            if (et.getMetrics() == null) continue;
            for (EntryTemplateMetric m : et.getMetrics()) {
                if (m.getValues() == null) continue;
                for (EntryTemplateMetricValue v : m.getValues()) {
                    DictionaryItem u = v.getUnit();
                    if (u != null && unitId.equals(u.getId())) return u;
                }
            }
        }
        return null;
    }
}
