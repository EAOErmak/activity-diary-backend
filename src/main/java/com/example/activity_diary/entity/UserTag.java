package com.example.activity_diary.entity;

import com.example.activity_diary.entity.diary.Tag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_tag",
        indexes = {
                @Index(name = "idx_user_tag_user", columnList = "user_id"),
                @Index(name = "idx_user_tag_tag", columnList = "tag_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTag {

    @EmbeddedId
    private UserTagId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static UserTag create(User user, Tag tag) {
        if (user == null || tag == null) throw new IllegalArgumentException("User and tag are required");
        return UserTag.builder()
                .id(new UserTagId(user.getId(), tag.getId()))
                .user(user)
                .tag(tag)
                .build();
    }
}
