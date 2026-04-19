package com.example.activity_diary.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserLockDto {

    @NotNull
    private Boolean locked;
}
