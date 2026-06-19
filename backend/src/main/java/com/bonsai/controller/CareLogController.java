package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.CareLog;
import com.bonsai.service.CareLogService;
import jakarta.validation.Valid;
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

    @GetMapping("/bonsai/{bonsaiId}")
    public Result<List<CareLog>> getBonsaiCareLogs(@PathVariable Long bonsaiId) {
        return Result.success(careLogService.getBonsaiCareLogs(bonsaiId));
    }

    @PostMapping
    public Result<CareLog> createCareLog(@Valid @RequestBody CareLog careLog) {
        return Result.success(careLogService.createCareLog(careLog));
    }
}
