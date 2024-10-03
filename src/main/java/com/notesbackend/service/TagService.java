package com.notesbackend.service;

import com.notesbackend.dto.TagDto;
import com.notesbackend.model.Tag;
import com.notesbackend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public Tag createTag(String tagName) {
        if (tagRepository.findByName(tagName).isPresent()) {
            throw new IllegalArgumentException("Tag already exists.");
        }
        Tag tag = new Tag();
        tag.setName(tagName);
        return tagRepository.save(tag);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public TagDto convertToDto(Tag tag) {
        return new TagDto(tag.getTid(), tag.getName());
    }

    public List<TagDto> convertToDtoList(List<Tag> tags) {
        return tags.stream()
                   .map(this::convertToDto)
                   .collect(Collectors.toList());
    }
}
