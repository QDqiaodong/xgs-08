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
    List<CareLog> findTop1ByUserIdOrderByLogDateDesc(Long userId);
    List<CareLog> findByBonsaiIdOrderByLogDateDesc(Long bonsaiId);
    List<CareLog> findByBonsaiIdAndLogTypeOrderByLogDateDesc(Long bonsaiId, String logType);
    List<CareLog> findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(Long bonsaiId, String logType);
    List<CareLog> findByUserIdAndBonsaiIdIsNotNullOrderByLogDateAsc(Long userId);
}
