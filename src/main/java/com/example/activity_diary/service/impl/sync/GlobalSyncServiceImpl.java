package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.GlobalSyncStateResponseDto;
import com.example.activity_diary.entity.GlobalSyncState;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.repository.GlobalSyncStateRepository;

import com.example.activity_diary.service.sync.GlobalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GlobalSyncServiceImpl implements GlobalSyncService {

    private final GlobalSyncStateRepository repository;

    @Override
    public void initIfNeeded() {
        for (GlobalSyncEntityType type : GlobalSyncEntityType.values()) {
            repository.findById(type).orElseGet(() ->
                    repository.save(new GlobalSyncState(type, 0L, LocalDateTime.now()))
            );

            repository.findById(type).orElseGet(() ->
                    repository.save(new GlobalSyncState(
                            type,
                            0L,
                            LocalDateTime.now()
                    ))
            );
        }
    }

    @Override
    public void bump(GlobalSyncEntityType type) {
        int updated = repository.increment(type);

        if (updated == 0) {
            repository.save(new GlobalSyncState(
                    type,
                    1L,
                    LocalDateTime.now()
            ));
        }
    }

    @Override
    public Map<GlobalSyncEntityType, Long> getState() {
        initIfNeeded();

        return repository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        GlobalSyncState::getEntityType,
                        GlobalSyncState::getVersion
                ));
    }
    @Override
    public GlobalSyncStateResponseDto getStateDto() {
        return new GlobalSyncStateResponseDto(getState());
    }
}


