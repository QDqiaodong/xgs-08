package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.dto.WateringReminder;
import com.bonsai.service.WateringReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/watering-reminders")
@RequiredArgsConstructor
public class WateringReminderController {

    private final WateringReminderService wateringReminderService;

    @GetMapping("/user/{userId}")
    public Result<List<WateringReminder>> getUserWateringReminders(@PathVariable Long userId) {
        return Result.success(wateringReminderService.getUserWateringReminders(userId));
    }

    @GetMapping("/bonsai/{bonsaiId}")
    public Result<WateringReminder> getBonsaiWateringReminder(@PathVariable Long bonsaiId) {
        return Result.success(wateringReminderService.getBonsaiWateringReminder(bonsaiId));
    }
}
