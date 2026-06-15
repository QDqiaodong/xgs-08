package com.bonsai.repository;

import com.bonsai.entity.Bonsai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonsaiRepository extends JpaRepository<Bonsai, Long> {
    Page<Bonsai> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Bonsai> findByUserIdOrderByCreatedAtDesc(Long userId);
}
