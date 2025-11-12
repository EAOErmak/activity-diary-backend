package com.example.activity_diary.repository;

import com.example.activity_diary.entity.ActivityItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityItemRepository extends JpaRepository<ActivityItem, Long> {
}
