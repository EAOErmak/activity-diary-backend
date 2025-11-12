package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diary_entry")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiaryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    @Size(max = 50)
    private String whatHappened;

    @Column(length = 50)
    @Size(max = 50)
    private String what;

    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;
    private Integer duration; // minutes

    @OneToMany(mappedBy = "diaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ActivityItem> whatDidYouDo = new ArrayList<>();

    @Min(1)
    @Max(5)
    private Short howYouWereFeeling;

    @Column(length = 1000)
    @Size(max = 1000)
    private String anyDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status = EntryStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
