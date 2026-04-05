package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalCalendarCreateServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    @Mock
    private DayTemplateRepository dayTemplateRepository;

    @Mock
    private WeekTemplateRepository weekTemplateRepository;

    @Mock
    private WeekGoalRepository weekGoalRepository;

    @Mock
    private DayGoalRepository dayGoalRepository;

    @Mock
    private DiaryEntryGoalRepository diaryEntryGoalRepository;

    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private GoalCalendarCreateService service;

    @Test
    void createEntryGoal_copiesAllTemplateMetricsWithoutMetricPositions() {
        Long userId = 10L;
        Long templateId = 20L;
        LocalDate targetDate = LocalDate.parse("2026-04-05");

        User user = user(userId);
        DiaryEntryTemplate template = template(user);
        addTemplateMetric(template, metricType(100L, "Buckwheat"), unit(200L, "grams"), 300);
        addTemplateMetric(template, metricType(101L, "Water"), unit(201L, "ml"), 500);

        WeekGoal week = weekGoal(user);
        DayGoal day = dayGoal(week, targetDate);
        DiaryEntryGoalDetailDto mapped = new DiaryEntryGoalDetailDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(diaryEntryTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(weekGoalRepository.findByUser_IdAndWhenStarted(eq(userId), any(Instant.class))).thenReturn(Optional.of(week));
        when(dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate)).thenReturn(Optional.of(day));
        when(diaryEntryGoalRepository.save(any(DiaryEntryGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goalMapper.toEntryView(any(DiaryEntryGoal.class))).thenReturn(mapped);

        DiaryEntryGoalDetailDto result = service.createEntryGoal(userId, templateId, targetDate);

        assertSame(mapped, result);

        ArgumentCaptor<DiaryEntryGoal> goalCaptor = ArgumentCaptor.forClass(DiaryEntryGoal.class);
        verify(diaryEntryGoalRepository).save(goalCaptor.capture());

        DiaryEntryGoal saved = goalCaptor.getValue();
        assertEquals(1, saved.getPosition());
        assertEquals(targetDate.atTime(8, 0).atZone(ZoneId.systemDefault()).toInstant(), saved.getWhenStarted());
        assertEquals(targetDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant(), saved.getWhenEnded());
        assertEquals(60, saved.getExpectedDurationMin());
        assertEquals(2, saved.getMetricGoals().size());

        List<Long> metricTypeIds = saved.getMetricGoals().stream()
                .map(EntryMetricGoal::getMetricType)
                .map(DictionaryItem::getId)
                .sorted(Comparator.naturalOrder())
                .toList();

        assertEquals(List.of(100L, 101L), metricTypeIds);
        assertTrue(saved.getMetricGoals().stream().allMatch(metricGoal -> metricGoal.getEntryGoal() == saved));
        assertTrue(saved.getMetricGoals().stream().allMatch(metricGoal -> metricGoal.getValues().size() == 1));
    }

    @Test
    void createDayGoal_usesEntryTemplateTimeWindowWhenTemplateProvidesTime() {
        Long userId = 10L;
        Long templateId = 21L;
        LocalDate targetDate = LocalDate.parse("2026-04-05");

        User user = user(userId);
        DiaryEntryTemplate entryTemplate = DiaryEntryTemplate.create(
                user,
                "Lunch",
                (short) 3,
                "Food goal",
                LocalTime.of(12, 15),
                LocalTime.of(13, 45)
        );
        addTemplateMetric(entryTemplate, metricType(102L, "Soup"), unit(202L, "grams"), 450);

        DayTemplate dayTemplate = DayTemplate.builder()
                .user(user)
                .name("Sunday")
                .build();
        dayTemplate.setItems(List.of(
                TemplateEntryItem.builder()
                        .dayTemplate(dayTemplate)
                        .entryTemplate(entryTemplate)
                        .position(1)
                        .build()
        ));

        WeekGoal week = weekGoal(user);
        DayGoal day = dayGoal(week, targetDate);
        DayGoalDetailDto mapped = new DayGoalDetailDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(dayTemplateRepository.findById(templateId)).thenReturn(Optional.of(dayTemplate));
        when(weekGoalRepository.findByUser_IdAndWhenStarted(eq(userId), any(Instant.class))).thenReturn(Optional.of(week));
        when(dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate)).thenReturn(Optional.of(day));
        when(diaryEntryGoalRepository.save(any(DiaryEntryGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goalMapper.toDayView(day)).thenReturn(mapped);

        DayGoalDetailDto result = service.createDayGoal(userId, templateId, targetDate);

        assertSame(mapped, result);

        ArgumentCaptor<DiaryEntryGoal> goalCaptor = ArgumentCaptor.forClass(DiaryEntryGoal.class);
        verify(diaryEntryGoalRepository).save(goalCaptor.capture());

        DiaryEntryGoal saved = goalCaptor.getValue();
        assertEquals(targetDate.atTime(12, 15).atZone(ZoneId.systemDefault()).toInstant(), saved.getWhenStarted());
        assertEquals(targetDate.atTime(13, 45).atZone(ZoneId.systemDefault()).toInstant(), saved.getWhenEnded());
        assertEquals(90, saved.getExpectedDurationMin());
    }

    private static User user(Long id) {
        User user = User.builder()
                .username("user")
                .build();
        user.setId(id);
        return user;
    }

    private static DiaryEntryTemplate template(User user) {
        return DiaryEntryTemplate.create(
                user,
                "Breakfast",
                (short) 4,
                "Food goal",
                LocalTime.of(8, 0),
                LocalTime.of(9, 0)
        );
    }

    private static void addTemplateMetric(
            DiaryEntryTemplate template,
            DictionaryItem metricType,
            DictionaryItem unit,
            int value
    ) {
        EntryTemplateMetric metric = EntryTemplateMetric.create(template, metricType);
        metric.addValue(unit, value);
        template.addMetric(metric);
    }

    private static DictionaryItem metricType(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label(label)
                .build();
        item.setId(id);
        return item;
    }

    private static DictionaryItem unit(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label(label)
                .build();
        item.setId(id);
        return item;
    }

    private static WeekGoal weekGoal(User user) {
        WeekGoal week = WeekGoal.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-03-30T00:00:00Z"))
                .whenEnded(Instant.parse("2026-04-05T23:59:59Z"))
                .completeness(0)
                .build();
        week.setId(30L);
        return week;
    }

    private static DayGoal dayGoal(WeekGoal week, LocalDate targetDate) {
        DayGoal day = DayGoal.builder()
                .weekGoal(week)
                .dayIndex(targetDate.getDayOfWeek().getValue())
                .targetDate(targetDate)
                .whenStarted(targetDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .whenEnded(targetDate.atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC).toInstant())
                .completeness(0)
                .build();
        day.setId(40L);
        return day;
    }
}
