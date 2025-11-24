package com.example.activity_diary.service.auth;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken create(User user);

    RefreshToken verifyAndRotate(String token);

    void revoke(RefreshToken token);
}
