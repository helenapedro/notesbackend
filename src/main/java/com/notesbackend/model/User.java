package com.notesbackend.model;

import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    private String password;
    private String firstname;
    private String lastname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Date birthday;
    private String gender;
    private String phoneNumber;
    private String address;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "uid"),
        inverseJoinColumns = @JoinColumn(name = "rid")
    )
    private List<Role> roles;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.email = this.email != null ? this.email.toLowerCase() : null;
        if (createdAt == null) {
            createdAt = LocalDateTime.now(); 
        }
        updatedAt = LocalDateTime.now(); 
    }
}