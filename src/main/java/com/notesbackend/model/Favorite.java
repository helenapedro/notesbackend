package com.notesbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "favorites")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fav_id;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note;
}