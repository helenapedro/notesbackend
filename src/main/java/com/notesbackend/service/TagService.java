package com.notesbackend.service;

import com.notesbackend.model.Tag;
import com.notesbackend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

// This service will manage tags, including creating tags and linking them to notes.
@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public Tag createTag(String tagName) {
        Tag tag = new Tag();
        tag.setName(tagName);
        return tagRepository.save(tag);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}