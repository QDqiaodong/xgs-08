package com.bonsai.service;

import com.bonsai.entity.CareLog;
import com.bonsai.repository.CareLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private final CareLogRepository careLogRepository;

    public Page<CareLog> getUserCareLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return careLogRepository.findByUserIdOrderByLogDateDesc(userId, pageable);
    }

    public List<CareLog> getPostCareLogs(Long postId) {
        return careLogRepository.findByPostIdOrderByLogDateDesc(postId);
    }

    public CareLog createCareLog(CareLog careLog) {
        return careLogRepository.save(careLog);
    }
}
