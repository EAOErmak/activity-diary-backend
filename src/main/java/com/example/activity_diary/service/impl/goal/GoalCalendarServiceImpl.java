package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.DiaryEntryCreateMode;
import com.example.activity_diary.entity.goal.*;
import com.example.activity_diary.entity.template.*;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.WeekTemplateRepository;
import com.example.activity_diary.service.diary.DiaryService;
import com.example.activity_diary.service.goal.GoalCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoalCalendarServiceImpl implements GoalCalendarService {

    private final UserRepository userRepository;

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final DayTemplateRepository dayTemplateRepository;
    private final WeekTemplateRepository weekTemplateRepository;

    private final WeekGoalRepository weekGoalRepository;
    private final DayGoalRepository dayGoalRepository;
    private final DiaryEntryGoalRepository diaryEntryGoalRepository;
    private final DiaryRepository diaryRepository;

    private final DiaryService diaryService;

    private final GoalMapper goalMapper;

    @Override
    @Transactional
    public DiaryEntryGoalDetailDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DiaryEntryTemplate tpl = diaryEntryTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Entry template not found"));

        WeekGoal week = findOrCreateWeekGoal(user, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        int expectedDurationMin = calculateDurationMinutes(tpl);

        if (alreadyHasSnapshot(day, tpl, expectedDurationMin)) {
            // ничего не создаём, возвращаем “любой” существующий matching goal (можно null, но лучше вернуть существующий)
            DiaryEntryGoal existing = day.getEntryGoals().stream()
                    .filter(g -> sameSnapshot(g, tpl, expectedDurationMin))
                    .findFirst()
                    .orElseThrow();
            return goalMapper.toEntryView(existing);
        }

        DiaryEntryGoal created = createEntryGoalFromTemplate(user, day, tpl, targetDate, expectedDurationMin);
        diaryEntryGoalRepository.save(created);

        return goalMapper.toEntryView(created);
    }

    @Override
    @Transactional
    public DayGoalDetailDto createDayGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DayTemplate dayTemplate = dayTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));

        WeekGoal week = findOrCreateWeekGoal(user, targetDate);
        DayGoal day = findOrCreateDayGoal(week, targetDate);

        int expectedDurationMin = 60;

        for (TemplateEntryItem item : dayTemplate.getItems()) {
            DiaryEntryTemplate tpl = item.getEntryTemplate();

            if (alreadyHasSnapshot(day, tpl, expectedDurationMin)) continue;

            DiaryEntryGoal created = createEntryGoalFromTemplate(user, day, tpl, targetDate, expectedDurationMin);
            diaryEntryGoalRepository.save(created);
        }

        return goalMapper.toDayView(day);
    }

    @Override
    @Transactional
    public WeekGoalDetailDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        WeekTemplate weekTemplate = weekTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Week template not found"));

        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);

        WeekGoal week = findOrCreateWeekGoal(user, targetDate);

        int expectedDurationMin = 60;

        for (TemplateDayItem dayItem : weekTemplate.getItems()) {
            int dayIndex = dayItem.getDayOfWeek(); // 1..7
            LocalDate date = monday.plusDays(dayIndex - 1);

            DayGoal day = findOrCreateDayGoal(week, date);

            DayTemplate dayTemplate = dayItem.getDayTemplate();
            for (TemplateEntryItem entryItem : dayTemplate.getItems()) {
                DiaryEntryTemplate tpl = entryItem.getEntryTemplate();

                if (alreadyHasSnapshot(day, tpl, expectedDurationMin)) continue;

                DiaryEntryGoal created = createEntryGoalFromTemplate(user, day, tpl, date, expectedDurationMin);
                diaryEntryGoalRepository.save(created);
            }
        }

        return goalMapper.toWeekView(week);
    }

    @Override
    @Transactional
    public DiaryEntryGoalDetailDto confirmEntryGoal(Long userId, Long goalId, DiaryEntryCreateDto dto) {

        DiaryEntryGoal goal = diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));

        if (goal.getCurrentEntry() != null) {
            throw new BadRequestException("Goal already confirmed");
        }

        Instant now = Instant.now();
        if (!now.isBefore(goal.getWhenEnded())) {
            throw new BadRequestException("Goal deadline has passed");
        }

        DiaryEntryDto createdDto = diaryService.create(dto, userId, DiaryEntryCreateMode.CONFIRM_GOAL);

        DiaryEntry createdEntry = diaryRepository.findGraphByIdAndUser_Id(createdDto.getId(), userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        goal.setCurrentEntry(createdEntry);
        recalcUp(goal, createdEntry);

        // 4) сохраняем goal (day/week могут сохраниться каскадом, но безопаснее явно пересчитать и сохранить)
        diaryEntryGoalRepository.save(goal);

        return goalMapper.toEntryView(goal);
    }

    @Override
    @Transactional
    public DiaryEntryGoalDetailDto updateConfirmedEntryGoal(Long userId, Long goalId, DiaryEntryUpdateDto dto) {

        DiaryEntryGoal goal = diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));

        if (goal.getCurrentEntry() == null) {
            throw new BadRequestException("Goal not confirmed yet");
        }

        Instant now = Instant.now();
        if (!now.isBefore(goal.getWhenEnded())) {
            throw new BadRequestException("Goal deadline has passed");
        }

        Long entryId = goal.getCurrentEntry().getId();

        DiaryEntryDto updatedDto = diaryService.update(entryId, dto, userId);

        DiaryEntry updatedEntry = diaryRepository.findGraphByIdAndUser_Id(updatedDto.getId(), userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        goal.setCurrentEntry(updatedEntry);
        recalcUp(goal, updatedEntry);

        diaryEntryGoalRepository.save(goal);

        return goalMapper.toEntryView(goal);
    }

    private void recalcUp(DiaryEntryGoal goal, DiaryEntry entry) {

        // 1) entry-goal
        GoalCompletenessCalculator.recalcEntryGoal(goal, entry);
        diaryEntryGoalRepository.save(goal);

        // 2) day
        DayGoal day = goal.getDayGoal();
        GoalCompletenessCalculator.recalcDayGoal(day);
        dayGoalRepository.save(day);

        // 3) week
        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);
        weekGoalRepository.save(week);
    }

    @Override
    @Transactional
    public DayGoalDetailDto confirmDayGoal(Long userId, Long dayGoalId) {

        DayGoal day = dayGoalRepository.findById(dayGoalId)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));

        // защита по пользователю (через weekGoal.user)
        if (!day.getWeekGoal().getUser().getId().equals(userId)) {
            throw new NotFoundException("DayGoal not found");
        }

        Instant now = Instant.now();
        if (!now.isBefore(day.getWhenEnded())) {
            throw new BadRequestException("Day goal deadline has passed");
        }

        // IMPORTANT: entryGoals должны быть загружены.
        // Если lazy — лучше сделать dayGoalRepository.findGraph... (см. ниже)
        for (DiaryEntryGoal goal : day.getEntryGoals()) {

            if (goal.getCurrentEntry() != null) {
                continue; // уже подтверждён
            }

            // создаём DiaryEntryCreateDto из goal
            DiaryEntryCreateDto createDto = buildCreateDtoFromGoal(goal);

            DiaryEntryDto createdDto = diaryService.create(createDto, userId, DiaryEntryCreateMode.CONFIRM_GOAL);

            DiaryEntry createdEntry = diaryRepository.findGraphByIdAndUser_Id(createdDto.getId(), userId)
                    .orElseThrow(() -> new NotFoundException("Entry not found"));

            goal.setCurrentEntry(createdEntry);

            // как ты просишь: ставим сразу 100 (без пересчёта)
            goal.setCompleteness(100);

            diaryEntryGoalRepository.save(goal);
        }

        // день ставим 100
        day.setCompleteness(100);
        dayGoalRepository.save(day);

        // неделю лучше пересчитать по дням (иначе будет 100 даже если другие дни не подтверждены)
        WeekGoal week = day.getWeekGoal();
        GoalCompletenessCalculator.recalcWeekGoal(week);
        weekGoalRepository.save(week);

        return goalMapper.toDayView(day);
    }

    @Override
    @Transactional
    public void deleteWeekGoal(Long userId, LocalDate targetDate) {
        Instant weekStart = weekStartInstant(targetDate);

        WeekGoal week = weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStart)
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));

        assertNotStarted(week.getWhenStarted(), "Week already started");

        weekGoalRepository.delete(week); // каскадом удалит days/entries/metrics/values
    }

    @Override
    @Transactional
    public WeekGoalDetailDto replaceWeekGoal(Long userId, Long templateId, LocalDate targetDate) {
        Instant weekStart = weekStartInstant(targetDate);

        weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStart).ifPresent(week -> {
            assertNotStarted(week.getWhenStarted(), "Week already started");
            weekGoalRepository.delete(week);
        });

        // после удаления создаём новую неделю из WeekTemplate
        return createWeekGoal(userId, templateId, targetDate);
    }

    @Override
    @Transactional
    public void deleteDayGoal(Long userId, LocalDate targetDate) {

        Instant weekStart = weekStartInstant(targetDate);

        WeekGoal week = weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStart)
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));

        DayGoal day = dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));

        assertNotStarted(day.getWhenStarted(), "Day already started");

        dayGoalRepository.delete(day);

        // если после удаления дней не осталось — удаляем week (только если week ещё не началась)
        if (dayGoalRepository.countByWeekGoal_Id(week.getId()) == 0) {
            assertNotStarted(week.getWhenStarted(), "Week already started");
            weekGoalRepository.delete(week);
        }
    }

    @Override
    @Transactional
    public DayGoalDetailDto replaceDayGoal(Long userId, Long templateId, LocalDate targetDate) {

        Instant weekStart = weekStartInstant(targetDate);

        WeekGoal week = weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStart)
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));

        dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate).ifPresent(day -> {
            assertNotStarted(day.getWhenStarted(), "Day already started");
            dayGoalRepository.delete(day);

            if (dayGoalRepository.countByWeekGoal_Id(week.getId()) == 0) {
                assertNotStarted(week.getWhenStarted(), "Week already started");
                weekGoalRepository.delete(week);
            }
        });

        return createDayGoal(userId, templateId, targetDate);
    }

    @Override
    @Transactional
    public void deleteEntryGoal(Long userId, Long entryGoalId) {

        DiaryEntryGoal g = diaryEntryGoalRepository.findByIdAndUser_Id(entryGoalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));

        assertNotStarted(g.getWhenStarted(), "EntryGoal already started");

        DayGoal day = g.getDayGoal();
        WeekGoal week = day.getWeekGoal();

        diaryEntryGoalRepository.delete(g);

        // если в дне больше нет entryGoals — удаляем day (если он ещё не начался)
        if (diaryEntryGoalRepository.countByDayGoal_Id(day.getId()) == 0) {
            assertNotStarted(day.getWhenStarted(), "Day already started");
            dayGoalRepository.delete(day);

            // если в неделе больше нет дней — удаляем week (если он ещё не начался)
            if (dayGoalRepository.countByWeekGoal_Id(week.getId()) == 0) {
                assertNotStarted(week.getWhenStarted(), "Week already started");
                weekGoalRepository.delete(week);
            }
        }
    }

    private Instant weekStartInstant(LocalDate anyDateInWeek) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate monday = anyDateInWeek.with(DayOfWeek.MONDAY);
        return monday.atStartOfDay(zone).toInstant();
    }

    private void assertNotStarted(Instant whenStarted, String message) {
        if (!Instant.now().isBefore(whenStarted)) {
            throw new BadRequestException(message);
        }
    }

    private DiaryEntryCreateDto buildCreateDtoFromGoal(DiaryEntryGoal goal) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(goal.getWhenStarted());
        dto.setWhenEnded(goal.getWhenEnded());
        dto.setMood(goal.getMood());
        dto.setDescription(goal.getDescription());

        // metrics: goal -> createDto.metrics
        if (goal.getMetricGoals() != null && !goal.getMetricGoals().isEmpty()) {
            var metricDtos = new java.util.ArrayList<com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto>();

            for (EntryMetricGoal mg : goal.getMetricGoals()) {
                var mDto = new com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto();
                mDto.setMetricTypeId(mg.getMetricType().getId());

                var valueDtos = new java.util.ArrayList<com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto>();
                for (EntryMetricValueGoal vg : mg.getValues()) {
                    var vDto = new com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto();
                    vDto.setUnitId(vg.getUnit().getId());
                    vDto.setValue(vg.getExpectedValue()); // фактическое = ожидаемое (100%)
                    valueDtos.add(vDto);
                }

                mDto.setValues(valueDtos);
                metricDtos.add(mDto);
            }

            dto.setMetrics(metricDtos);
        }

        return dto;
    }

    /* ===========================
       CREATE / FIND HELPERS
    ============================ */

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

                    DayGoal d = DayGoal.builder()
                            .weekGoal(week)
                            .dayIndex(date.getDayOfWeek().getValue())
                            .targetDate(date)
                            .whenStarted(start)
                            .whenEnded(end)
                            .completeness(0)
                            .build();

                    week.addDay(d);
                    return dayGoalRepository.save(d);
                });
    }

    private DiaryEntryGoal createEntryGoalFromTemplate(
            User user,
            DayGoal day,
            DiaryEntryTemplate tpl,
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
                .name(tpl.getName())
                .mood(tpl.getMood())
                .description(tpl.getDescription())
                .completeness(0)
                .build();

        day.addEntryGoal(goal);

        copyMetricsFromTemplate(goal, tpl);

        return goal;
    }

    private void copyMetricsFromTemplate(DiaryEntryGoal goal, DiaryEntryTemplate tpl) {
        // metricTypeId -> nextPosition
        Map<Long, Integer> posByType = new HashMap<>();

        for (EntryTemplateMetric tm : tpl.getMetrics()) {
            Long typeId = tm.getMetricType().getId();

            int pos = posByType.getOrDefault(typeId, 0) + 1;
            posByType.put(typeId, pos);

            EntryMetricGoal mg = EntryMetricGoal.create(goal, tm.getMetricType(), pos);

            for (EntryTemplateMetricValue tv : tm.getValues()) {
                mg.addValue(tv.getUnit(), tv.getValue());
            }

            goal.addMetricGoal(mg);
        }
    }

    /* ===========================
       ANTI-DUPLICATE (snapshot equality)
    ============================ */

    private boolean alreadyHasSnapshot(DayGoal day, DiaryEntryTemplate tpl, Integer expectedDurationMin) {
        for (DiaryEntryGoal g : day.getEntryGoals()) {
            if (sameSnapshot(g, tpl, expectedDurationMin)) return true;
        }
        return false;
    }

    private boolean sameSnapshot(DiaryEntryGoal g, DiaryEntryTemplate tpl, Integer expectedDurationMin) {
        if (!eq(g.getName(), tpl.getName())) return false;
        if (!eq(g.getMood(), tpl.getMood())) return false;
        if (!eq(norm(g.getDescription()), norm(tpl.getDescription()))) return false;
        if (!eq(g.getExpectedDurationMin(), expectedDurationMin)) return false;

        return goalMetricsMap(g).equals(templateMetricsMap(tpl));
    }

    private Map<Long, Map<Long, Integer>> goalMetricsMap(DiaryEntryGoal g) {
        Map<Long, Map<Long, Integer>> res = new HashMap<>();
        for (EntryMetricGoal mg : g.getMetricGoals()) {
            Long typeId = mg.getMetricType().getId();
            Map<Long, Integer> units = new HashMap<>();
            for (EntryMetricValueGoal vg : mg.getValues()) {
                units.put(vg.getUnit().getId(), vg.getExpectedValue());
            }
            res.put(typeId, units);
        }
        return res;
    }

    private Map<Long, Map<Long, Integer>> templateMetricsMap(DiaryEntryTemplate tpl) {
        Map<Long, Map<Long, Integer>> res = new HashMap<>();
        for (EntryTemplateMetric m : tpl.getMetrics()) {
            Long typeId = m.getMetricType().getId();
            Map<Long, Integer> units = new HashMap<>();
            for (EntryTemplateMetricValue v : m.getValues()) {
                units.put(v.getUnit().getId(), v.getValue());
            }
            res.put(typeId, units);
        }
        return res;
    }

    private int calculateDurationMinutes(DiaryEntryTemplate tpl) {

        if (tpl.getTimeStart() == null || tpl.getTimeEnd() == null) {
            throw new BadRequestException("Template timeStart and timeEnd must be set");
        }

        LocalTime start = tpl.getTimeStart();
        LocalTime end = tpl.getTimeEnd();

        if (!end.isAfter(start)) {
            throw new BadRequestException("Template timeEnd must be after timeStart");
        }

        return (int) Duration.between(start, end).toMinutes();
    }

    private static String norm(String s) {
        return s == null ? null : s.trim();
    }

    private static <T> boolean eq(T a, T b) {
        return java.util.Objects.equals(a, b);
    }
}
