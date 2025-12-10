package com.example.activity_diary.dto.user;

import com.example.activity_diary.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String username;

    private String fullName;

    private Role role;

    private boolean enabled;
}

