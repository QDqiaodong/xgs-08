package com.bonsai.repository;

import com.bonsai.entity.CareLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLogRepository extends JpaRepository<CareLog, Long> {
    Page<CareLog> findByUserIdOrderByLogDateDesc(Long userId, Pageable pageable);
    List<CareLog> findByPostIdOrderByLogDateDesc(Long postId);
}
