package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.base.BaseDictionary;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "activity_item_unit_dict")
@Getter
@Setter
@NoArgsConstructor
public class ActivityItemUnitDict extends BaseDictionary {
}
