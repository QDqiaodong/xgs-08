package com.bonsai.service;

import com.bonsai.entity.Bonsai;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.LifecycleEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BonsaiService {

    private final BonsaiRepository bonsaiRepository;
    private final LifecycleEventRepository lifecycleEventRepository;

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
        return bonsaiRepository.save(bonsai);
    }

    public Bonsai updateBonsai(Bonsai bonsai) {
        return bonsaiRepository.save(bonsai);
    }

    @Transactional
    public void deleteBonsai(Long id) {
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
}
