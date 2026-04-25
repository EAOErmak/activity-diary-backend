package com.example.activity_diary.repository.tag;

import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.UserTagId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTagRepository extends JpaRepository<UserTag, UserTagId> {

    @Modifying
    @Query("""
        delete from UserTag userTag
        where userTag.tag.id = :tagId
    """)
    void deleteByTagId(@Param("tagId") Long tagId);
}
