package com.bonsai.service;

import com.bonsai.entity.CareLog;
import com.bonsai.repository.CareLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private static final Set<String> VALID_LOG_TYPES = Set.of(
            "water", "fertilize", "prune", "repot", "other"
    );

    private final CareLogRepository careLogRepository;

    public Page<CareLog> getUserCareLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return careLogRepository.findByUserIdOrderByLogDateDesc(userId, pageable);
    }

    public List<CareLog> getPostCareLogs(Long postId) {
        return careLogRepository.findByPostIdOrderByLogDateDesc(postId);
    }

    public CareLog createCareLog(CareLog careLog) {
        validateLogType(careLog.getLogType());
        return careLogRepository.save(careLog);
    }

    private void validateLogType(String logType) {
        if (logType == null || !VALID_LOG_TYPES.contains(logType)) {
            throw new IllegalArgumentException(
                    "无效的养护类型: " + logType + "。允许的类型: " + String.join(", ", VALID_LOG_TYPES)
            );
        }
    }
}
