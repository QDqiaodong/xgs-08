package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.RepotRecord;
import com.bonsai.service.RepotRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repot-records")
@RequiredArgsConstructor
public class RepotRecordController {

    private final RepotRecordService repotRecordService;

    @GetMapping("/user/{userId}")
    public Result<Page<RepotRecord>> getUserRepotRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(repotRecordService.getUserRepotRecords(userId, page, size));
    }

    @GetMapping("/user/{userId}/list")
    public Result<List<RepotRecord>> getUserRepotRecordList(@PathVariable Long userId) {
        return Result.success(repotRecordService.getUserRepotRecords(userId));
    }

    @GetMapping("/bonsai/{bonsaiId}")
    public Result<List<RepotRecord>> getBonsaiRepotRecords(@PathVariable Long bonsaiId) {
        return Result.success(repotRecordService.getBonsaiRepotRecords(bonsaiId));
    }

    @GetMapping("/{id}")
    public Result<RepotRecord> getRepotRecordById(@PathVariable Long id) {
        return Result.success(repotRecordService.getRepotRecordById(id));
    }

    @PostMapping
    public Result<RepotRecord> createRepotRecord(@Valid @RequestBody RepotRecord record) {
        return Result.success(repotRecordService.createRepotRecord(record));
    }

    @PutMapping
    public Result<RepotRecord> updateRepotRecord(@RequestBody RepotRecord record) {
        return Result.success(repotRecordService.updateRepotRecord(record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRepotRecord(@PathVariable Long id) {
        repotRecordService.deleteRepotRecord(id);
        return Result.success();
    }
}
