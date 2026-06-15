package com.bonsai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "盆景ID不能为空")
    @Column(name = "bonsai_id", nullable = false)
    private Long bonsaiId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bonsai_id", insertable = false, updatable = false)
    private Bonsai bonsai;

    @NotBlank(message = "事件类型不能为空")
    @Size(max = 50, message = "事件类型长度不能超过50个字符")
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(length = 200)
    private String title;

    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "事件日期不能为空")
    @PastOrPresent(message = "事件日期不能是未来日期")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Size(max = 2000, message = "图片数据长度不能超过2000个字符")
    @Column(length = 1000)
    private String images;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
