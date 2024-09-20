package com.notesbackend.service;

import com.notesbackend.model.Collaborator;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.repository.CollaboratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// this service allows adding or removing collaborators with specific permissions
@Service
public class CollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    public Collaborator addCollaborator(Note note, User user, String permissionLevel) {
        Collaborator collaborator = new Collaborator();
        collaborator.setNote(note);
        collaborator.setUser(user);
        collaborator.setPermissionLevel(permissionLevel);
        return collaboratorRepository.save(collaborator);
    }

    public List<Collaborator> getCollaboratorsForNote(Long noteId) {
        return collaboratorRepository.findByNoteNid(noteId);
    }

    public void removeCollaborator(Long collaboratorId) {
        collaboratorRepository.deleteById(collaboratorId);
    }
}
