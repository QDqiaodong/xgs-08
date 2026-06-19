package com.bonsai.service;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.CareLog;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.CareLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private static final Set<String> VALID_LOG_TYPES = Set.of(
            "water", "fertilize", "prune", "repot", "other"
    );

    private static final int MAX_FUTURE_DAYS = 30;

    private final CareLogRepository careLogRepository;
    private final BonsaiRepository bonsaiRepository;

    public Page<CareLog> getUserCareLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return careLogRepository.findByUserIdOrderByLogDateDesc(userId, pageable);
    }

    public List<CareLog> getPostCareLogs(Long postId) {
        return careLogRepository.findByPostIdOrderByLogDateDesc(postId);
    }

    public List<CareLog> getBonsaiCareLogs(Long bonsaiId) {
        return careLogRepository.findByBonsaiIdOrderByLogDateDesc(bonsaiId);
    }

    public CareLog createCareLog(CareLog careLog) {
        validateLogType(careLog.getLogType());
        validateCareLogDate(careLog);
        return careLogRepository.save(careLog);
    }

    private void validateCareLogDate(CareLog careLog) {
        LocalDate logDate = careLog.getLogDate();
        if (logDate == null) {
            throw new IllegalArgumentException("养护日期不能为空");
        }

        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusDays(MAX_FUTURE_DAYS);
        if (logDate.isAfter(maxFutureDate)) {
            throw new IllegalArgumentException(
                    "养护日期不能超过未来" + MAX_FUTURE_DAYS + "天（最晚允许：" + maxFutureDate + "）"
            );
        }

        if (careLog.getBonsaiId() != null) {
            Bonsai bonsai = bonsaiRepository.findById(careLog.getBonsaiId()).orElse(null);
            if (bonsai != null && bonsai.getAcquireDate() != null) {
                if (logDate.isBefore(bonsai.getAcquireDate())) {
                    throw new IllegalArgumentException(
                            "养护日期不能早于盆景入手日期（" + bonsai.getAcquireDate() + "）"
                    );
                }
            }
        }
    }

    private void validateLogType(String logType) {
        if (logType == null || !VALID_LOG_TYPES.contains(logType)) {
            throw new IllegalArgumentException(
                    "无效的养护类型: " + logType + "。允许的类型: " + String.join(", ", VALID_LOG_TYPES)
            );
        }
    }
}
