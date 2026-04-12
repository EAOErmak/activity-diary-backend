package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartSeriesDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class DiaryEntryChartSupportService {

    private static final Comparator<DiaryEntry> ENTRY_ORDER = Comparator
            .comparing(DiaryEntry::getWhenStarted, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(DiaryEntry::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final DiaryRepository diaryRepository;

    public DiaryEntryChartSupportService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    public ChartResponseDto buildSinglePointPerEntryChart(
            ChartType chartType,
            Long userId,
            ChartFilterDto filter,
            Function<DiaryEntry, ChartPointDto> pointFactory
    ) {
        List<ChartSeriesDto> series = diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(
                        userId,
                        filter.getTagId(),
                        filter.getDateFrom(),
                        filter.getDateTo()
                ).stream()
                .sorted(ENTRY_ORDER)
                .map(pointFactory)
                .map(point -> new ChartSeriesDto(List.of(point)))
                .toList();

        return new ChartResponseDto(chartType, series);
    }
}
