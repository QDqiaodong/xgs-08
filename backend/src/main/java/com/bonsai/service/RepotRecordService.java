package com.bonsai.service;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.RepotRecord;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.RepotRecordRepository;
import com.bonsai.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepotRecordService {

    private final RepotRecordRepository repotRecordRepository;
    private final BonsaiRepository bonsaiRepository;
    private final ImageUtil imageUtil;

    public List<RepotRecord> getBonsaiRepotRecords(Long bonsaiId) {
        return repotRecordRepository.findByBonsaiIdOrderByRepotDateDesc(bonsaiId);
    }

    public List<RepotRecord> getUserRepotRecords(Long userId) {
        return repotRecordRepository.findByUserIdOrderByRepotDateDesc(userId);
    }

    public Page<RepotRecord> getUserRepotRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repotRecordRepository.findByUserIdOrderByRepotDateDesc(userId, pageable);
    }

    public RepotRecord getRepotRecordById(Long id) {
        return repotRecordRepository.findById(id).orElse(null);
    }

    @Transactional
    public RepotRecord createRepotRecord(RepotRecord record) {
        validateRepotRecord(record);

        Bonsai bonsai = bonsaiRepository.findById(record.getBonsaiId()).orElse(null);
        if (bonsai == null) {
            throw new IllegalArgumentException("盆景不存在");
        }

        if (record.getNewPot() != null && !record.getNewPot().trim().isEmpty()) {
            bonsai.setPotType(record.getNewPot());
            bonsaiRepository.save(bonsai);
        }

        return repotRecordRepository.save(record);
    }

    @Transactional
    public RepotRecord updateRepotRecord(RepotRecord record) {
        if (record.getId() == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }

        RepotRecord existing = repotRecordRepository.findById(record.getId()).orElse(null);
        if (existing == null) {
            throw new IllegalArgumentException("记录不存在");
        }

        validateRepotRecord(record);

        if (record.getImages() != null && !record.getImages().equals(existing.getImages())) {
            imageUtil.deleteImages(existing.getImages());
        }
        if (record.getBeforeRootImages() != null && !record.getBeforeRootImages().equals(existing.getBeforeRootImages())) {
            imageUtil.deleteImages(existing.getBeforeRootImages());
        }
        if (record.getAfterRootImages() != null && !record.getAfterRootImages().equals(existing.getAfterRootImages())) {
            imageUtil.deleteImages(existing.getAfterRootImages());
        }

        if (record.getRepotDate() != null) {
            existing.setRepotDate(record.getRepotDate());
        }
        if (record.getSoilType() != null) {
            existing.setSoilType(record.getSoilType());
        }
        if (record.getRootPruning() != null) {
            existing.setRootPruning(record.getRootPruning());
        }
        if (record.getOldPot() != null) {
            existing.setOldPot(record.getOldPot());
        }
        if (record.getNewPot() != null) {
            existing.setNewPot(record.getNewPot());
        }
        if (record.getImages() != null) {
            existing.setImages(record.getImages());
        }
        if (record.getBeforeRootImages() != null) {
            existing.setBeforeRootImages(record.getBeforeRootImages());
        }
        if (record.getAfterRootImages() != null) {
            existing.setAfterRootImages(record.getAfterRootImages());
        }
        if (record.getTitle() != null) {
            existing.setTitle(record.getTitle());
        }
        if (record.getNotes() != null) {
            existing.setNotes(record.getNotes());
        }

        return repotRecordRepository.save(existing);
    }

    @Transactional
    public void deleteRepotRecord(Long id) {
        RepotRecord record = repotRecordRepository.findById(id).orElse(null);
        if (record != null) {
            imageUtil.deleteImages(record.getImages());
            imageUtil.deleteImages(record.getBeforeRootImages());
            imageUtil.deleteImages(record.getAfterRootImages());
            repotRecordRepository.deleteById(id);
        }
    }

    @Transactional
    public void deleteByBonsaiId(Long bonsaiId) {
        List<RepotRecord> records = repotRecordRepository.findByBonsaiIdOrderByRepotDateDesc(bonsaiId);
        for (RepotRecord record : records) {
            imageUtil.deleteImages(record.getImages());
            imageUtil.deleteImages(record.getBeforeRootImages());
            imageUtil.deleteImages(record.getAfterRootImages());
        }
        repotRecordRepository.deleteByBonsaiId(bonsaiId);
    }

    private void validateRepotRecord(RepotRecord record) {
        if (record.getUserId() == null || record.getUserId() <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        if (record.getBonsaiId() == null || record.getBonsaiId() <= 0) {
            throw new IllegalArgumentException("盆景ID不能为空");
        }
        if (record.getRepotDate() == null) {
            throw new IllegalArgumentException("换盆日期不能为空");
        }
        if (record.getRepotDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("换盆日期不能是未来日期");
        }
    }
}
