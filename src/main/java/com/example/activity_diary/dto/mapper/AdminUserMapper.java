package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.admin.AdminUserDto;
import com.example.activity_diary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(source = "role", target = "role")
    AdminUserDto toAdminDto(User user);
}
