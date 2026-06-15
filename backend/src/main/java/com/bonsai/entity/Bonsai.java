package com.bonsai.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bonsais")
public class Bonsai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "species_id")
    private Long speciesId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id", insertable = false, updatable = false)
    private Category species;

    @Column(name = "acquire_date")
    private LocalDate acquireDate;

    @Column(name = "tree_age")
    private Integer treeAge;

    @Column(length = 100)
    private String potType;

    @Column(length = 500)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
