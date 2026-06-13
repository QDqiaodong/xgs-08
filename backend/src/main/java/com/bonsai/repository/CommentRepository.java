package com.bonsai.repository;

import com.bonsai.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(Long postId, Integer status, Pageable pageable);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByPostIdAndStatus(Long postId, Integer status);
}
