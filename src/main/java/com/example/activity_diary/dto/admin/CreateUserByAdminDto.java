package com.example.activity_diary.dto.admin;

import com.example.activity_diary.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserByAdminDto {

    @NotBlank
    @Email
    private String username;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    @Size(max = 128)
    private String fullName;

    @Size(max = 128)
    private String email;

    @NotNull
    private Role role;
}
