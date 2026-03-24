package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartSeriesDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TrainingRawChartStrategy implements ChartCalculationStrategy {

    private final DiaryRepository diaryRepository;

    public TrainingRawChartStrategy(
            DiaryRepository diaryRepository
    ) {
        this.diaryRepository = diaryRepository;
    }

    @Override
    public ChartType getChartType(){
        return ChartType.TRAINING_RAW;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {

        List<DiaryEntry> diaryEntryList = diaryRepository.findAllByUserIdAndTags_Id(userId ,filter.getTagId());

        List<ChartSeriesDto> chartSeriesDtoList = new ArrayList<>();

        for(DiaryEntry diaryEntry : diaryEntryList){

            List<EntryMetric> metrics = diaryEntry.getMetrics();

            for(EntryMetric metric : metrics) {

                List<EntryMetricValue> values = metric.getValues();
                List<ChartPointDto> chartPointDtoList = new ArrayList<>();

                for (EntryMetricValue value : values){

                    chartPointDtoList.add(new ChartPointDto(value.getUnit().getLabel(), new BigDecimal(value.getValue())));

                }

                ChartSeriesDto chartSeriesDto = new ChartSeriesDto(chartPointDtoList);
                chartSeriesDtoList.add(chartSeriesDto);
            }
        }

        return new ChartResponseDto(getChartType(), chartSeriesDtoList);
    }
}
