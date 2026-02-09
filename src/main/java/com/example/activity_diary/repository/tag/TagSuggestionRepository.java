package com.example.activity_diary.repository.tag;

import com.example.activity_diary.entity.TagSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagSuggestionRepository extends JpaRepository<TagSuggestion, Long> {
    Optional<TagSuggestion> findByTagName(String tagName);
}
