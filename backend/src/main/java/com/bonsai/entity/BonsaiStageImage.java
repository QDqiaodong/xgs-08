package com.bonsai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bonsai_stage_images")
public class BonsaiStageImage {

    public enum Stage {
        MATERIAL_TREE("素材树"),
        PRUNING("修剪中"),
        AFTER_REPOT("换盆后"),
        CURRENT("当前状态");

        private final String displayName;

        Stage(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "盆景ID不能为空")
    @Column(name = "bonsai_id", nullable = false)
    private Long bonsaiId;

    @NotNull(message = "阶段不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Stage stage;

    @NotBlank(message = "图片URL不能为空")
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    @Column(length = 200)
    private String note;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
