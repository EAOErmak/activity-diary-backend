package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.EntryMetricValueGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.EntryTemplateMetricValue;
import com.example.activity_diary.entity.template.TemplateDayItem;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import com.example.activity_diary.entity.template.WeekTemplate;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCalendarCreateService {

    private static final int DEFAULT_TEMPLATE_DURATION_MIN = 60;

    private final UserRepository userRepository;

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final DayTemplateRepository dayTemplateRepository;
    private final WeekTemplateRepository weekTemplateRepository;

    private final WeekGoalRepository weekGoalRepository;
    private final DayGoalRepository dayGoalRepository;
    private final DiaryEntryGoalRepository diaryEntryGoalRepository;

    private final GoalMapper goalMapper;

    public DiaryEntryGoalDetailDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DiaryEntryTemplate template = diaryEntryTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Entry template not found"));

        WeekGoal week = findOrCreateWeekGoal(user, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        int expectedDurationMin = calculateDurationMinutes(template);

        if (alreadyHasSnapshot(day, template, expectedDurationMin)) {
            DiaryEntryGoal existing = day.getEntryGoals().stream()
                    .filter(goal -> sameSnapshot(goal, template, expectedDurationMin))
                    .findFirst()
                    .orElseThrow();
            return goalMapper.toEntryView(existing);
        }

        DiaryEntryGoal created = createEntryGoalFromTemplate(user, day, template, targetDate, expectedDurationMin);
        diaryEntryGoalRepository.save(created);

        return goalMapper.toEntryView(created);
    }

    public DayGoalDetailDto createDayGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DayTemplate template = dayTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));

        WeekGoal week = findOrCreateWeekGoal(user, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        for (TemplateEntryItem item : template.getItems()) {
            DiaryEntryTemplate entryTemplate = item.getEntryTemplate();

            if (alreadyHasSnapshot(day, entryTemplate, DEFAULT_TEMPLATE_DURATION_MIN)) {
                continue;
            }

            DiaryEntryGoal created = createEntryGoalFromTemplate(
                    user,
                    day,
                    entryTemplate,
                    targetDate,
                    DEFAULT_TEMPLATE_DURATION_MIN
            );
            diaryEntryGoalRepository.save(created);
        }

        return goalMapper.toDayView(day);
    }

    public WeekGoalDetailDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        WeekTemplate template = weekTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));

        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
        WeekGoal week = findOrCreateWeekGoal(user, targetDate);

        for (TemplateDayItem dayItem : template.getItems()) {
            LocalDate date = monday.plusDays(dayItem.getDayOfWeek() - 1L);
            DayGoal day = findOrCreateDayGoal(week, date);

            DayTemplate dayTemplate = dayItem.getDayTemplate();
            for (TemplateEntryItem entryItem : dayTemplate.getItems()) {
                DiaryEntryTemplate entryTemplate = entryItem.getEntryTemplate();

                if (alreadyHasSnapshot(day, entryTemplate, DEFAULT_TEMPLATE_DURATION_MIN)) {
                    continue;
                }

                DiaryEntryGoal created = createEntryGoalFromTemplate(
                        user,
                        day,
                        entryTemplate,
                        date,
                        DEFAULT_TEMPLATE_DURATION_MIN
                );
                diaryEntryGoalRepository.save(created);
            }
        }

        return goalMapper.toWeekView(week);
    }

    private WeekGoal findOrCreateWeekGoal(User user, LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Instant start = monday.atStartOfDay(zone).toInstant();
        Instant end = sunday.atTime(23, 59, 59).atZone(zone).toInstant();

        return weekGoalRepository.findByUser_IdAndWhenStarted(user.getId(), start)
                .orElseGet(() -> weekGoalRepository.save(
                        WeekGoal.builder()
                                .user(user)
                                .whenStarted(start)
                                .whenEnded(end)
                                .completeness(0)
                                .build()
                ));
    }

    private DayGoal findOrCreateDayGoal(WeekGoal week, LocalDate date) {
        return dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), date)
                .orElseGet(() -> {
                    ZoneId zone = ZoneId.systemDefault();
                    Instant start = date.atStartOfDay(zone).toInstant();
                    Instant end = date.atTime(23, 59, 59).atZone(zone).toInstant();

                    DayGoal day = DayGoal.builder()
                            .weekGoal(week)
                            .dayIndex(date.getDayOfWeek().getValue())
                            .targetDate(date)
                            .whenStarted(start)
                            .whenEnded(end)
                            .completeness(0)
                            .build();

                    week.addDay(day);
                    return dayGoalRepository.save(day);
                });
    }

    private DiaryEntryGoal createEntryGoalFromTemplate(
            User user,
            DayGoal day,
            DiaryEntryTemplate template,
            LocalDate date,
            int expectedDurationMin
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.atTime(23, 59, 59).atZone(zone).toInstant();

        int position = day.getEntryGoals().stream()
                .map(DiaryEntryGoal::getPosition)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(user)
                .dayGoal(day)
                .position(position)
                .whenStarted(start)
                .whenEnded(end)
                .expectedDurationMin(expectedDurationMin)
                .name(template.getName())
                .mood(template.getMood())
                .description(template.getDescription())
                .completeness(0)
                .build();

        day.addEntryGoal(goal);
        copyMetricsFromTemplate(goal, template);

        return goal;
    }

    private void copyMetricsFromTemplate(DiaryEntryGoal goal, DiaryEntryTemplate template) {
        Map<Long, Integer> positionByMetricType = new HashMap<>();

        for (EntryTemplateMetric templateMetric : template.getMetrics()) {
            Long typeId = templateMetric.getMetricType().getId();

            int position = positionByMetricType.getOrDefault(typeId, 0) + 1;
            positionByMetricType.put(typeId, position);

            EntryMetricGoal metricGoal = EntryMetricGoal.create(goal, templateMetric.getMetricType(), position);

            for (EntryTemplateMetricValue templateValue : templateMetric.getValues()) {
                metricGoal.addValue(templateValue.getUnit(), templateValue.getValue());
            }

            goal.addMetricGoal(metricGoal);
        }
    }

    private boolean alreadyHasSnapshot(DayGoal day, DiaryEntryTemplate template, Integer expectedDurationMin) {
        for (DiaryEntryGoal goal : day.getEntryGoals()) {
            if (sameSnapshot(goal, template, expectedDurationMin)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameSnapshot(DiaryEntryGoal goal, DiaryEntryTemplate template, Integer expectedDurationMin) {
        if (!eq(goal.getName(), template.getName())) {
            return false;
        }
        if (!eq(goal.getMood(), template.getMood())) {
            return false;
        }
        if (!eq(norm(goal.getDescription()), norm(template.getDescription()))) {
            return false;
        }
        if (!eq(goal.getExpectedDurationMin(), expectedDurationMin)) {
            return false;
        }

        return goalMetricsMap(goal).equals(templateMetricsMap(template));
    }

    private Map<Long, Map<Long, Integer>> goalMetricsMap(DiaryEntryGoal goal) {
        Map<Long, Map<Long, Integer>> result = new HashMap<>();
        for (EntryMetricGoal metricGoal : goal.getMetricGoals()) {
            Long typeId = metricGoal.getMetricType().getId();
            Map<Long, Integer> units = new HashMap<>();
            for (EntryMetricValueGoal valueGoal : metricGoal.getValues()) {
                units.put(valueGoal.getUnit().getId(), valueGoal.getExpectedValue());
            }
            result.put(typeId, units);
        }
        return result;
    }

    private Map<Long, Map<Long, Integer>> templateMetricsMap(DiaryEntryTemplate template) {
        Map<Long, Map<Long, Integer>> result = new HashMap<>();
        for (EntryTemplateMetric metric : template.getMetrics()) {
            Long typeId = metric.getMetricType().getId();
            Map<Long, Integer> units = new HashMap<>();
            for (EntryTemplateMetricValue value : metric.getValues()) {
                units.put(value.getUnit().getId(), value.getValue());
            }
            result.put(typeId, units);
        }
        return result;
    }

    private int calculateDurationMinutes(DiaryEntryTemplate template) {

        if (template.getTimeStart() == null || template.getTimeEnd() == null) {
            throw new BadRequestException("Template timeStart and timeEnd must be set");
        }

        LocalTime start = template.getTimeStart();
        LocalTime end = template.getTimeEnd();

        if (!end.isAfter(start)) {
            throw new BadRequestException("Template timeEnd must be after timeStart");
        }

        return (int) Duration.between(start, end).toMinutes();
    }

    private static String norm(String value) {
        return value == null ? null : value.trim();
    }

    private static <T> boolean eq(T left, T right) {
        return Objects.equals(left, right);
    }
}
