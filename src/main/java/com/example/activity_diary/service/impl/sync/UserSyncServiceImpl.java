package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.entity.UserSyncState;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
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
        Map<UserSyncEntityType, UserSyncState> existing =
                userSyncStateRepository.findAllByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                UserSyncState::getEntityType,
                                s -> s
                        ));

        for (UserSyncEntityType type : UserSyncEntityType.values()) {
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
    public void bump(Long userId, UserSyncEntityType type) {
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

    @Override
    public Map<UserSyncEntityType, Long> getState(Long userId) {
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
    public UserSyncStateResponseDto getStateDto(Long userId) {
        return new UserSyncStateResponseDto(getState(userId));
    }
}

