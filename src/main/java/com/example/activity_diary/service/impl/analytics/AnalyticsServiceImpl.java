package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.service.analytics.AnalyticsService;
import com.example.activity_diary.service.analytics.ChartValueService;
import com.example.activity_diary.service.diary.DiaryAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final DiaryRepository diaryRepository;
    private final ChartValueService chartValueService;
    private final DiaryAccessService diaryAccessService;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // =========================================================
    // ====================== BY TIME ==========================
    // =========================================================

    @Override
    public ChartResponseDto buildByTimeByCategory(
            UserDetails currentUser,
            Long whatHappenedId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Long userId = diaryAccessService.getUserId(currentUser);

        List<DiaryEntry> entries =
                diaryRepository.findForAnalyticsByCategory(userId, whatHappenedId, from, to);

        return buildTimeChart(entries);
    }

    @Override
    public ChartResponseDto buildByTimeByWhat(
            UserDetails currentUser,
            Long whatId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Long userId = diaryAccessService.getUserId(currentUser);

        List<DiaryEntry> entries =
                diaryRepository.findForAnalyticsByWhat(userId, whatId, from, to);

        return buildTimeChart(entries);
    }

    // =========================================================
    // ==================== BY SEQUENCE ========================
    // =========================================================

    @Override
    public ChartResponseDto buildBySequenceByCategory(
            UserDetails currentUser,
            Long whatHappenedId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Long userId = diaryAccessService.getUserId(currentUser);

        List<DiaryEntry> entries =
                diaryRepository.findForAnalyticsByCategory(userId, whatHappenedId, from, to);

        return buildSequenceChart(entries);
    }

    @Override
    public ChartResponseDto buildBySequenceByWhat(
            UserDetails currentUser,
            Long whatId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Long userId = diaryAccessService.getUserId(currentUser);

        List<DiaryEntry> entries =
                diaryRepository.findForAnalyticsByWhat(userId, whatId, from, to);

        return buildSequenceChart(entries);
    }

    // =========================================================
    // =================== COMMON BUILDERS =====================
    // =========================================================

    private ChartResponseDto buildTimeChart(List<DiaryEntry> entries) {
        ChartResponseDto response = baseResponse(entries);

        if (entries == null || entries.isEmpty()) {
            response.setPoints(List.of());
            return response;
        }

        List<ChartPointDto> points = entries.stream()
                .sorted(Comparator.comparing(DiaryEntry::getWhenStarted))
                .map(entry -> new ChartPointDto(
                        entry.getWhenStarted().format(TIME_FORMAT),
                        chartValueService.calculateY(entry)
                ))
                .toList();

        response.setPoints(points);
        return response;
    }

    private ChartResponseDto buildSequenceChart(List<DiaryEntry> entries) {
        ChartResponseDto response = baseResponse(entries);

        if (entries == null || entries.isEmpty()) {
            response.setPoints(List.of());
            return response;
        }

        List<DiaryEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(DiaryEntry::getWhenStarted))
                .toList();

        List<ChartPointDto> points = new ArrayList<>();
        int index = 1;

        for (DiaryEntry entry : sorted) {
            points.add(new ChartPointDto(
                    String.valueOf(index++),
                    chartValueService.calculateY(entry)
            ));
        }

        response.setPoints(points);
        return response;
    }

    private ChartResponseDto baseResponse(List<DiaryEntry> entries) {
        ChartResponseDto response = new ChartResponseDto();

        if (entries == null || entries.isEmpty()) {
            return response;
        }

        DiaryEntry first = entries.get(0);

        response.setTitle(
                first.getWhat() != null
                        ? first.getWhat().getLabel()
                        : first.getWhatHappened().getLabel()
        );

        response.setChartType(first.getWhatHappened().getChartType());

        response.setUnit(
                first.getWhatDidYouDo().isEmpty()
                        ? null
                        : first.getWhatDidYouDo().get(0).getUnit().getLabel()
        );

        return response;
    }
}
