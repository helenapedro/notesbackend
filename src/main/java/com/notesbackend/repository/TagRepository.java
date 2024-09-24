package com.notesbackend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
