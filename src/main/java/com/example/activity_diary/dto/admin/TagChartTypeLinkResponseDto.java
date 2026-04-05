package com.example.activity_diary.dto.admin;

import com.example.activity_diary.entity.enums.ChartType;
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
public class TagChartTypeLinkResponseDto {
    private Long tagId;
    private ChartType chartType;
}
