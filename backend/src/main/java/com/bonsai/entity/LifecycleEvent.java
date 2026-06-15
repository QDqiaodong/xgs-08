package com.bonsai.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lifecycle_events")
public class LifecycleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bonsai_id", nullable = false)
    private Long bonsaiId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bonsai_id", insertable = false, updatable = false)
    private Bonsai bonsai;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(length = 1000)
    private String images;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
