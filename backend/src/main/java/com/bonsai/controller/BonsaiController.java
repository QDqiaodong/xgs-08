package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.dto.UserBonsaiProfile;
import com.bonsai.entity.Bonsai;
import com.bonsai.service.BonsaiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
