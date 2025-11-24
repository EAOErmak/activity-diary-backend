package com.example.activity_diary.dto.dictionary;

import lombok.Data;

import java.time.Instant;

@Data
public class DictionaryResponseDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private Long parentId;        // only for WhatDict
    private Instant createdAt;
    private Instant updatedAt;
}
