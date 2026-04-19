package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EntryTemplateMetricViewDto {
    Long id;
    Long metricTypeId;
    String metricTypeName; // РјРѕР¶РЅРѕ null РµСЃР»Рё РЅРµ С…РѕС‡РµС€СЊ С‚СЏРЅСѓС‚СЊ СЃР»РѕРІР°СЂСЊ
    List<EntryTemplateMetricValueViewDto> values;
}
