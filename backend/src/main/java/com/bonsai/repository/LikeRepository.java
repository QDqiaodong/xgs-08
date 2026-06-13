package com.bonsai.repository;

import com.bonsai.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
