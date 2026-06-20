package com.bonsai.service;

import com.bonsai.dto.CareSummary;
import com.bonsai.dto.UserBonsaiProfile;
import com.bonsai.entity.Bonsai;
import com.bonsai.entity.BonsaiStageImage;
import com.bonsai.entity.BonsaiStageImage.Stage;
import com.bonsai.entity.CareLog;
import com.bonsai.entity.Category;
import com.bonsai.entity.LifecycleEvent;
import com.bonsai.entity.Post;
import com.bonsai.entity.PostImage;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.BonsaiStageImageRepository;
import com.bonsai.repository.CareLogRepository;
import com.bonsai.repository.LifecycleEventRepository;
import com.bonsai.repository.PostRepository;
import com.bonsai.util.BonsaiValidator;
import com.bonsai.util.BonsaiValidator.ValidationResult;
import com.bonsai.util.ImageUtil;
import com.bonsai.util.SpeciesNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonsaiService {

    private final BonsaiRepository bonsaiRepository;
    private final BonsaiStageImageRepository stageImageRepository;
    private final LifecycleEventRepository lifecycleEventRepository;
    private final CareLogRepository careLogRepository;
    private final PostRepository postRepository;
    private final BonsaiValidator bonsaiValidator;
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

    @Transactional
    public Bonsai createBonsai(Bonsai bonsai) {
        ValidationResult validationResult = validateBonsai(bonsai);
        if (validationResult.hasErrors()) {
            throw new IllegalArgumentException(String.join("; ", validationResult.getErrors()));
        }
        return bonsaiRepository.save(bonsai);
    }

    @Transactional
    public Bonsai updateBonsai(Bonsai bonsai) {
        ValidationResult validationResult = validateBonsai(bonsai);
        if (validationResult.hasErrors()) {
            throw new IllegalArgumentException(String.join("; ", validationResult.getErrors()));
        }
        return bonsaiRepository.save(bonsai);
    }

    public ValidationResult validateBonsaiData(Bonsai bonsai) {
        if (bonsai.getSpeciesId() != null) {
            bonsai.setSpecies(getCategoryReference(bonsai.getSpeciesId()));
        }
        if (bonsai.getStyleId() != null) {
            bonsai.setStyle(getCategoryReference(bonsai.getStyleId()));
        }
        return bonsaiValidator.validate(bonsai);
    }

    private Category getCategoryReference(Long id) {
        Category cat = new Category();
        cat.setId(id);
        return cat;
    }

    @Transactional
    public void deleteBonsai(Long id) {
        Bonsai bonsai = bonsaiRepository.findById(id).orElse(null);
        if (bonsai == null) {
            return;
        }

        List<LifecycleEvent> events = lifecycleEventRepository.findByBonsaiIdOrderByEventDateAsc(id);
        List<BonsaiStageImage> stageImages = stageImageRepository.findByBonsaiIdOrderByStageAscSortOrderAsc(id);

        imageUtil.deleteImages(bonsai.getCoverImage());

        for (LifecycleEvent event : events) {
            imageUtil.deleteImages(event.getImages());
            imageUtil.deleteImages(event.getBeforeImages());
        }

        for (BonsaiStageImage stageImage : stageImages) {
            imageUtil.deleteImages(stageImage.getImageUrl());
        }

        stageImageRepository.deleteByBonsaiId(id);
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

    private ValidationResult validateBonsai(Bonsai bonsai) {
        if (bonsai.getUserId() == null || bonsai.getUserId() <= 0) {
            ValidationResult result = new ValidationResult();
            result.addError("无效的用户ID");
            return result;
        }
        if (bonsai.getName() == null || bonsai.getName().trim().isEmpty()) {
            ValidationResult result = new ValidationResult();
            result.addError("盆景名称不能为空");
            return result;
        }
        if (bonsai.getTreeAge() != null && bonsai.getTreeAge() < 0) {
            ValidationResult result = new ValidationResult();
            result.addError("树龄不能为负数");
            return result;
        }
        if (bonsai.getAcquireDate() != null && bonsai.getAcquireDate().isAfter(LocalDate.now())) {
            ValidationResult result = new ValidationResult();
            result.addError("入手日期不能是未来日期");
            return result;
        }
        if (bonsai.getViewCount() == null) {
            bonsai.setViewCount(0);
        }
        if (bonsai.getViewCount() < 0) {
            ValidationResult result = new ValidationResult();
            result.addError("浏览次数不能为负数");
            return result;
        }

        return bonsaiValidator.validate(bonsai);
    }

    public List<BonsaiStageImage> getStageImages(Long bonsaiId) {
        return stageImageRepository.findByBonsaiIdOrderByStageAscSortOrderAsc(bonsaiId);
    }

    public Map<String, List<BonsaiStageImage>> getStageImagesGrouped(Long bonsaiId) {
        List<BonsaiStageImage> images = stageImageRepository.findByBonsaiIdOrderByStageAscSortOrderAsc(bonsaiId);
        return images.stream().collect(Collectors.groupingBy(img -> img.getStage().name()));
    }

    @Transactional
    public BonsaiStageImage addStageImage(BonsaiStageImage stageImage) {
        if (stageImage.getBonsaiId() == null) {
            throw new IllegalArgumentException("盆景ID不能为空");
        }
        if (stageImage.getStage() == null) {
            throw new IllegalArgumentException("阶段不能为空");
        }
        if (stageImage.getImageUrl() == null || stageImage.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }
        Bonsai bonsai = bonsaiRepository.findById(stageImage.getBonsaiId()).orElse(null);
        if (bonsai == null) {
            throw new IllegalArgumentException("盆景不存在");
        }
        return stageImageRepository.save(stageImage);
    }

    @Transactional
    public List<BonsaiStageImage> addStageImages(Long bonsaiId, List<BonsaiStageImage> images) {
        Bonsai bonsai = bonsaiRepository.findById(bonsaiId).orElse(null);
        if (bonsai == null) {
            throw new IllegalArgumentException("盆景不存在");
        }
        List<BonsaiStageImage> savedImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            BonsaiStageImage img = images.get(i);
            img.setBonsaiId(bonsaiId);
            if (img.getSortOrder() == null) {
                img.setSortOrder(i);
            }
            savedImages.add(stageImageRepository.save(img));
        }
        return savedImages;
    }

    @Transactional
    public void deleteStageImage(Long imageId) {
        BonsaiStageImage image = stageImageRepository.findById(imageId).orElse(null);
        if (image != null) {
            imageUtil.deleteImages(image.getImageUrl());
            stageImageRepository.deleteById(imageId);
        }
    }

    @Transactional
    public void deleteStageImagesByStage(Long bonsaiId, Stage stage) {
        List<BonsaiStageImage> images = stageImageRepository.findByBonsaiIdAndStageOrderBySortOrderAsc(bonsaiId, stage);
        for (BonsaiStageImage img : images) {
            imageUtil.deleteImages(img.getImageUrl());
        }
        stageImageRepository.deleteByBonsaiIdAndStage(bonsaiId, stage);
    }

    @Transactional
    public BonsaiStageImage updateStageImage(BonsaiStageImage stageImage) {
        if (stageImage.getId() == null) {
            throw new IllegalArgumentException("图片ID不能为空");
        }
        BonsaiStageImage existing = stageImageRepository.findById(stageImage.getId()).orElse(null);
        if (existing == null) {
            throw new IllegalArgumentException("图片不存在");
        }
        if (stageImage.getNote() != null) {
            existing.setNote(stageImage.getNote());
        }
        if (stageImage.getSortOrder() != null) {
            existing.setSortOrder(stageImage.getSortOrder());
        }
        return stageImageRepository.save(existing);
    }

    public UserBonsaiProfile getUserBonsaiProfile(Long userId) {
        UserBonsaiProfile profile = new UserBonsaiProfile();

        List<Bonsai> bonsais = bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId);
        profile.setBonsaiCount(bonsais.size());

        if (!bonsais.isEmpty()) {
            Map<String, Long> speciesCount = bonsais.stream()
                    .map(b -> {
                        String speciesName = b.getSpecies() != null ? b.getSpecies().getName() : null;
                        return SpeciesNormalizer.normalize(speciesName);
                    })
                    .collect(Collectors.groupingBy(
                            speciesName -> speciesName,
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
