package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.admin.AdminUserDto;
import com.example.activity_diary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(source = "role", target = "role")
    @Mapping(
            target = "fullName",
            expression = "java(user.getFullName() == null ? \"\" : user.getFullName())"
    )
    @Mapping(target = "accountLocked", expression = "java(user.isCurrentlyLocked())")
    @Mapping(target = "chatId", ignore = true)
    AdminUserDto toAdminDto(User user);
}
