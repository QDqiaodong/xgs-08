package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/check")
    public Result<Boolean> hasLiked(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return Result.success(likeService.hasLiked(userId, targetType, targetId));
    }

    @PostMapping("/toggle")
    public Result<Void> toggleLike(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        likeService.toggleLike(userId, targetType, targetId);
        return Result.success();
    }
}
