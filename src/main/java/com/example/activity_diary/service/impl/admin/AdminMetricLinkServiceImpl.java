package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.MetricLinkResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.dict.MetricNameUnitLink;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.MetricNameUnitLinkRepository;
import com.example.activity_diary.service.admin.AdminMetricLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMetricLinkServiceImpl implements AdminMetricLinkService {

    private final MetricNameUnitLinkRepository metricNameUnitLinkRepository;
    private final DictionaryRepository dictionaryRepository;
    @Override
    public MetricLinkResponseDto createLink(Long metricNameId, Long metricUnitId) {
        DictionaryItem metricName = getDictionaryItem(metricNameId, DictionaryType.METRIC_NAME, "Metric name");
        DictionaryItem metricUnit = getDictionaryItem(metricUnitId, DictionaryType.METRIC_UNIT, "Metric unit");

        if (metricNameUnitLinkRepository.existsByMetricNameIdAndMetricUnitId(metricNameId, metricUnitId)) {
            throw new BadRequestException("Metric link already exists");
        }

        MetricNameUnitLink link = MetricNameUnitLink.create(metricName, metricUnit);
        MetricNameUnitLink saved = metricNameUnitLinkRepository.save(link);

        return toDto(saved.getMetricUnit());
    }

    @Override
    public void deleteLink(Long metricNameId, Long metricUnitId) {
        getDictionaryItem(metricNameId, DictionaryType.METRIC_NAME, "Metric name");
        getDictionaryItem(metricUnitId, DictionaryType.METRIC_UNIT, "Metric unit");

        if (!metricNameUnitLinkRepository.existsByMetricNameIdAndMetricUnitId(metricNameId, metricUnitId)) {
            throw new BadRequestException("Metric link does not exist");
        }

        metricNameUnitLinkRepository.deleteByMetricNameIdAndMetricUnitId(metricNameId, metricUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricLinkResponseDto> getUnitsByMetricName(Long metricNameId) {
        getDictionaryItem(metricNameId, DictionaryType.METRIC_NAME, "Metric name");

        return metricNameUnitLinkRepository.findByMetricNameId(metricNameId).stream()
                .map(MetricNameUnitLink::getMetricUnit)
                .map(this::toDto)
                .toList();
    }

    private DictionaryItem getDictionaryItem(Long id, DictionaryType expectedType, String fieldName) {
        DictionaryItem item = dictionaryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(fieldName + " not found"));

        if (item.getType() != expectedType) {
            throw new BadRequestException(fieldName + " must be of type " + expectedType);
        }

        return item;
    }

    private MetricLinkResponseDto toDto(DictionaryItem item) {
        return MetricLinkResponseDto.builder()
                .id(item.getId())
                .label(item.getLabel())
                .build();
    }
}
