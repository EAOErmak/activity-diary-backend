package com.example.activity_diary.service.impl.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.GeneralFoodResponseDto;
import com.example.activity_diary.dto.mapper.FoodMapper;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.food.GeneralFood;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import com.example.activity_diary.service.food.GeneralFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GeneralFoodServiceImpl implements GeneralFoodService {

    private final GeneralFoodRepository generalFoodRepository;
    private final DictionaryRepository dictionaryRepository;
    private final FoodMapper foodMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GeneralFoodResponseDto> getAll(String q) {
        List<GeneralFood> foods = hasText(q)
                ? generalFoodRepository.searchByDictionaryLabel(q.trim())
                : generalFoodRepository.findAllOrdered();

        return foodMapper.toGeneralFoodDtoList(foods);
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralFoodResponseDto getById(Long id) {
        return foodMapper.toDto(getGeneralFood(id));
    }

    @Override
    public GeneralFoodResponseDto create(FoodUpsertDto dto) {
        validateUniqueDictionaryItem(dto.getDictionaryItemId(), null);

        DictionaryItem dictionaryItem = getDictionaryItem(dto.getDictionaryItemId());

        GeneralFood generalFood = GeneralFood.builder()
                .dictionaryItem(dictionaryItem)
                .protein(normalizeValue(dto.getProtein(), "Protein"))
                .fat(normalizeValue(dto.getFat(), "Fat"))
                .carbs(normalizeValue(dto.getCarbs(), "Carbs"))
                .callories(normalizeValue(dto.getCallories(), "Callories"))
                .build();

        return foodMapper.toDto(generalFoodRepository.save(generalFood));
    }

    @Override
    public GeneralFoodResponseDto update(Long id, FoodUpsertDto dto) {
        GeneralFood generalFood = getGeneralFood(id);

        validateUniqueDictionaryItem(dto.getDictionaryItemId(), id);

        generalFood.setDictionaryItem(getDictionaryItem(dto.getDictionaryItemId()));
        generalFood.setProtein(normalizeValue(dto.getProtein(), "Protein"));
        generalFood.setFat(normalizeValue(dto.getFat(), "Fat"));
        generalFood.setCarbs(normalizeValue(dto.getCarbs(), "Carbs"));
        generalFood.setCallories(normalizeValue(dto.getCallories(), "Callories"));

        return foodMapper.toDto(generalFoodRepository.save(generalFood));
    }

    @Override
    public void delete(Long id) {
        GeneralFood generalFood = getGeneralFood(id);
        generalFoodRepository.delete(generalFood);
    }

    private GeneralFood getGeneralFood(Long id) {
        return generalFoodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("General food not found"));
    }

    private DictionaryItem getDictionaryItem(Long dictionaryItemId) {
        return dictionaryRepository.findById(dictionaryItemId)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));
    }

    private void validateUniqueDictionaryItem(Long dictionaryItemId, Long currentId) {
        boolean exists = currentId == null
                ? generalFoodRepository.existsByDictionaryItemId(dictionaryItemId)
                : generalFoodRepository.existsByDictionaryItemIdAndIdNot(dictionaryItemId, currentId);

        if (exists) {
            throw new BadRequestException("General food for this dictionary item already exists");
        }
    }

    private BigDecimal normalizeValue(BigDecimal value, String fieldName) {
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
