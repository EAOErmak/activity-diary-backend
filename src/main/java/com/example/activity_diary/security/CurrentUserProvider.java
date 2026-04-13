package com.example.activity_diary.security;

import com.example.activity_diary.entity.User;

public interface CurrentUserProvider {

    User getCurrentUser();

    Long getCurrentUserId();
}
