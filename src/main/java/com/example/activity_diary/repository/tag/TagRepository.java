package com.example.activity_diary.repository.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.activity_diary.entity.diary.Tag;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    @Query("""
        select t from Tag t
        where t.status <> 'REJECTED'
        and (
          t.status = 'APPROVED'
          or (t.status = 'PENDING' and t.createdBy.id = :userId)
        )
        and lower(t.name) like lower(concat('%', :q, '%'))
          order by
          case when t.status = 'APPROVED' then 0 else 1 end,
        t.name
    """)
    List<Tag> searchVisible(@Param("userId") Long userId, @Param("q") String query);


    @Query("""
          select t from Tag t
          where t.status <> 'REJECTED'
            and (
              t.status = 'APPROVED'
              or (t.status = 'PENDING' and t.createdBy.id = :userId)
            )
          order by
            case when t.status = 'APPROVED' then 0 else 1 end,
            t.name
    """)
    List<Tag> findAllVisible(@Param("userId") Long userId);

    @Query("""
        select t from Tag t
        order by
          case
            when t.status = com.example.activity_diary.entity.enums.TagStatus.PENDING then 0
            when t.status = com.example.activity_diary.entity.enums.TagStatus.APPROVED then 1
            else 2
          end,
          t.name,
          t.id
    """)
    Slice<Tag> findAllSlice(Pageable pageable);

    @Query("""
        select t from Tag t
        where lower(t.name) like concat('%', :q, '%')
        order by
          case
            when t.status = com.example.activity_diary.entity.enums.TagStatus.PENDING then 0
            when t.status = com.example.activity_diary.entity.enums.TagStatus.APPROVED then 1
            else 2
          end,
          t.name,
          t.id
    """)
    Slice<Tag> searchSlice(@Param("q") String q, Pageable pageable);

    List<Tag> findByNameIn(LinkedHashSet<String> names);
}

