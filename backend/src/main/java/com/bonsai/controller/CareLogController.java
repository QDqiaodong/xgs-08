package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.CareLog;
import com.bonsai.service.CareLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/care-logs")
@RequiredArgsConstructor
public class CareLogController {

    private final CareLogService careLogService;

    @GetMapping("/user/{userId}")
    public Result<Page<CareLog>> getUserCareLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(careLogService.getUserCareLogs(userId, page, size));
    }

    @GetMapping("/post/{postId}")
    public Result<List<CareLog>> getPostCareLogs(@PathVariable Long postId) {
        return Result.success(careLogService.getPostCareLogs(postId));
    }

    @PostMapping
    public Result<CareLog> createCareLog(@RequestBody CareLog careLog) {
        return Result.success(careLogService.createCareLog(careLog));
    }
}
