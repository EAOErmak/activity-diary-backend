package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}