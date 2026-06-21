package com.bonsai.service;

import com.bonsai.dto.SpeciesCareProfile;
import com.bonsai.entity.Bonsai;
import com.bonsai.entity.CareLog;
import com.bonsai.entity.Category;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.CareLogRepository;
import com.bonsai.repository.CategoryRepository;
import com.bonsai.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private static final Set<String> VALID_LOG_TYPES = Set.of(
            "water", "fertilize", "prune", "repot", "other"
    );

    private static final int MAX_FUTURE_DAYS = 30;

    private final CareLogRepository careLogRepository;
    private final BonsaiRepository bonsaiRepository;
    private final CategoryRepository categoryRepository;
    private final ImageUtil imageUtil;

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

    @Transactional
    public CareLog updateCareLog(CareLog careLog) {
        if (careLog.getId() == null) {
            throw new IllegalArgumentException("日志ID不能为空");
        }
        CareLog existingLog = careLogRepository.findById(careLog.getId()).orElse(null);
        if (existingLog == null) {
            throw new IllegalArgumentException("养护日志不存在");
        }

        validateLogType(careLog.getLogType());
        validateCareLogDate(careLog);

        if (careLog.getImages() != null && !careLog.getImages().equals(existingLog.getImages())) {
            imageUtil.deleteImages(existingLog.getImages());
        }

        if (careLog.getLogType() != null) {
            existingLog.setLogType(careLog.getLogType());
        }
        if (careLog.getTitle() != null) {
            existingLog.setTitle(careLog.getTitle());
        }
        if (careLog.getContent() != null) {
            existingLog.setContent(careLog.getContent());
        }
        if (careLog.getLogDate() != null) {
            existingLog.setLogDate(careLog.getLogDate());
        }
        if (careLog.getImages() != null) {
            existingLog.setImages(careLog.getImages());
        }
        if (careLog.getFertilizer() != null) {
            existingLog.setFertilizer(careLog.getFertilizer());
        }
        if (careLog.getPosition() != null) {
            existingLog.setPosition(careLog.getPosition());
        }
        if (careLog.getSoilType() != null) {
            existingLog.setSoilType(careLog.getSoilType());
        }
        if (careLog.getBonsaiId() != null) {
            existingLog.setBonsaiId(careLog.getBonsaiId());
        }

        return careLogRepository.save(existingLog);
    }

    @Transactional
    public void deleteCareLog(Long id) {
        CareLog log = careLogRepository.findById(id).orElse(null);
        if (log != null) {
            imageUtil.deleteImages(log.getImages());
            careLogRepository.deleteById(id);
        }
    }

    public List<SpeciesCareProfile> getSpeciesCareProfiles(Long userId) {
        List<CareLog> logs = careLogRepository.findByUserIdAndBonsaiIdIsNotNullOrderByLogDateAsc(userId);
        List<Category> speciesList = categoryRepository.findByTypeOrderBySortOrderAsc("species");
        List<Bonsai> userBonsais = bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId);

        Map<Long, Category> speciesMap = new HashMap<>();
        for (Category species : speciesList) {
            speciesMap.put(species.getId(), species);
        }

        Map<Long, List<CareLog>> logsBySpecies = new HashMap<>();
        Map<Long, Set<Long>> bonsaiIdsBySpecies = new HashMap<>();

        for (Bonsai bonsai : userBonsais) {
            if (bonsai.getSpeciesId() != null) {
                bonsaiIdsBySpecies.computeIfAbsent(bonsai.getSpeciesId(), k -> new HashSet<>())
                    .add(bonsai.getId());
            }
        }

        for (CareLog log : logs) {
            if (log.getBonsai() != null && log.getBonsai().getSpeciesId() != null) {
                Long speciesId = log.getBonsai().getSpeciesId();
                logsBySpecies.computeIfAbsent(speciesId, k -> new ArrayList<>()).add(log);
            }
        }

        List<SpeciesCareProfile> profiles = new ArrayList<>();
        for (Map.Entry<Long, List<CareLog>> entry : logsBySpecies.entrySet()) {
            Long speciesId = entry.getKey();
            List<CareLog> speciesLogs = entry.getValue();
            Category species = speciesMap.get(speciesId);

            if (species == null) {
                continue;
            }

            SpeciesCareProfile profile = new SpeciesCareProfile();
            profile.setSpeciesId(speciesId);
            profile.setSpeciesName(species.getName());
            profile.setSpeciesIcon(species.getIcon());

            List<CareLog> waterLogs = speciesLogs.stream()
                .filter(l -> "water".equals(l.getLogType()))
                .collect(Collectors.toList());
            List<CareLog> fertilizeLogs = speciesLogs.stream()
                .filter(l -> "fertilize".equals(l.getLogType()))
                .collect(Collectors.toList());
            List<CareLog> pruneLogs = speciesLogs.stream()
                .filter(l -> "prune".equals(l.getLogType()))
                .collect(Collectors.toList());

            profile.setWaterCount(waterLogs.size());
            profile.setFertilizeCount(fertilizeLogs.size());
            profile.setPruneCount(pruneLogs.size());

            profile.setAvgWaterInterval(calculateAvgInterval(waterLogs));
            profile.setAvgFertilizeInterval(calculateAvgInterval(fertilizeLogs));
            profile.setAvgPruneInterval(calculateAvgInterval(pruneLogs));

            Set<Long> bonsaiSet = bonsaiIdsBySpecies.getOrDefault(speciesId, new HashSet<>());
            profile.setBonsaiCount(bonsaiSet.size());

            profiles.add(profile);
        }

        profiles.sort((a, b) -> {
            int countA = a.getWaterCount() + a.getFertilizeCount() + a.getPruneCount();
            int countB = b.getWaterCount() + b.getFertilizeCount() + b.getPruneCount();
            return Integer.compare(countB, countA);
        });

        return profiles;
    }

    private Double calculateAvgInterval(List<CareLog> logs) {
        if (logs.size() < 2) {
            return null;
        }
        long totalDays = 0;
        int intervalCount = 0;
        LocalDate prevDate = null;
        for (CareLog log : logs) {
            if (prevDate != null) {
                long days = ChronoUnit.DAYS.between(prevDate, log.getLogDate());
                if (days > 0) {
                    totalDays += days;
                    intervalCount++;
                }
            }
            prevDate = log.getLogDate();
        }
        if (intervalCount == 0) {
            return null;
        }
        return (double) totalDays / intervalCount;
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
