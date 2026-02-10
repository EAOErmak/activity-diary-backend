package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.TemplateType;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.TemplateEntryItemRepository;
import com.example.activity_diary.repository.template.TemplateGoalMetricRepository;
import com.example.activity_diary.repository.template.TemplateGoalTagRepository;
import com.example.activity_diary.repository.template.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateGoalsServiceImplTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateEntryItemRepository templateEntryItemRepository;

    @Mock
    private TemplateGoalTagRepository goalTagRepository;

    @Mock
    private TemplateGoalMetricRepository goalMetricRepository;

    @InjectMocks
    private TemplateGoalsServiceImpl service;

    @Test
    void recalcGoals_templateMissing_throwsNotFound() {
        when(templateRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.recalcGoals(10L, 1L));
    }

    @Test
    void recalcGoals_dayTemplate_aggregatesTagsAndMetrics() {
        Template day = template(1L, TemplateType.DAY, 10L);
        when(templateRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(day));

        Tag tag1 = tag(1L, "work");
        Tag tag2 = tag(2L, "sport");

        DiaryEntryTemplate et1 = entryTemplate(11L, Set.of(tag1, tag2));
        DiaryEntryTemplate et2 = entryTemplate(12L, Set.of(tag1));

        DictionaryItem metricType = dictItem(100L, "Steps");
        DictionaryItem unit = dictItem(200L, "count");

        EntryTemplateMetric m1 = EntryTemplateMetric.create(et1, metricType);
        m1.addValue(unit, 3);
        et1.addMetric(m1);

        EntryTemplateMetric m2 = EntryTemplateMetric.create(et2, metricType);
        m2.addValue(unit, 4);
        et2.addMetric(m2);

        TemplateEntryItem di1 = dayItem(day, et1, 1);
        TemplateEntryItem di2 = dayItem(day, et2, 2);

        when(templateEntryItemRepository.findDayItemsGraph(List.of(1L)))
                .thenReturn(List.of(di1, di2));

        service.recalcGoals(10L, 1L);

        verify(goalTagRepository).deleteByTemplateId(1L);
        verify(goalMetricRepository).deleteByTemplateId(1L);

        ArgumentCaptor<List<TemplateGoalTag>> tagCaptor = ArgumentCaptor.forClass(List.class);
        verify(goalTagRepository).saveAll(tagCaptor.capture());

        List<TemplateGoalTag> savedTags = tagCaptor.getValue();
        assertEquals(2, savedTags.size());

        TemplateGoalTag t1 = savedTags.stream()
                .filter(t -> t.getTag().getId().equals(1L))
                .findFirst()
                .orElseThrow();
        TemplateGoalTag t2 = savedTags.stream()
                .filter(t -> t.getTag().getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertEquals(2, t1.getUsageCount());
        assertEquals(1, t2.getUsageCount());

        ArgumentCaptor<List<TemplateGoalMetric>> metricCaptor = ArgumentCaptor.forClass(List.class);
        verify(goalMetricRepository).saveAll(metricCaptor.capture());

        List<TemplateGoalMetric> savedMetrics = metricCaptor.getValue();
        assertEquals(1, savedMetrics.size());
        assertEquals(7, savedMetrics.get(0).getSumValue());
    }

    @Test
    void recalcGoals_weekTemplate_usesWeekGraph() {
        Template week = template(2L, TemplateType.WEEK, 10L);
        when(templateRepository.findByIdAndUserId(2L, 10L)).thenReturn(Optional.of(week));

        Template day1 = template(3L, TemplateType.DAY, 10L);
        Template day2 = template(4L, TemplateType.DAY, 10L);

        TemplateDayItem wi1 = new TemplateDayItem();
        wi1.setTemplate(week);
        wi1.setDayTemplate(day1);
        wi1.setPosition(1);
        TemplateDayItem wi2 = new TemplateDayItem();
        wi2.setTemplate(week);
        wi2.setDayTemplate(day2);
        wi2.setPosition(2);
        week.getWeekItems().addAll(List.of(wi1, wi2));

        when(templateRepository.findWeekWithDays(2L, 10L)).thenReturn(Optional.of(week));
        when(templateEntryItemRepository.findDayItemsGraph(List.of(3L, 4L)))
                .thenReturn(List.of());

        service.recalcGoals(10L, 2L);

        verify(templateEntryItemRepository).findDayItemsGraph(eq(List.of(3L, 4L)));
        verify(goalTagRepository).deleteByTemplateId(2L);
        verify(goalMetricRepository).deleteByTemplateId(2L);
    }

    private static Template template(Long id, TemplateType type, Long userId) {
        Template t = Template.builder()
                .type(type)
                .name("t")
                .user(userWithId(userId))
                .build();
        t.setId(id);
        return t;
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static Tag tag(Long id, String name) {
        Tag tag = Tag.builder().name(name).build();
        tag.setId(id);
        return tag;
    }

    private static DictionaryItem dictItem(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder().label(label).build();
        item.setId(id);
        return item;
    }

    private static DiaryEntryTemplate entryTemplate(Long id, Set<Tag> tags) {
        DiaryEntryTemplate t = DiaryEntryTemplate.builder()
                .name("entry")
                .user(userWithId(10L))
                .build();
        t.setId(id);
        t.setTags(tags);
        return t;
    }

    private static TemplateEntryItem dayItem(Template template, DiaryEntryTemplate entryTemplate, int pos) {
        TemplateEntryItem item = new TemplateEntryItem();
        item.setTemplate(template);
        item.setEntryTemplate(entryTemplate);
        item.setPosition(pos);
        return item;
    }
}
