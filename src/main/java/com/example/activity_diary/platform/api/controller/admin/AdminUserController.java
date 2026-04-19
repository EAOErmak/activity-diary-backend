package com.example.activity_diary.platform.api.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.AdminUserDto;
import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.dto.admin.UpdateUserEnabledDto;
import com.example.activity_diary.dto.admin.UpdateUserLockDto;
import com.example.activity_diary.dto.admin.UpdateUserRoleDto;
import com.example.activity_diary.dto.mapper.AdminUserMapper;
import com.example.activity_diary.service.admin.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserMapper adminUserMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAll() {
        log.info("Admin users requested");
        return ResponseEntity.ok(
                ApiResponse.ok(
                        adminUserService.getAllUsers()
                                .stream()
                                .map(adminUserMapper::toAdminDto)
                                .toList()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserDto>> create(
            @Valid @RequestBody CreateUserByAdminDto dto
    ) {
        AdminUserDto createdUser = adminUserMapper.toAdminDto(adminUserService.createUser(dto));
        log.info("Admin user created: id={}, username={}, role={}", createdUser.getId(), createdUser.getUsername(), createdUser.getRole());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User created", createdUser)
        );
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<AdminUserDto>> block(
            @PathVariable @Positive Long id,
            @RequestBody(required = false) UpdateUserEnabledDto dto
    ) {
        boolean enabled = dto != null && dto.getEnabled() != null ? dto.getEnabled() : false;
        AdminUserDto updatedUser = adminUserMapper.toAdminDto(adminUserService.updateEnabled(id, enabled));
        log.info("Admin user enabled state updated: id={}, enabled={}", id, enabled);
        return ResponseEntity.ok(
                new ApiResponse<>(true, enabled ? "User enabled" : "User disabled", updatedUser)
        );
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<AdminUserDto>> unblock(
            @PathVariable @Positive Long id,
            @RequestBody(required = false) UpdateUserLockDto dto
    ) {
        boolean locked = dto != null && dto.getLocked() != null && dto.getLocked();
        AdminUserDto updatedUser = adminUserMapper.toAdminDto(adminUserService.updateLock(id, locked));
        log.info("Admin user lock state updated: id={}, locked={}", id, locked);
        return ResponseEntity.ok(
                new ApiResponse<>(true, locked ? "User locked" : "User unlocked", updatedUser)
        );
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserDto>> changeRole(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateUserRoleDto dto
    ) {
        AdminUserDto updatedUser = adminUserMapper.toAdminDto(adminUserService.changeRole(id, dto.getRole()));
        log.info("Admin user role updated: id={}, role={}", id, updatedUser.getRole());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Role updated", updatedUser)
        );
    }
}
