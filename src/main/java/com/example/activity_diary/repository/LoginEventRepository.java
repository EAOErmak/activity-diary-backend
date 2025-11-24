package com.example.activity_diary.repository;

import com.example.activity_diary.entity.log.LoginEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginEventRepository extends JpaRepository<LoginEvent, Long> {}
