package com.smartshift.smartshift_backend.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWTs are long, so we tell PostgreSQL to use the TEXT data type instead of VARCHAR(255)
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    // The Kill Switch
    @Column(nullable = false)
    private boolean revoked;

    // The One-To-Many Relationship back to the User table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}