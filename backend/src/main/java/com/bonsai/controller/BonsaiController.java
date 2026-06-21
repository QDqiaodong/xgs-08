package com.bonsai.controller;

import com.bonsai.dto.BonsaiStatus;
import com.bonsai.dto.CareSummary;
import com.bonsai.dto.Result;
import com.bonsai.dto.UserBonsaiProfile;
import com.bonsai.entity.Bonsai;
import com.bonsai.entity.BonsaiStageImage;
import com.bonsai.entity.BonsaiStageImage.Stage;
import com.bonsai.service.BonsaiService;
import com.bonsai.util.BonsaiValidator.ValidationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bonsais")
@RequiredArgsConstructor
public class BonsaiController {

    private final BonsaiService bonsaiService;

    @GetMapping("/user/{userId}")
    public Result<Page<Bonsai>> getUserBonsais(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bonsaiService.getUserBonsais(userId, page, size));
    }

    @GetMapping("/user/{userId}/list")
    public Result<List<Bonsai>> getUserBonsaiList(@PathVariable Long userId) {
        return Result.success(bonsaiService.getUserBonsaiList(userId));
    }

    @GetMapping("/user/{userId}/profile")
    public Result<UserBonsaiProfile> getUserBonsaiProfile(@PathVariable Long userId) {
        return Result.success(bonsaiService.getUserBonsaiProfile(userId));
    }

    @GetMapping("/{id}")
    public Result<Bonsai> getBonsaiById(@PathVariable Long id) {
        bonsaiService.incrementViewCount(id);
        return Result.success(bonsaiService.getBonsaiById(id));
    }

    @PostMapping
    public Result<Bonsai> createBonsai(@Valid @RequestBody Bonsai bonsai) {
        return Result.success(bonsaiService.createBonsai(bonsai));
    }

    @PutMapping
    public Result<Bonsai> updateBonsai(@Valid @RequestBody Bonsai bonsai) {
        return Result.success(bonsaiService.updateBonsai(bonsai));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBonsai(@PathVariable Long id) {
        bonsaiService.deleteBonsai(id);
        return Result.success();
    }

    @GetMapping("/{id}/care-summary")
    public Result<CareSummary> getCareSummary(@PathVariable Long id) {
        return Result.success(bonsaiService.getCareSummary(id));
    }

    @GetMapping("/{id}/status")
    public Result<BonsaiStatus> getBonsaiStatus(@PathVariable Long id) {
        return Result.success(bonsaiService.getBonsaiStatus(id));
    }

    @GetMapping("/user/{userId}/statuses")
    public Result<java.util.Map<Long, BonsaiStatus>> getUserBonsaiStatuses(@PathVariable Long userId) {
        java.util.List<Bonsai> bonsais = bonsaiService.getUserBonsaiList(userId);
        java.util.Map<Long, BonsaiStatus> statusMap = new java.util.HashMap<>();
        for (Bonsai bonsai : bonsais) {
            statusMap.put(bonsai.getId(), bonsaiService.getBonsaiStatus(bonsai.getId()));
        }
        return Result.success(statusMap);
    }

    @PostMapping("/validate")
    public Result<ValidationResult> validateBonsai(@RequestBody Bonsai bonsai) {
        return Result.success(bonsaiService.validateBonsaiData(bonsai));
    }

    @GetMapping("/{id}/stage-images")
    public Result<List<BonsaiStageImage>> getStageImages(@PathVariable Long id) {
        return Result.success(bonsaiService.getStageImages(id));
    }

    @GetMapping("/{id}/stage-images/grouped")
    public Result<Map<String, List<BonsaiStageImage>>> getStageImagesGrouped(@PathVariable Long id) {
        return Result.success(bonsaiService.getStageImagesGrouped(id));
    }

    @PostMapping("/{id}/stage-images")
    public Result<BonsaiStageImage> addStageImage(
            @PathVariable Long id,
            @RequestBody BonsaiStageImage stageImage) {
        stageImage.setBonsaiId(id);
        return Result.success(bonsaiService.addStageImage(stageImage));
    }

    @PostMapping("/{id}/stage-images/batch")
    public Result<List<BonsaiStageImage>> addStageImages(
            @PathVariable Long id,
            @RequestBody List<BonsaiStageImage> images) {
        return Result.success(bonsaiService.addStageImages(id, images));
    }

    @PutMapping("/stage-images/{imageId}")
    public Result<BonsaiStageImage> updateStageImage(
            @PathVariable Long imageId,
            @RequestBody BonsaiStageImage stageImage) {
        stageImage.setId(imageId);
        return Result.success(bonsaiService.updateStageImage(stageImage));
    }

    @DeleteMapping("/stage-images/{imageId}")
    public Result<Void> deleteStageImage(@PathVariable Long imageId) {
        bonsaiService.deleteStageImage(imageId);
        return Result.success();
    }

    @DeleteMapping("/{id}/stage-images/stage/{stage}")
    public Result<Void> deleteStageImagesByStage(
            @PathVariable Long id,
            @PathVariable String stage) {
        try {
            Stage stageEnum = Stage.valueOf(stage);
            bonsaiService.deleteStageImagesByStage(id, stageEnum);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, "无效的阶段类型: " + stage);
        }
    }
}
