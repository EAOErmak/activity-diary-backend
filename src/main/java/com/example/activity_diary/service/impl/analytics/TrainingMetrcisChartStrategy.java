package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartSeriesDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class TrainingMetrcisChartStrategy implements ChartCalculationStrategy {

    private final DiaryRepository diaryRepository;

    public TrainingMetrcisChartStrategy(
            DiaryRepository diaryRepository
    ) {
        this.diaryRepository = diaryRepository;
    }

    @Override
    public ChartType getChartType(){
        return ChartType.TRAINING_METRICS;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {

        List<DiaryEntry> diaryEntryList = diaryRepository.findAllByUserIdAndTags_Id(userId ,filter.getTagId());

        List<ChartSeriesDto> chartSeriesDtoList = new ArrayList<>();

        for(DiaryEntry diaryEntry : diaryEntryList){

            HashMap<DictionaryItem, BigDecimal> total = new HashMap<>();
            List<EntryMetric> metrics = diaryEntry.getMetrics();

            for(EntryMetric metric : metrics) {

                List<EntryMetricValue> values = metric.getValues();


                for (EntryMetricValue value : values){
                    total.merge(value.getUnit(), value.getValue(), BigDecimal::add);
                }

            }

            List<ChartPointDto> chartPointDtoList = new ArrayList<>();

            for (HashMap.Entry<DictionaryItem, BigDecimal> entry : total.entrySet()) {
                chartPointDtoList.add(new ChartPointDto(entry.getKey().getLabel(), entry.getValue()));
            }
            ChartSeriesDto chartSeriesDto = new ChartSeriesDto(chartPointDtoList);
            chartSeriesDtoList.add(chartSeriesDto);
        }

        return new ChartResponseDto(getChartType(), chartSeriesDtoList);
    }
}
