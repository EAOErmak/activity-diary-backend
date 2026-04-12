package com.example.activity_diary.service.impl.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.GeneralFoodResponseDto;
import com.example.activity_diary.dto.mapper.FoodMapper;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.food.GeneralFood;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.GeneralFoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralFoodServiceImplTest {

    @Mock
    private GeneralFoodRepository generalFoodRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private GeneralFoodServiceImpl service;

    @Test
    void create_savesCallories() {
        DictionaryItem dictionaryItem = dictionaryItem(5L, "Apple");
        FoodUpsertDto dto = upsertDto(5L, "11.10", "22.20", "33.30", "56.00");

        when(generalFoodRepository.existsByDictionaryItemId(5L)).thenReturn(false);
        when(dictionaryRepository.findById(5L)).thenReturn(Optional.of(dictionaryItem));
        when(generalFoodRepository.save(any(GeneralFood.class))).thenAnswer(invocation -> {
            GeneralFood saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });
        when(foodMapper.toDto(any(GeneralFood.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GeneralFoodResponseDto result = service.create(dto);

        ArgumentCaptor<GeneralFood> captor = ArgumentCaptor.forClass(GeneralFood.class);
        verify(generalFoodRepository).save(captor.capture());
        assertEquals(new BigDecimal("56.00"), captor.getValue().getCallories());
        assertEquals(new BigDecimal("56.00"), result.getCallories());
        assertEquals(50L, result.getId());
    }

    @Test
    void update_updatesCallories() {
        DictionaryItem initialDictionaryItem = dictionaryItem(5L, "Apple");
        DictionaryItem updatedDictionaryItem = dictionaryItem(6L, "Banana");
        GeneralFood generalFood = GeneralFood.builder()
                .dictionaryItem(initialDictionaryItem)
                .protein(new BigDecimal("10.00"))
                .fat(new BigDecimal("20.00"))
                .carbs(new BigDecimal("30.00"))
                .callories(new BigDecimal("40.00"))
                .build();
        generalFood.setId(10L);

        FoodUpsertDto dto = upsertDto(6L, "44.40", "55.50", "66.60", "79.00");

        when(generalFoodRepository.findById(10L)).thenReturn(Optional.of(generalFood));
        when(generalFoodRepository.existsByDictionaryItemIdAndIdNot(6L, 10L)).thenReturn(false);
        when(dictionaryRepository.findById(6L)).thenReturn(Optional.of(updatedDictionaryItem));
        when(generalFoodRepository.save(any(GeneralFood.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(foodMapper.toDto(any(GeneralFood.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GeneralFoodResponseDto result = service.update(10L, dto);

        assertEquals(6L, generalFood.getDictionaryItem().getId());
        assertEquals(new BigDecimal("79.00"), generalFood.getCallories());
        assertEquals(new BigDecimal("79.00"), result.getCallories());
    }

    private static FoodUpsertDto upsertDto(
            Long dictionaryItemId,
            String protein,
            String fat,
            String carbs,
            String callories
    ) {
        FoodUpsertDto dto = new FoodUpsertDto();
        dto.setDictionaryItemId(dictionaryItemId);
        dto.setProtein(new BigDecimal(protein));
        dto.setFat(new BigDecimal(fat));
        dto.setCarbs(new BigDecimal(carbs));
        dto.setCallories(new BigDecimal(callories));
        return dto;
    }

    private static GeneralFoodResponseDto toDto(GeneralFood generalFood) {
        GeneralFoodResponseDto dto = new GeneralFoodResponseDto();
        dto.setId(generalFood.getId());
        dto.setDictionaryItemId(generalFood.getDictionaryItem().getId());
        dto.setDictionaryItemLabel(generalFood.getDictionaryItem().getLabel());
        dto.setProtein(generalFood.getProtein());
        dto.setFat(generalFood.getFat());
        dto.setCarbs(generalFood.getCarbs());
        dto.setCallories(generalFood.getCallories());
        return dto;
    }

    private static DictionaryItem dictionaryItem(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label(label)
                .active(true)
                .build();
        item.setId(id);
        return item;
    }
}
