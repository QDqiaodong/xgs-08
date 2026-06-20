package com.bonsai.repository;

import com.bonsai.entity.BonsaiStageImage;
import com.bonsai.entity.BonsaiStageImage.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonsaiStageImageRepository extends JpaRepository<BonsaiStageImage, Long> {

    List<BonsaiStageImage> findByBonsaiIdOrderByStageAscSortOrderAsc(Long bonsaiId);

    List<BonsaiStageImage> findByBonsaiIdAndStageOrderBySortOrderAsc(Long bonsaiId, Stage stage);

    void deleteByBonsaiId(Long bonsaiId);

    void deleteByBonsaiIdAndStage(Long bonsaiId, Stage stage);
}
