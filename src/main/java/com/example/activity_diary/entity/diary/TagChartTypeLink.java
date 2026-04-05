package com.example.activity_diary.entity.diary;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.ChartType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tag_chart_type_link",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tag_chart_type_link_tag_chart_type",
                        columnNames = {"tag_id", "chart_type"}
                )
        },
        indexes = {
                @Index(name = "idx_tag_chart_type_link_tag", columnList = "tag_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagChartTypeLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false, length = 64)
    private ChartType chartType;

    public static TagChartTypeLink create(Tag tag, ChartType chartType) {
        validate(tag, chartType);

        TagChartTypeLink link = new TagChartTypeLink();
        link.tag = tag;
        link.chartType = chartType;
        return link;
    }

    @PrePersist
    @PreUpdate
    private void validateLink() {
        validate(tag, chartType);
    }

    private static void validate(Tag tag, ChartType chartType) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag is required");
        }

        if (chartType == null) {
            throw new IllegalArgumentException("Chart type is required");
        }
    }
}
