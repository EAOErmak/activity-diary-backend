package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.repository.diary.ActivityItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryMetricDetailsLoader {

    private final ActivityItemRepository activityItemRepository;

    public List<EntryMetric> loadForEntry(Long diaryEntryId) {
        if (diaryEntryId == null) {
            return List.of();
        }

        return activityItemRepository.findAllDetailedByDiaryEntryIdIn(List.of(diaryEntryId));
    }

    public Map<Long, List<EntryMetric>> loadForEntries(Collection<Long> diaryEntryIds) {
        if (diaryEntryIds == null || diaryEntryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<EntryMetric>> metricsByEntryId = new LinkedHashMap<>();
        for (Long diaryEntryId : diaryEntryIds) {
            if (diaryEntryId != null) {
                metricsByEntryId.put(diaryEntryId, new ArrayList<>());
            }
        }

        for (EntryMetric metric : activityItemRepository.findAllDetailedByDiaryEntryIdIn(metricsByEntryId.keySet())) {
            if (metric.getDiaryEntry() == null || metric.getDiaryEntry().getId() == null) {
                continue;
            }

            metricsByEntryId.computeIfAbsent(metric.getDiaryEntry().getId(), ignored -> new ArrayList<>())
                    .add(metric);
        }

        return metricsByEntryId;
    }
}
