package com.bonsai.repository;

import com.bonsai.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    Page<Post> findByStatusAndIsHotOrderByCreatedAtDesc(Integer status, Integer isHot, Pageable pageable);

    Page<Post> findBySpeciesIdAndStatusOrderByCreatedAtDesc(Long speciesId, Integer status, Pageable pageable);

    Page<Post> findByStyleIdAndStatusOrderByCreatedAtDesc(Long styleId, Integer status, Pageable pageable);

    Page<Post> findBySpeciesIdAndStyleIdAndStatusOrderByCreatedAtDesc(Long speciesId, Long styleId, Integer status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.isHot = 1 ORDER BY p.viewCount DESC, p.likeCount DESC")
    List<Post> findHotPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.userId = :userId ORDER BY p.createdAt DESC")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
