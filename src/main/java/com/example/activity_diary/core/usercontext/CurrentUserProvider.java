package com.example.activity_diary.core.usercontext;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.Role;

public interface CurrentUserProvider {

    User getCurrentUser();

    Long getCurrentUserId();

    Role getCurrentUserRole();
}
