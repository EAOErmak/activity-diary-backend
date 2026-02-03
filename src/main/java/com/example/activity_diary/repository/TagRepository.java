package com.example.activity_diary.repository;

import com.example.activity_diary.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Tag> findByNameIn(LinkedHashSet<String> names);
}

