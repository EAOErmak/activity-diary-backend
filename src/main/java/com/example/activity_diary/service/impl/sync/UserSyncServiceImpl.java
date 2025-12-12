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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSyncServiceImpl implements UserSyncService {

    private final UserSyncStateRepository userSyncStateRepository;

    @Override
    public void initUser(Long userId) {
        // Запрашиваем ВСЁ сразу
        Map<SyncEntityType, UserSyncState> existing =
                userSyncStateRepository.findAllByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                UserSyncState::getEntityType,
                                s -> s
                        ));

        // Создаём только отсутствующие
        for (SyncEntityType type : SyncEntityType.values()) {
            if (!existing.containsKey(type)) {
                userSyncStateRepository.save(
                        new UserSyncState(
                                userId,
                                type,
                                0L,
                                LocalDateTime.now()
                        )
                );
            }
        }
    }

    @Override
    public void bump(Long userId, SyncEntityType type) {
        int updated = userSyncStateRepository.increment(userId, type);

        // Если записи нет — создаём с версией 1
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

    @Override
    public Map<SyncEntityType, Long> getState(Long userId) {
        List<UserSyncState> states = userSyncStateRepository.findAllByUserId(userId);

        if (states.isEmpty()) {
            initUser(userId);
            states = userSyncStateRepository.findAllByUserId(userId);
        }

        // --- 3. Преобразование в Map без лишних запросов
        return states.stream()
                .collect(Collectors.toMap(
                        UserSyncState::getEntityType,
                        UserSyncState::getVersion
                ));
    }

    @Override
    public SyncStateResponseDto getStateDto(Long userId) {
        return new SyncStateResponseDto(getState(userId));
    }
}

