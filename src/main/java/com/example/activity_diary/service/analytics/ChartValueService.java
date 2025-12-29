package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.DiaryEntry;

public interface ChartValueService {


     //Возвращает ЧИСЛО для одной DiaryEntry по её ChartType
    Double calculateY(DiaryEntry entry);
}
