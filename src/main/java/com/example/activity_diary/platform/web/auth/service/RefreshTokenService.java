package com.example.activity_diary.platform.web.auth.service;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;

public interface RefreshTokenService {

    void save(User user, String rawToken);

    RefreshToken verify(String rawToken);

    RefreshToken rotate(RefreshToken token, String rawReplacementToken);

    void revoke(RefreshToken token);

    void revokeByToken(String rawToken);

    void revokeAllByUser(User user);
}
