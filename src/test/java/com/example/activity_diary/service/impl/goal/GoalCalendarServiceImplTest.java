package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalCalendarServiceImplTest {

    @Mock
    private GoalCalendarCreateService goalCalendarCreateService;

    @Mock
    private GoalCalendarProgressService goalCalendarProgressService;

    @Mock
    private GoalCalendarMutationService goalCalendarMutationService;

    @Mock
    private GoalDiaryEntryCommandFactory goalDiaryEntryCommandFactory;

    @InjectMocks
    private GoalCalendarServiceImpl service;

    @Test
    void confirmEntryGoal_mapsCreateDtoToInternalCommandBeforeDelegation() {
        Long userId = 10L;
        Long goalId = 20L;
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        GoalDiaryEntryCommand command = new GoalDiaryEntryCommand(null, null, null, "desc #tag", null, null);
        DiaryEntryGoalDetailDto expected = new DiaryEntryGoalDetailDto();

        when(goalDiaryEntryCommandFactory.fromCreateDto(dto)).thenReturn(command);
        when(goalCalendarProgressService.confirmEntryGoal(userId, goalId, command)).thenReturn(expected);

        DiaryEntryGoalDetailDto result = service.confirmEntryGoal(userId, goalId, dto);

        assertEquals(expected, result);
        verify(goalDiaryEntryCommandFactory).fromCreateDto(dto);
        verify(goalCalendarProgressService).confirmEntryGoal(userId, goalId, command);
    }

    @Test
    void updateConfirmedEntryGoal_mapsUpdateDtoToInternalCommandBeforeDelegation() {
        Long userId = 10L;
        Long goalId = 20L;
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        GoalDiaryEntryCommand command = new GoalDiaryEntryCommand(null, null, null, "desc #tag", null, null);
        DiaryEntryGoalDetailDto expected = new DiaryEntryGoalDetailDto();

        when(goalDiaryEntryCommandFactory.fromUpdateDto(dto)).thenReturn(command);
        when(goalCalendarProgressService.updateConfirmedEntryGoal(userId, goalId, command)).thenReturn(expected);

        DiaryEntryGoalDetailDto result = service.updateConfirmedEntryGoal(userId, goalId, dto);

        assertEquals(expected, result);
        verify(goalDiaryEntryCommandFactory).fromUpdateDto(dto);
        verify(goalCalendarProgressService).updateConfirmedEntryGoal(userId, goalId, command);
    }
}
