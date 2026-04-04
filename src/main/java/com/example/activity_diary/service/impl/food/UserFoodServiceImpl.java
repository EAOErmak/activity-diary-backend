package com.example.activity_diary.service.impl.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;
import com.example.activity_diary.dto.mapper.FoodMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.food.UserFood;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.UserFoodRepository;
import com.example.activity_diary.service.food.UserFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFoodServiceImpl implements UserFoodService {

    private final UserFoodRepository userFoodRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;
    private final FoodMapper foodMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserFoodResponseDto> getAll(Long userId, String q) {
        List<UserFood> foods = hasText(q)
                ? userFoodRepository.searchByUserIdAndDictionaryLabel(userId, q.trim())
                : userFoodRepository.findAllByUserIdOrdered(userId);

        return foodMapper.toUserFoodDtoList(foods);
    }

    @Override
    @Transactional(readOnly = true)
    public UserFoodResponseDto getById(Long userId, Long id) {
        return foodMapper.toDto(getUserFood(userId, id));
    }

    @Override
    public UserFoodResponseDto create(Long userId, FoodUpsertDto dto) {
        validateUniqueDictionaryItem(userId, dto.getDictionaryItemId(), null);

        UserFood userFood = UserFood.builder()
                .user(getUser(userId))
                .dictionaryItem(getDictionaryItem(dto.getDictionaryItemId()))
                .protein(normalizeMacro(dto.getProtein(), "Protein"))
                .fat(normalizeMacro(dto.getFat(), "Fat"))
                .carbs(normalizeMacro(dto.getCarbs(), "Carbs"))
                .build();

        return foodMapper.toDto(userFoodRepository.save(userFood));
    }

    @Override
    public UserFoodResponseDto update(Long userId, Long id, FoodUpsertDto dto) {
        UserFood userFood = getUserFood(userId, id);

        validateUniqueDictionaryItem(userId, dto.getDictionaryItemId(), id);

        userFood.setDictionaryItem(getDictionaryItem(dto.getDictionaryItemId()));
        userFood.setProtein(normalizeMacro(dto.getProtein(), "Protein"));
        userFood.setFat(normalizeMacro(dto.getFat(), "Fat"));
        userFood.setCarbs(normalizeMacro(dto.getCarbs(), "Carbs"));

        return foodMapper.toDto(userFoodRepository.save(userFood));
    }

    @Override
    public void delete(Long userId, Long id) {
        UserFood userFood = getUserFood(userId, id);
        userFoodRepository.delete(userFood);
    }

    private UserFood getUserFood(Long userId, Long id) {
        return userFoodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("User food not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private DictionaryItem getDictionaryItem(Long dictionaryItemId) {
        return dictionaryRepository.findById(dictionaryItemId)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));
    }

    private void validateUniqueDictionaryItem(Long userId, Long dictionaryItemId, Long currentId) {
        boolean exists = currentId == null
                ? userFoodRepository.existsByUserIdAndDictionaryItemId(userId, dictionaryItemId)
                : userFoodRepository.existsByUserIdAndDictionaryItemIdAndIdNot(userId, dictionaryItemId, currentId);

        if (exists) {
            throw new BadRequestException("User food for this dictionary item already exists");
        }
    }

    private BigDecimal normalizeMacro(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
