package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.AdminUserDto;
import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.dto.admin.UpdateUserRoleDto;
import com.example.activity_diary.dto.mapper.AdminUserMapper;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.admin.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserMapper adminUserMapper;

    // ✅ Список пользователей
    @GetMapping
    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        adminUserService.getAllUsers()
                                .stream()
                                .map(adminUserMapper::toAdminDto)
                                .toList()
                )
        );
    }

    // ✅ Создание пользователя админом
    @PostMapping
    @RateLimit(capacity = 5, refillTokens = 5, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody CreateUserByAdminDto dto
    ) {
        adminUserService.createUser(dto);
        return ResponseEntity.ok(
                ApiResponse.okMessage("User created")
        );
    }

    // ✅ Блокировка
    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<Void>> block(@PathVariable @Positive Long id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok(ApiResponse.okMessage("User blocked"));
    }

    // ✅ Разблокировка
    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblock(@PathVariable @Positive Long id) {
        adminUserService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.okMessage("User unblocked"));
    }

    // ✅ Смена роли
    @PostMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateUserRoleDto dto
    ) {
        adminUserService.changeRole(id, dto.getRole());
        return ResponseEntity.ok(ApiResponse.okMessage("Role updated"));
    }
}
