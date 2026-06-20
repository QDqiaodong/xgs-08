package com.bonsai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "repot_records")
public class RepotRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "盆景ID不能为空")
    @Column(name = "bonsai_id", nullable = false)
    private Long bonsaiId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bonsai_id", insertable = false, updatable = false)
    private Bonsai bonsai;

    @NotNull(message = "换盆日期不能为空")
    @PastOrPresent(message = "换盆日期不能是未来日期")
    @Column(name = "repot_date", nullable = false)
    private LocalDate repotDate;

    @Size(max = 200, message = "用土描述长度不能超过200个字符")
    @Column(name = "soil_type", length = 200)
    private String soilType;

    @Size(max = 1000, message = "修根情况描述长度不能超过1000个字符")
    @Column(name = "root_pruning", columnDefinition = "TEXT")
    private String rootPruning;

    @Size(max = 200, message = "原盆器描述长度不能超过200个字符")
    @Column(name = "old_pot", length = 200)
    private String oldPot;

    @Size(max = 200, message = "新盆器描述长度不能超过200个字符")
    @Column(name = "new_pot", length = 200)
    private String newPot;

    @Size(max = 2000, message = "图片数据长度不能超过2000个字符")
    @Column(length = 500)
    private String images;

    @Size(max = 2000, message = "根系处理前图片长度不能超过2000个字符")
    @Column(name = "before_root_images", length = 500)
    private String beforeRootImages;

    @Size(max = 2000, message = "根系处理后图片长度不能超过2000个字符")
    @Column(name = "after_root_images", length = 500)
    private String afterRootImages;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(length = 200)
    private String title;

    @Size(max = 5000, message = "备注内容长度不能超过5000个字符")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
