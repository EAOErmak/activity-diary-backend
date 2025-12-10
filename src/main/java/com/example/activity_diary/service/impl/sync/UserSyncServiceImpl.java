package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.entity.UserSyncState;
import com.example.activity_diary.entity.enums.SyncEntityType;
import com.example.activity_diary.repository.UserSyncStateRepository;
import com.example.activity_diary.service.sync.UserSyncService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSyncServiceImpl implements UserSyncService {

    private final UserSyncStateRepository userSyncStateRepository;

    // INIT USER: создать записи, если их нет
    @Override
    public void initUser(Long userId) {
        for (SyncEntityType type : SyncEntityType.values()) {
            userSyncStateRepository.findByUserIdAndEntityType(userId, type)
                    .orElseGet(() -> userSyncStateRepository.save(
                            new UserSyncState(
                                    userId,
                                    type,
                                    0L,
                                    LocalDateTime.now()
                            )
                    ));
        }
    }

    // BUMP VERSION
    @Override
    public void bump(Long userId, SyncEntityType type) {
        int updated = userSyncStateRepository.increment(userId, type);

        if (updated == 0) {
            userSyncStateRepository.save(
                    new UserSyncState(
                            userId,
                            type,
                            1L,
                            LocalDateTime.now()
                    )
            );
        }
    }

    // GET SYNC STATE (MAP)
    @Override
    public Map<SyncEntityType, Long> getState(Long userId) {
        initUser(userId);

        return userSyncStateRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        UserSyncState::getEntityType,
                        UserSyncState::getVersion
                ));
    }

    // GET SYNC STATE (DTO)
    @Override
    public SyncStateResponseDto getStateDto(Long userId) {
        return new SyncStateResponseDto(getState(userId));
    }
}
