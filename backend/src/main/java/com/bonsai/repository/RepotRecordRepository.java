package com.bonsai.repository;

import com.bonsai.entity.RepotRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepotRecordRepository extends JpaRepository<RepotRecord, Long> {

    List<RepotRecord> findByBonsaiIdOrderByRepotDateDesc(Long bonsaiId);

    List<RepotRecord> findByUserIdOrderByRepotDateDesc(Long userId);

    Page<RepotRecord> findByUserIdOrderByRepotDateDesc(Long userId, Pageable pageable);

    List<RepotRecord> findTop1ByBonsaiIdOrderByRepotDateDesc(Long bonsaiId);

    void deleteByBonsaiId(Long bonsaiId);
}
