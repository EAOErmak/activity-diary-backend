package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.repository.template.EntryTemplateMetricRepository;
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
public class EntryTemplateMetricDetailsLoader {

    private final EntryTemplateMetricRepository entryTemplateMetricRepository;

    public List<EntryTemplateMetric> loadForTemplate(Long templateId) {
        if (templateId == null) {
            return List.of();
        }

        return entryTemplateMetricRepository.findAllDetailedByTemplateIdIn(List.of(templateId));
    }

    public Map<Long, List<EntryTemplateMetric>> loadForTemplates(Collection<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<EntryTemplateMetric>> metricsByTemplateId = new LinkedHashMap<>();
        for (Long templateId : templateIds) {
            if (templateId != null) {
                metricsByTemplateId.put(templateId, new ArrayList<>());
            }
        }

        for (EntryTemplateMetric metric : entryTemplateMetricRepository.findAllDetailedByTemplateIdIn(metricsByTemplateId.keySet())) {
            if (metric.getTemplate() == null || metric.getTemplate().getId() == null) {
                continue;
            }

            metricsByTemplateId.computeIfAbsent(metric.getTemplate().getId(), ignored -> new ArrayList<>())
                    .add(metric);
        }

        return metricsByTemplateId;
    }
}
