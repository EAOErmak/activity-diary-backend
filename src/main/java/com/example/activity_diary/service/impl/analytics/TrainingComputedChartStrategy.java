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
import com.example.activity_diary.util.MetricValueNormalizer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class TrainingComputedChartStrategy implements ChartCalculationStrategy {

    private static final int ROOT_SCALE = MetricValueNormalizer.SCALE;
    private static final int ROOT_INTERNAL_SCALE = ROOT_SCALE + 5;
    private static final MathContext ROOT_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private final DiaryRepository diaryRepository;

    public TrainingComputedChartStrategy(
            DiaryRepository diaryRepository
    ) {
        this.diaryRepository = diaryRepository;
    }

    @Override
    public ChartType getChartType(){
        return ChartType.TRAINING_COMPUTED;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {
        List<DiaryEntry> diaryEntryList = diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(
                userId,
                filter.getTagId(),
                filter.getDateFrom(),
                filter.getDateTo()
        );

        List<ChartSeriesDto> chartSeriesDtoList = new ArrayList<>();

        for (DiaryEntry diaryEntry : diaryEntryList) {
            HashMap<DictionaryItem, BigDecimal> total = new HashMap<>();
            List<EntryMetric> metrics = diaryEntry.getMetrics();

            for (EntryMetric metric : metrics) {
                List<EntryMetricValue> values = metric.getValues();
                for (EntryMetricValue value : values) {
                    total.merge(value.getUnit(), value.getValue(), BigDecimal::add);
                }
            }

            BigDecimal progress = geometricMean(total.values());

            List<ChartPointDto> chartPointDtoList = new ArrayList<>();
            chartPointDtoList.add(new ChartPointDto("progress", progress));
            chartSeriesDtoList.add(new ChartSeriesDto(chartPointDtoList));
        }

        return new ChartResponseDto(getChartType(), chartSeriesDtoList);
    }

    private BigDecimal geometricMean(Iterable<BigDecimal> values) {
        BigDecimal product = BigDecimal.ONE;
        int count = 0;

        for (BigDecimal value : values) {
            product = product.multiply(value, ROOT_CONTEXT);
            count++;
        }

        if (count == 0) {
            return BigDecimal.ZERO.setScale(ROOT_SCALE, RoundingMode.HALF_UP);
        }

        return nthRoot(product, count);
    }

    private BigDecimal nthRoot(BigDecimal value, int n) {
        if (value.signum() == 0) {
            return BigDecimal.ZERO.setScale(ROOT_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal nBigDecimal = BigDecimal.valueOf(n);
        BigDecimal x = value.compareTo(BigDecimal.ONE) > 0
                ? value.divide(nBigDecimal, ROOT_INTERNAL_SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-ROOT_SCALE - 1);

        for (int i = 0; i < 50; i++) {
            BigDecimal xToNMinusOne = x.pow(n - 1, ROOT_CONTEXT);
            if (xToNMinusOne.signum() == 0) {
                break;
            }

            BigDecimal numerator = x.multiply(BigDecimal.valueOf(n - 1), ROOT_CONTEXT)
                    .add(value.divide(xToNMinusOne, ROOT_INTERNAL_SCALE, RoundingMode.HALF_UP), ROOT_CONTEXT);
            BigDecimal next = numerator.divide(nBigDecimal, ROOT_INTERNAL_SCALE, RoundingMode.HALF_UP);

            if (next.subtract(x).abs().compareTo(tolerance) <= 0) {
                return next.setScale(ROOT_SCALE, RoundingMode.HALF_UP);
            }

            x = next;
        }

        return x.setScale(ROOT_SCALE, RoundingMode.HALF_UP);
    }
}
