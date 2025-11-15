package com.example.activity_diary.controller;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.service.DiaryService;
import com.example.activity_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<DiaryEntry>> getAll() {
        return ResponseEntity.ok(diaryService.getAll());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DiaryEntry>> myEntries(@AuthenticationPrincipal UserDetails ud) {
        var opt = userService.findByEmail(ud.getUsername());
        if (opt.isEmpty()) return ResponseEntity.status(401).build();
        var list = diaryService.getByUserId(opt.get().getId());
        return ResponseEntity.ok(list);
    }

    // ✅ Получить запись по ID
    @GetMapping("/{id}")
    public DiaryEntry getById(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud) {
        return diaryService.getByIdForUser(id, ud);
    }

    @PostMapping
    public ResponseEntity<DiaryEntry> create(@Valid @RequestBody DiaryEntry entry,
                                             @AuthenticationPrincipal UserDetails ud) {
        var user = userService.findByEmail(ud.getUsername()).orElseThrow();
        entry.setUser(user);
        DiaryEntry created = diaryService.create(entry);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryEntry> update(@PathVariable Long id,
                                             @RequestBody DiaryEntry entry,
                                             @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(diaryService.update(id, entry, ud));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails ud) {
        diaryService.delete(id, ud);
        return ResponseEntity.noContent().build();
    }
}
