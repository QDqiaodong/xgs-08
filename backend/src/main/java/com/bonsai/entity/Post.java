package com.bonsai.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "species_id")
    private Long speciesId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id", insertable = false, updatable = false)
    private Category species;

    @Column(name = "style_id")
    private Long styleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "style_id", insertable = false, updatable = false)
    private Category style;

    @Column(name = "tree_age")
    private Integer treeAge;

    @Column(name = "pot_type", length = 100)
    private String potType;

    @Column(name = "care_points", columnDefinition = "TEXT")
    private String carePoints;

    @Column(name = "shaping_ideas", columnDefinition = "TEXT")
    private String shapingIdeas;

    @Column(name = "trunk_treatment", columnDefinition = "TEXT")
    private String trunkTreatment;

    @Column(name = "branch_selection", columnDefinition = "TEXT")
    private String branchSelection;

    @Column(name = "pot_layout", columnDefinition = "TEXT")
    private String potLayout;

    @Column(name = "future_direction", columnDefinition = "TEXT")
    private String futureDirection;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "is_hot")
    private Integer isHot = 0;

    private Integer status = 1;

    @OneToMany(mappedBy = "postId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PostImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
