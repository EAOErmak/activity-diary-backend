package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
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
import com.example.activity_diary.service.diary.TemplateGoalsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleTemplateServiceImplTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateGoalsService templateGoalsService;

    @Mock
    private ScheduleTemplateMapper mapper;

    @InjectMocks
    private ScheduleTemplateServiceImpl service;

    @Test
    void createDayTemplate_userMissing_throwsNotFound() {
        DayTemplateCreateDto dto = new DayTemplateCreateDto();
        dto.setName("day");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createDayTemplate(1L, dto));
    }

    @Test
    void createDayTemplate_blankName_throwsBadRequest() {
        DayTemplateCreateDto dto = new DayTemplateCreateDto();
        dto.setName("   ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));

        assertThrows(BadRequestException.class, () -> service.createDayTemplate(1L, dto));
    }

    @Test
    void createDayTemplate_missingEntryTemplates_throwsBadRequest() {
        DayTemplateCreateDto dto = new DayTemplateCreateDto();
        dto.setName("day");
        dto.setEntryTemplateIds(List.of(1L, 2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(diaryEntryTemplateRepository.findAllByIdInAndUserId(List.of(1L, 2L), 1L))
                .thenReturn(List.of(entryTemplate(1L, "t1")));

        assertThrows(BadRequestException.class, () -> service.createDayTemplate(1L, dto));
    }

    @Test
    void updateDayTemplateItems_wrongType_throwsBadRequest() {
        Template week = Template.builder()
                .type(TemplateType.WEEK)
                .user(userWithId(1L))
                .name("w")
                .build();
        week.setId(10L);

        when(templateRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(week));

        TemplateItemsUpdateDto dto = new TemplateItemsUpdateDto();
        dto.setIds(List.of(1L));

        assertThrows(BadRequestException.class, () -> service.updateDayTemplateItems(1L, 10L, dto));
    }

    @Test
    void updateWeekTemplateItems_wrongType_throwsBadRequest() {
        Template day = Template.builder()
                .type(TemplateType.DAY)
                .user(userWithId(1L))
                .name("d")
                .build();
        day.setId(11L);

        when(templateRepository.findByIdAndUserId(11L, 1L))
                .thenReturn(Optional.of(day));

        TemplateItemsUpdateDto dto = new TemplateItemsUpdateDto();
        dto.setIds(List.of(2L));

        assertThrows(BadRequestException.class, () -> service.updateWeekTemplateItems(1L, 11L, dto));
    }

    @Test
    void getTemplate_day_populatesDayItemsAndGoals() {
        Template day = Template.builder()
                .type(TemplateType.DAY)
                .user(userWithId(1L))
                .name("day")
                .build();
        day.setId(5L);

        TemplateEntryItem item = new TemplateEntryItem();
        item.setId(100L);
        item.setTemplate(day);
        item.setEntryTemplate(entryTemplate(7L, "e"));
        item.setPosition(1);
        day.getDayItems().add(item);

        when(templateRepository.findTypeByIdAndUserId(5L, 1L))
                .thenReturn(Optional.of(TemplateType.DAY));
        when(templateRepository.findDayViewByIdAndUserId(5L, 1L))
                .thenReturn(Optional.of(day));

        TemplateViewDto base = new TemplateViewDto();
        base.setId(5L);
        base.setType(TemplateType.DAY);
        when(mapper.toViewDto(day)).thenReturn(base);
        when(mapper.toDto(any(TemplateEntryItem.class))).thenReturn(new TemplateEntryItemDto());

        Tag tag = Tag.builder().name("t").build();
        tag.setId(9L);
        TemplateGoalTag goalTag = TemplateGoalTag.builder()
                .id(new TemplateGoalTagId(5L, 9L))
                .template(day)
                .tag(tag)
                .usageCount(2)
                .build();

        DictionaryItem metricType = DictionaryItem.builder().label("m").build();
        metricType.setId(20L);
        DictionaryItem unit = DictionaryItem.builder().label("u").build();
        unit.setId(21L);
        TemplateGoalMetric goalMetric = TemplateGoalMetric.builder()
                .id(new TemplateGoalMetricId(5L, 20L, 21L))
                .template(day)
                .metricType(metricType)
                .unit(unit)
                .sumValue(3)
                .build();

        when(templateRepository.findGoalTagsByTemplateId(5L, 1L)).thenReturn(List.of(goalTag));
        when(templateRepository.findGoalMetricsByTemplateId(5L, 1L)).thenReturn(List.of(goalMetric));
        when(mapper.toDto(goalTag)).thenReturn(new TemplateGoalTagDto());
        when(mapper.toDto(goalMetric)).thenReturn(new TemplateGoalMetricDto());

        TemplateViewDto result = service.getTemplate(1L, 5L);

        assertEquals(1, result.getDayItems().size());
        assertTrue(result.getWeekItems().isEmpty());
        assertEquals(1, result.getGoalTags().size());
        assertEquals(1, result.getGoalMetrics().size());
    }

    @Test
    void getTemplate_week_populatesWeekItemsAndGoals() {
        Template week = Template.builder()
                .type(TemplateType.WEEK)
                .user(userWithId(1L))
                .name("week")
                .build();
        week.setId(6L);

        Template day = Template.builder()
                .type(TemplateType.DAY)
                .user(userWithId(1L))
                .name("day")
                .build();
        day.setId(7L);

        TemplateDayItem wi = new TemplateDayItem();
        wi.setId(200L);
        wi.setTemplate(week);
        wi.setDayTemplate(day);
        wi.setPosition(1);
        week.getWeekItems().add(wi);

        when(templateRepository.findTypeByIdAndUserId(6L, 1L))
                .thenReturn(Optional.of(TemplateType.WEEK));
        when(templateRepository.findWeekViewByIdAndUserId(6L, 1L))
                .thenReturn(Optional.of(week));

        TemplateViewDto base = new TemplateViewDto();
        base.setId(6L);
        base.setType(TemplateType.WEEK);
        when(mapper.toViewDto(week)).thenReturn(base);
        when(mapper.toDto(any(TemplateDayItem.class))).thenReturn(new TemplateDayItemDto());

        when(templateRepository.findGoalTagsByTemplateId(6L, 1L)).thenReturn(List.of());
        when(templateRepository.findGoalMetricsByTemplateId(6L, 1L)).thenReturn(List.of());

        TemplateViewDto result = service.getTemplate(1L, 6L);

        assertEquals(1, result.getWeekItems().size());
        assertTrue(result.getDayItems().isEmpty());
    }

    @Test
    void listTemplates_setsCounts() {
        Template t = Template.builder()
                .type(TemplateType.DAY)
                .user(userWithId(1L))
                .name("day")
                .build();
        t.setId(10L);

        when(templateRepository.findAllByUserId(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(t)));
        when(templateRepository.countDayItems(anyList())).thenReturn(java.util.Map.of(10L, 2));
        when(templateRepository.countWeekItems(anyList())).thenReturn(java.util.Map.of());
        when(templateRepository.countGoalTags(anyList())).thenReturn(java.util.Map.of(10L, 1));
        when(templateRepository.countGoalMetrics(anyList())).thenReturn(java.util.Map.of());

        TemplateListItemDto dto = service.listTemplates(1L, PageRequest.of(0, 10)).getContent().get(0);

        assertEquals(2, dto.getDayItemsCount());
        assertEquals(0, dto.getWeekItemsCount());
        assertEquals(1, dto.getGoalsTagsCount());
        assertEquals(0, dto.getGoalsMetricsCount());
    }

    @Test
    void deleteTemplate_missing_throwsNotFound() {
        when(templateRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteTemplate(1L, 9L));
        verify(templateRepository, never()).delete(any(Template.class));
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static DiaryEntryTemplate entryTemplate(Long id, String name) {
        DiaryEntryTemplate t = DiaryEntryTemplate.builder()
                .name(name)
                .user(userWithId(1L))
                .build();
        t.setId(id);
        return t;
    }
}
