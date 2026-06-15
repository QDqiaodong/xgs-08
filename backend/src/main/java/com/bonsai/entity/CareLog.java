package com.bonsai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "care_logs")
public class CareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id")
    private Long postId;

    @NotBlank(message = "养护类型不能为空")
    @Size(max = 50, message = "养护类型长度不能超过50个字符")
    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(length = 200)
    private String title;

    @NotBlank(message = "养护内容不能为空")
    @Size(max = 5000, message = "养护内容长度不能超过5000个字符")
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "养护日期不能为空")
    @PastOrPresent(message = "养护日期不能是未来日期")
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Size(max = 2000, message = "图片数据长度不能超过2000个字符")
    @Column(length = 500)
    private String images;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
