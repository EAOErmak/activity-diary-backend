package com.example.activity_diary.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagMetricLinkResponseDto {
    private Long tagId;
    private Long metricNameId;
    private String metricNameLabel;
}
