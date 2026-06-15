package com.bonsai.repository;

import com.bonsai.entity.LifecycleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LifecycleEventRepository extends JpaRepository<LifecycleEvent, Long> {
    List<LifecycleEvent> findByBonsaiIdOrderByEventDateAsc(Long bonsaiId);
    List<LifecycleEvent> findByBonsaiIdOrderByEventDateDesc(Long bonsaiId);
    void deleteByBonsaiId(Long bonsaiId);
}
