package com.example.activity_diary.entity.food;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "general_food",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_general_food_dictionary_item",
                        columnNames = {"dictionary_item_id"}
                )
        },
        indexes = {
                @Index(name = "idx_general_food_dictionary_item", columnList = "dictionary_item_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralFood extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dictionary_item_id", nullable = false)
    private DictionaryItem dictionaryItem;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal protein = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fat = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal carbs = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal callories = BigDecimal.ZERO;
}
