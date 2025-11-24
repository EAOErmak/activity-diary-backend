package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "diaryEntries", ignore = true) // не создаём каскад
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "chatId", ignore = true)
    User toEntity(UserDto dto);
}