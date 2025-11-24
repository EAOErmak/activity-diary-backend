package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import com.example.activity_diary.entity.dict.WhatDict;
import com.example.activity_diary.entity.dict.WhatHappenedDict;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DictionaryResolver {

    private final DictionaryCache cache;

    // ===================== WHAT HAPPENED =====================

    public WhatHappenedDict getWhatHappened(Long id) {
        if (id == null)
            throw new BadRequestException("whatHappenedId is required");

        WhatHappenedDict d = cache.getWhatHappened(id);
        if (d == null)
            throw new NotFoundException("WhatHappened not found: id=" + id);

        if (Boolean.FALSE.equals(d.getIsActive()))
            throw new BadRequestException("WhatHappened is inactive: id=" + id);

        return d;
    }

    // ===================== WHAT =====================

    public WhatDict getWhat(Long id) {
        if (id == null)
            throw new BadRequestException("whatId is required");

        WhatDict d = cache.getWhat(id);
        if (d == null)
            throw new NotFoundException("What not found: id=" + id);

        if (Boolean.FALSE.equals(d.getIsActive()))
            throw new BadRequestException("What is inactive: id=" + id);

        return d;
    }

    // ===================== ACTIVITY ITEM NAME =====================

    public ActivityItemNameDict getItemName(Long id) {
        if (id == null)
            throw new BadRequestException("dictNameId is required");

        ActivityItemNameDict d = cache.getItemName(id);
        if (d == null)
            throw new NotFoundException("ActivityItem name not found: id=" + id);

        if (Boolean.FALSE.equals(d.getIsActive()))
            throw new BadRequestException("ActivityItem name is inactive: id=" + id);

        return d;
    }

    // ===================== UNIT =====================

    public ActivityItemUnitDict getUnit(Long id) {
        if (id == null)
            throw new BadRequestException("dictUnitId is required");

        ActivityItemUnitDict d = cache.getUnit(id);
        if (d == null)
            throw new NotFoundException("ActivityItem unit not found: id=" + id);

        if (Boolean.FALSE.equals(d.getIsActive()))
            throw new BadRequestException("ActivityItem unit is inactive: id=" + id);

        return d;
    }
}
