package com.bonsai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @NotBlank(message = "盆景名称不能为空")
    @Size(max = 100, message = "盆景名称长度不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "species_id")
    private Long speciesId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id", insertable = false, updatable = false)
    private Category species;

    @PastOrPresent(message = "入手日期不能是未来日期")
    @Column(name = "acquire_date")
    private LocalDate acquireDate;

    @Min(value = 0, message = "树龄不能为负数")
    @Max(value = 10000, message = "树龄值过大")
    @Column(name = "tree_age")
    private Integer treeAge;

    @Size(max = 100, message = "盆器类型长度不能超过100个字符")
    @Column(length = 100)
    private String potType;

    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    @Column(length = 500)
    private String coverImage;

    @Size(max = 2000, message = "简介长度不能超过2000个字符")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 200, message = "干型描述长度不能超过200个字符")
    @Column(name = "trunk_shape", length = 200)
    private String trunkShape;

    @Size(max = 200, message = "枝托描述长度不能超过200个字符")
    @Column(name = "branch_support", length = 200)
    private String branchSupport;

    @Size(max = 200, message = "冠幅描述长度不能超过200个字符")
    @Column(name = "crown_width", length = 200)
    private String crownWidth;

    @Size(max = 200, message = "盆面描述长度不能超过200个字符")
    @Column(name = "pot_surface", length = 200)
    private String potSurface;

    @Min(value = 0, message = "浏览次数不能为负数")
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
