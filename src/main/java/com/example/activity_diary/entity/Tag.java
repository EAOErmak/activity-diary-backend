package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.TagStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "tag",
        indexes = {
                @Index(name = "idx_tag_name", columnList = "name"),
                @Index(name = "idx_tag_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tag_name", columnNames = {"name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tag extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TagStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    public void rename(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be blank");
        }
        this.name = normalizedName;
    }

    public void approve() {
        this.status = TagStatus.APPROVED;
    }

    public void reject() {
        this.status = TagStatus.REJECTED;
    }
}
