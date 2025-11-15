package com.example.activity_diary.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // хранить в виде хэша BCrypt

    @Column(nullable = false)
    private boolean enabled = false; // подтверждение по email

    @Column(length = 500)
    private String refreshToken;

    private String fullName;

    // NEW: chatId and verifyToken
    private Long chatId;

    @Column(unique = true)
    private String verifyToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<DiaryEntry> diaryEntries = new ArrayList<>();
}
