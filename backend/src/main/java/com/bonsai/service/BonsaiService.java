package com.bonsai.service;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.LifecycleEvent;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.LifecycleEventRepository;
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
public class BonsaiService {

    private final BonsaiRepository bonsaiRepository;
    private final LifecycleEventRepository lifecycleEventRepository;
    private final ImageUtil imageUtil;

    public Page<Bonsai> getUserBonsais(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Bonsai> getUserBonsaiList(Long userId) {
        return bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Bonsai getBonsaiById(Long id) {
        return bonsaiRepository.findById(id).orElse(null);
    }

    public Bonsai createBonsai(Bonsai bonsai) {
        validateBonsai(bonsai);
        return bonsaiRepository.save(bonsai);
    }

    public Bonsai updateBonsai(Bonsai bonsai) {
        validateBonsai(bonsai);
        return bonsaiRepository.save(bonsai);
    }

    @Transactional
    public void deleteBonsai(Long id) {
        Bonsai bonsai = bonsaiRepository.findById(id).orElse(null);
        if (bonsai == null) {
            return;
        }

        List<LifecycleEvent> events = lifecycleEventRepository.findByBonsaiIdOrderByEventDateAsc(id);

        imageUtil.deleteImages(bonsai.getCoverImage());

        for (LifecycleEvent event : events) {
            imageUtil.deleteImages(event.getImages());
            imageUtil.deleteImages(event.getBeforeImages());
        }

        lifecycleEventRepository.deleteByBonsaiId(id);
        bonsaiRepository.deleteById(id);
    }

    public Bonsai incrementViewCount(Long id) {
        Bonsai bonsai = bonsaiRepository.findById(id).orElse(null);
        if (bonsai != null) {
            bonsai.setViewCount(bonsai.getViewCount() + 1);
            return bonsaiRepository.save(bonsai);
        }
        return null;
    }

    private void validateBonsai(Bonsai bonsai) {
        if (bonsai.getUserId() == null || bonsai.getUserId() <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        if (bonsai.getName() == null || bonsai.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("盆景名称不能为空");
        }
        if (bonsai.getTreeAge() != null && bonsai.getTreeAge() < 0) {
            throw new IllegalArgumentException("树龄不能为负数");
        }
        if (bonsai.getAcquireDate() != null && bonsai.getAcquireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("入手日期不能是未来日期");
        }
        if (bonsai.getViewCount() == null) {
            bonsai.setViewCount(0);
        }
        if (bonsai.getViewCount() < 0) {
            throw new IllegalArgumentException("浏览次数不能为负数");
        }
    }
}
