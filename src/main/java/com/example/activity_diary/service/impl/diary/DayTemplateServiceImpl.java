package com.example.activity_diary.service.impl.diary;


import com.example.activity_diary.dto.mapper.DayTemplateMapper;
import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.day.DayTemplateUpdateDto;
import com.example.activity_diary.dto.template.day.DayTemplateViewDto;
import com.example.activity_diary.dto.template.day.DayTemplateItemDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.template.DayTemplateRepository;
import com.example.activity_diary.service.diary.DayTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DayTemplateServiceImpl implements DayTemplateService {

    private final DayTemplateRepository dayTemplateRepository;
    private final DiaryEntryTemplateRepository entryTemplateRepository;

    private final DayTemplateMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DayTemplateViewDto> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return dayTemplateRepository.findAllByUser_Id(userId, pageable)
                .map(mapper::toView);
    }

    @Override
    public DayTemplateViewDto create(Long userId, DayTemplateCreateDto dto) {
        String name = normalizeName(dto.getName());
        if (dayTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
            throw new BadRequestException("Day template name already exists");
        }

        DayTemplate tpl = DayTemplate.builder()
                .user(refUser(userId))
                .name(name)
                .build();

        applyItemsReplace(userId, tpl, dto.getItems());

        DayTemplate saved = dayTemplateRepository.save(tpl);
        return mapper.toView(saved);
    }

    @Override
    public DayTemplateViewDto update(Long userId, Long id, DayTemplateUpdateDto dto) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));

        if (dto.getName() != null) {
            String name = normalizeName(dto.getName());
            if (!name.equalsIgnoreCase(tpl.getName())
                    && dayTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
                throw new BadRequestException("Day template name already exists");
            }
            tpl.setName(name);
        }

        if (dto.getItems() != null) {
            applyItemsReplace(userId, tpl, dto.getItems());
        }

        return mapper.toView(tpl);
    }

    @Override
    @Transactional(readOnly = true)
    public DayTemplateViewDto get(Long userId, Long id) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));
        return mapper.toView(tpl);
    }

    @Override
    public void delete(Long userId, Long id) {
        DayTemplate tpl = dayTemplateRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Day template not found"));
        dayTemplateRepository.delete(tpl);
    }

    private void applyItemsReplace(Long userId, DayTemplate tpl, List<DayTemplateItemDto> items) {
        // 1) РѕС‡РёСЃС‚РёС‚СЊ СЃС‚Р°СЂС‹Рµ items
        tpl.getItems().clear();

        if (items == null || items.isEmpty()) return;

        // 2) РІР°Р»РёРґР°С†РёСЏ positions (СѓРЅРёРєР°Р»СЊРЅС‹Рµ Рё >0)
        Set<Integer> posSet = new HashSet<>();
        for (var it : items) {
            if (it.getEntryTemplateId() == null) throw new BadRequestException("entryTemplateId is required");
            if (it.getPosition() == null || it.getPosition() < 1) throw new BadRequestException("position must be >= 1");
            if (!posSet.add(it.getPosition())) throw new BadRequestException("duplicate position in day template");
        }

        // 3) Р·Р°РіСЂСѓР·РёС‚СЊ РІСЃРµ DiaryEntryTemplate РѕРґРЅРёРј Р·Р°РїСЂРѕСЃРѕРј (Рё С‚РѕР»СЊРєРѕ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ)
        List<Long> ids = items.stream().map(DayTemplateItemDto::getEntryTemplateId).toList();
        Map<Long, DiaryEntryTemplate> byId = entryTemplateRepository
                .findAllByIdInAndUser_Id(ids, userId)
                .stream().collect(Collectors.toMap(BaseEntity::getId, x -> x));

        for (var it : items) {
            DiaryEntryTemplate entryTpl = byId.get(it.getEntryTemplateId());
            if (entryTpl == null) throw new BadRequestException("Entry template not found: " + it.getEntryTemplateId());

            TemplateEntryItem e = TemplateEntryItem.builder()
                    .dayTemplate(tpl)
                    .entryTemplate(entryTpl)
                    .position(it.getPosition())
                    .build();

            tpl.getItems().add(e);
        }
    }

    private static String normalizeName(String name) {
        String v = name == null ? null : name.trim();
        if (v == null || v.isBlank()) throw new BadRequestException("name is required");
        if (v.length() > 120) throw new BadRequestException("name is too long");
        return v;
    }

    private static User refUser(Long userId) {
        User u = new User();
        u.setId(userId);
        return u;
    }
}
