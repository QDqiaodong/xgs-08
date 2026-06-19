package com.bonsai.service;

import com.bonsai.dto.CareSummary;
import com.bonsai.dto.UserBonsaiProfile;
import com.bonsai.entity.Bonsai;
import com.bonsai.entity.CareLog;
import com.bonsai.entity.Category;
import com.bonsai.entity.LifecycleEvent;
import com.bonsai.entity.Post;
import com.bonsai.entity.PostImage;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.CareLogRepository;
import com.bonsai.repository.LifecycleEventRepository;
import com.bonsai.repository.PostRepository;
import com.bonsai.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonsaiService {

    private final BonsaiRepository bonsaiRepository;
    private final LifecycleEventRepository lifecycleEventRepository;
    private final CareLogRepository careLogRepository;
    private final PostRepository postRepository;
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

    public UserBonsaiProfile getUserBonsaiProfile(Long userId) {
        UserBonsaiProfile profile = new UserBonsaiProfile();

        List<Bonsai> bonsais = bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId);
        profile.setBonsaiCount(bonsais.size());

        if (!bonsais.isEmpty()) {
            Map<String, Long> speciesCount = bonsais.stream()
                    .filter(b -> b.getSpecies() != null)
                    .collect(Collectors.groupingBy(
                            b -> b.getSpecies().getName(),
                            Collectors.counting()
                    ));

            String mainSpecies = speciesCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            profile.setMainSpecies(mainSpecies);

            Map<String, Long> styleCount = bonsais.stream()
                    .filter(b -> b.getTrunkShape() != null && !b.getTrunkShape().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            Bonsai::getTrunkShape,
                            Collectors.counting()
                    ));

            String commonStyle = styleCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            profile.setCommonStyle(commonStyle);
        }

        List<CareLog> recentLogs = careLogRepository.findTop1ByUserIdOrderByLogDateDesc(userId);
        if (!recentLogs.isEmpty()) {
            profile.setLastCareDate(recentLogs.get(0).getLogDate());
        }

        Pageable topOne = PageRequest.of(0, 1);
        List<Post> topPosts = postRepository.findTopByUserIdOrderByPopularity(userId, topOne);
        if (!topPosts.isEmpty()) {
            Post topPost = topPosts.get(0);
            profile.setRepresentativeWorkId(topPost.getId());
            profile.setRepresentativeWorkTitle(topPost.getTitle());

            String coverImage = Optional.ofNullable(topPost.getImages())
                    .filter(images -> !images.isEmpty())
                    .map(images -> images.get(0))
                    .map(PostImage::getImageUrl)
                    .orElse(null);
            profile.setRepresentativeWorkImage(coverImage);
        }

        return profile;
    }

    public CareSummary getCareSummary(Long bonsaiId) {
        CareSummary summary = new CareSummary();

        List<CareLog> lastWater = careLogRepository.findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(bonsaiId, "water");
        if (!lastWater.isEmpty()) {
            summary.setLastWaterDate(lastWater.get(0).getLogDate());
        }

        List<CareLog> lastPrune = careLogRepository.findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(bonsaiId, "prune");
        if (!lastPrune.isEmpty()) {
            summary.setLastPruneDate(lastPrune.get(0).getLogDate());
        }

        List<CareLog> lastFertilize = careLogRepository.findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(bonsaiId, "fertilize");
        if (!lastFertilize.isEmpty()) {
            summary.setLastFertilizeDate(lastFertilize.get(0).getLogDate());
        }

        List<CareLog> lastRepot = careLogRepository.findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(bonsaiId, "repot");
        if (!lastRepot.isEmpty()) {
            summary.setLastRepotDate(lastRepot.get(0).getLogDate());
        }

        return summary;
    }
}
