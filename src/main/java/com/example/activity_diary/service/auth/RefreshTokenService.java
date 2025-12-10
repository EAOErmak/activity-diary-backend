package com.example.activity_diary.service.auth;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;

public interface RefreshTokenService {

    // ✅ Сохранение нового refresh-токена
    void save(User user, String rawToken);

    // ✅ Проверка refresh-токена (без ротации)
    RefreshToken verify(String rawToken);

    // ✅ Отзыв конкретного refresh-токена
    void revoke(RefreshToken token);

    // ✅ Отзыв по сырому значению (logout)
    void revokeByToken(String rawToken);

    // ✅ Отзыв ВСЕХ токенов пользователя (security logout-all)
    void revokeAllByUser(User user);
}
