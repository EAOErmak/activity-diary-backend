package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
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
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCalendarCreateService {

    private static final int DEFAULT_TEMPLATE_DURATION_MIN = 60;
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);
    private static final String DUPLICATE_METRIC_GOAL_UNIT_MESSAGE =
            "Metric goal cannot contain duplicate values with the same unit";

    private final EntityManager entityManager;

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final DayTemplateRepository dayTemplateRepository;
    private final WeekTemplateRepository weekTemplateRepository;

    private final WeekGoalRepository weekGoalRepository;
    private final DayGoalRepository dayGoalRepository;
    private final DiaryEntryGoalRepository diaryEntryGoalRepository;

    private final GoalMapper goalMapper;

    public DiaryEntryGoalDetailDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate) {
        User userRef = entityManager.getReference(User.class, userId);

        DiaryEntryTemplate template = diaryEntryTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Entry template not found"));

        WeekGoal week = findOrCreateWeekGoal(userId, userRef, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        GoalSchedule schedule = resolveGoalSchedule(template, targetDate, true);

        DiaryEntryGoal created = createEntryGoalFromTemplate(userRef, day, template, schedule);
        diaryEntryGoalRepository.save(created);

        return goalMapper.toEntryView(created);
    }

    public DayGoalDetailDto createDayGoal(Long userId, Long templateId, LocalDate targetDate) {
        User userRef = entityManager.getReference(User.class, userId);

        DayTemplate template = dayTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));

        WeekGoal week = findOrCreateWeekGoal(userId, userRef, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        for (TemplateEntryItem item : template.getItems()) {
            DiaryEntryTemplate entryTemplate = item.getEntryTemplate();
            GoalSchedule schedule = resolveGoalSchedule(entryTemplate, targetDate, false);

            DiaryEntryGoal created = createEntryGoalFromTemplate(
                    userRef,
                    day,
                    entryTemplate,
                    schedule
            );
            diaryEntryGoalRepository.save(created);
        }

        return goalMapper.toDayView(day);
    }

    public WeekGoalDetailDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate) {
        User userRef = entityManager.getReference(User.class, userId);

        WeekTemplate template = weekTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));

        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
        WeekGoal week = findOrCreateWeekGoal(userId, userRef, targetDate);

        for (TemplateDayItem dayItem : template.getItems()) {
            LocalDate date = monday.plusDays(dayItem.getDayOfWeek() - 1L);
            DayGoal day = findOrCreateDayGoal(week, date);

            DayTemplate dayTemplate = dayItem.getDayTemplate();
            for (TemplateEntryItem entryItem : dayTemplate.getItems()) {
                DiaryEntryTemplate entryTemplate = entryItem.getEntryTemplate();
                GoalSchedule schedule = resolveGoalSchedule(entryTemplate, date, false);

                DiaryEntryGoal created = createEntryGoalFromTemplate(
                        userRef,
                        day,
                        entryTemplate,
                        schedule
                );
                diaryEntryGoalRepository.save(created);
            }
        }

        return goalMapper.toWeekView(week);
    }

    private WeekGoal findOrCreateWeekGoal(Long userId, User userRef, LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Instant start = monday.atStartOfDay(zone).toInstant();
        Instant end = sunday.atTime(23, 59, 59).atZone(zone).toInstant();

        return weekGoalRepository.findByUser_IdAndWhenStarted(userId, start)
                .orElseGet(() -> weekGoalRepository.save(
                        WeekGoal.builder()
                                .user(userRef)
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
            GoalSchedule schedule
    ) {
        int position = day.getEntryGoals().stream()
                .map(DiaryEntryGoal::getPosition)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(user)
                .dayGoal(day)
                .position(position)
                .whenStarted(schedule.whenStarted())
                .whenEnded(schedule.whenEnded())
                .expectedDurationMin(schedule.expectedDurationMin())
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
        for (EntryTemplateMetric templateMetric : template.getMetrics()) {
            validateMetricGoalValues(templateMetric);

            EntryMetricGoal metricGoal = EntryMetricGoal.create(goal, templateMetric.getMetricType());

            for (EntryTemplateMetricValue templateValue : templateMetric.getValues()) {
                metricGoal.addValue(templateValue.getUnit(), templateValue.getValue());
            }

            goal.addMetricGoal(metricGoal);
        }
    }

    private void validateMetricGoalValues(EntryTemplateMetric templateMetric) {
        Set<Long> unitIds = new HashSet<>();

        for (EntryTemplateMetricValue templateValue : templateMetric.getValues()) {
            Long unitId = templateValue.getUnit() == null ? null : templateValue.getUnit().getId();
            if (!unitIds.add(unitId)) {
                throw new BadRequestException(DUPLICATE_METRIC_GOAL_UNIT_MESSAGE);
            }
        }
    }

    private GoalSchedule resolveGoalSchedule(DiaryEntryTemplate template, LocalDate date, boolean requireTemplateTime) {
        LocalTime start = template.getTimeStart();
        LocalTime end = template.getTimeEnd();

        if (start == null && end == null) {
            if (requireTemplateTime) {
                throw new BadRequestException("Template timeStart and timeEnd must be set");
            }
            return defaultGoalSchedule(date);
        }

        if (start == null || end == null) {
            throw new BadRequestException("Template timeStart and timeEnd must both be set");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("Template timeEnd must be after timeStart");
        }

        ZoneId zone = ZoneId.systemDefault();
        Instant whenStarted = date.atTime(start).atZone(zone).toInstant();
        Instant whenEnded = date.atTime(end).atZone(zone).toInstant();

        return new GoalSchedule(
                whenStarted,
                whenEnded,
                (int) Duration.between(start, end).toMinutes()
        );
    }

    private GoalSchedule defaultGoalSchedule(LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();
        return new GoalSchedule(
                date.atStartOfDay(zone).toInstant(),
                date.atTime(END_OF_DAY).atZone(zone).toInstant(),
                DEFAULT_TEMPLATE_DURATION_MIN
        );
    }

    private record GoalSchedule(
            Instant whenStarted,
            Instant whenEnded,
            int expectedDurationMin
    ) {
    }
}
