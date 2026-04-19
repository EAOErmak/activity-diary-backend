package com.example.activity_diary.core.usercontext;

import com.example.activity_diary.entity.User;

public interface CurrentUserProvider {

    User getCurrentUser();

    Long getCurrentUserId();
}
