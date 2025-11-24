package com.example.activity_diary.repository;

import com.example.activity_diary.entity.RegistrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationEventRepository extends JpaRepository<RegistrationEvent, Long> {

    long countByIp(String ip);
}
