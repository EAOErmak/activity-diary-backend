package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.entity.User;
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
    private final UserRepository userRepository;

    @Override
    public void initUser(Long userId) {
        for (SyncEntityType type : SyncEntityType.values()) {
            UserSyncState state = new UserSyncState(
                    userId, type, 0L, LocalDateTime.now()
            );
            userSyncStateRepository.save(state);
        }
    }

    @Override
    public void bump(Long userId, SyncEntityType type) {
        userSyncStateRepository.increment(userId, type);
    }

    @Override
    public Map<SyncEntityType, Long> getState(Long userId) {

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

        return userSyncStateRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        UserSyncState::getEntityType,
                        UserSyncState::getVersion
                ));
    }

    @Override
    public SyncStateResponseDto getStateByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new SyncStateResponseDto(
                getState(user.getId())
        );
    }
}


