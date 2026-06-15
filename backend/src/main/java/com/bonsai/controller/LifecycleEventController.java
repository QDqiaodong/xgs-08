package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.LifecycleEvent;
import com.bonsai.service.LifecycleEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lifecycle-events")
@RequiredArgsConstructor
public class LifecycleEventController {

    private final LifecycleEventService lifecycleEventService;

    @GetMapping("/bonsai/{bonsaiId}")
    public Result<List<LifecycleEvent>> getEventsByBonsaiId(@PathVariable Long bonsaiId) {
        return Result.success(lifecycleEventService.getEventsByBonsaiId(bonsaiId));
    }

    @GetMapping("/{id}")
    public Result<LifecycleEvent> getEventById(@PathVariable Long id) {
        return Result.success(lifecycleEventService.getEventById(id));
    }

    @PostMapping
    public Result<LifecycleEvent> createEvent(@RequestBody LifecycleEvent event) {
        return Result.success(lifecycleEventService.createEvent(event));
    }

    @PutMapping
    public Result<LifecycleEvent> updateEvent(@RequestBody LifecycleEvent event) {
        return Result.success(lifecycleEventService.updateEvent(event));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEvent(@PathVariable Long id) {
        lifecycleEventService.deleteEvent(id);
        return Result.success();
    }
}
