package com.example.activity_diary.service.impl.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;
import com.example.activity_diary.dto.mapper.FoodMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.food.UserFood;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.food.UserFoodRepository;
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
class UserFoodServiceImplTest {

    @Mock
    private UserFoodRepository userFoodRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private UserFoodServiceImpl service;

    @Test
    void create_savesCallories() {
        User user = user(3L, "tester");
        DictionaryItem dictionaryItem = dictionaryItem(5L, "Apple");
        FoodUpsertDto dto = upsertDto(5L, "11.10", "22.20", "33.30", "56.00");

        when(userFoodRepository.existsByUserIdAndDictionaryItemId(3L, 5L)).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(dictionaryRepository.findById(5L)).thenReturn(Optional.of(dictionaryItem));
        when(userFoodRepository.save(any(UserFood.class))).thenAnswer(invocation -> {
            UserFood saved = invocation.getArgument(0);
            saved.setId(70L);
            return saved;
        });
        when(foodMapper.toDto(any(UserFood.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        UserFoodResponseDto result = service.create(3L, dto);

        ArgumentCaptor<UserFood> captor = ArgumentCaptor.forClass(UserFood.class);
        verify(userFoodRepository).save(captor.capture());
        assertEquals(new BigDecimal("56.00"), captor.getValue().getCallories());
        assertEquals(new BigDecimal("56.00"), result.getCallories());
        assertEquals(70L, result.getId());
    }

    @Test
    void update_updatesCallories() {
        User user = user(3L, "tester");
        DictionaryItem initialDictionaryItem = dictionaryItem(5L, "Apple");
        DictionaryItem updatedDictionaryItem = dictionaryItem(6L, "Banana");
        UserFood userFood = UserFood.builder()
                .user(user)
                .dictionaryItem(initialDictionaryItem)
                .protein(new BigDecimal("10.00"))
                .fat(new BigDecimal("20.00"))
                .carbs(new BigDecimal("30.00"))
                .callories(new BigDecimal("40.00"))
                .build();
        userFood.setId(11L);

        FoodUpsertDto dto = upsertDto(6L, "44.40", "55.50", "66.60", "79.00");

        when(userFoodRepository.findByIdAndUserId(11L, 3L)).thenReturn(Optional.of(userFood));
        when(userFoodRepository.existsByUserIdAndDictionaryItemIdAndIdNot(3L, 6L, 11L)).thenReturn(false);
        when(dictionaryRepository.findById(6L)).thenReturn(Optional.of(updatedDictionaryItem));
        when(userFoodRepository.save(any(UserFood.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(foodMapper.toDto(any(UserFood.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        UserFoodResponseDto result = service.update(3L, 11L, dto);

        assertEquals(6L, userFood.getDictionaryItem().getId());
        assertEquals(new BigDecimal("79.00"), userFood.getCallories());
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

    private static UserFoodResponseDto toDto(UserFood userFood) {
        UserFoodResponseDto dto = new UserFoodResponseDto();
        dto.setId(userFood.getId());
        dto.setUserId(userFood.getUser().getId());
        dto.setDictionaryItemId(userFood.getDictionaryItem().getId());
        dto.setDictionaryItemLabel(userFood.getDictionaryItem().getLabel());
        dto.setProtein(userFood.getProtein());
        dto.setFat(userFood.getFat());
        dto.setCarbs(userFood.getCarbs());
        dto.setCallories(userFood.getCallories());
        return dto;
    }

    private static User user(Long id, String username) {
        User user = User.builder()
                .username(username)
                .build();
        user.setId(id);
        return user;
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
