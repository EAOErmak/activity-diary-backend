package com.example.activity_diary.entity.food;

import com.example.activity_diary.entity.User;
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
        name = "user_food",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_food_user_dictionary_item",
                        columnNames = {"user_id", "dictionary_item_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_food_user", columnList = "user_id"),
                @Index(name = "idx_user_food_dictionary_item", columnList = "dictionary_item_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFood extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
}
