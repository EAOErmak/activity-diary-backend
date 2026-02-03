package com.example.activity_diary.repository;

import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.UserTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTagRepository extends JpaRepository<UserTag, UserTagId> {
}
