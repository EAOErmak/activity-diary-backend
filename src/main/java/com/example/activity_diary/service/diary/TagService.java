package com.example.activity_diary.service.diary;

public interface TagService {

    void approve(Long tagId);

    void reject(Long tagId);

    void deprecate(Long tagId);
}
