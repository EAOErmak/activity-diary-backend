package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.base.BaseDictionary;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "what_happened_dict")
@Getter
@Setter
@NoArgsConstructor
public class WhatHappenedDict extends BaseDictionary {
}

