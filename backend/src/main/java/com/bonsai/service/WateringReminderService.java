package com.bonsai.service;

import com.bonsai.dto.WateringReminder;
import com.bonsai.entity.Bonsai;
import com.bonsai.entity.CareLog;
import com.bonsai.entity.Category;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.CareLogRepository;
import com.bonsai.util.SpeciesNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WateringReminderService {

    private final BonsaiRepository bonsaiRepository;
    private final CareLogRepository careLogRepository;

    private static final Map<String, Integer> SPECIES_BASE_INTERVAL = new HashMap<>();
    private static final Map<String, Integer> POT_TYPE_ADJUSTMENT = new HashMap<>();

    static {
        SPECIES_BASE_INTERVAL.put("黑松", 4);
        SPECIES_BASE_INTERVAL.put("五针松", 5);
        SPECIES_BASE_INTERVAL.put("真柏", 3);
        SPECIES_BASE_INTERVAL.put("罗汉松", 4);
        SPECIES_BASE_INTERVAL.put("榕树", 2);
        SPECIES_BASE_INTERVAL.put("雀梅", 2);
        SPECIES_BASE_INTERVAL.put("火棘", 4);
        SPECIES_BASE_INTERVAL.put("榔榆", 2);
        SPECIES_BASE_INTERVAL.put("松树", 4);
        SPECIES_BASE_INTERVAL.put("柏树", 3);
        SPECIES_BASE_INTERVAL.put("杂木", 2);
        SPECIES_BASE_INTERVAL.put("观果", 4);

        POT_TYPE_ADJUSTMENT.put("紫砂盆", 0);
        POT_TYPE_ADJUSTMENT.put("陶盆", 1);
        POT_TYPE_ADJUSTMENT.put("瓷盆", -1);
        POT_TYPE_ADJUSTMENT.put("塑料盆", -1);
        POT_TYPE_ADJUSTMENT.put("泥盆", 1);
        POT_TYPE_ADJUSTMENT.put("石盆", 0);
    }

    public List<WateringReminder> getUserWateringReminders(Long userId) {
        List<Bonsai> bonsais = bonsaiRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<WateringReminder> reminders = new ArrayList<>();

        for (Bonsai bonsai : bonsais) {
            WateringReminder reminder = calculateWateringReminder(bonsai);
            reminders.add(reminder);
        }

        reminders.sort((r1, r2) -> {
            int priority1 = getStatusPriority(r1.getStatus());
            int priority2 = getStatusPriority(r2.getStatus());
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            return Integer.compare(r1.getDaysUntilNextWater(), r2.getDaysUntilNextWater());
        });

        return reminders;
    }

    public WateringReminder getBonsaiWateringReminder(Long bonsaiId) {
        Bonsai bonsai = bonsaiRepository.findById(bonsaiId).orElse(null);
        if (bonsai == null) {
            return null;
        }
        return calculateWateringReminder(bonsai);
    }

    private WateringReminder calculateWateringReminder(Bonsai bonsai) {
        WateringReminder reminder = new WateringReminder();
        reminder.setBonsaiId(bonsai.getId());
        reminder.setBonsaiName(bonsai.getName());
        reminder.setBonsaiCoverImage(bonsai.getCoverImage());
        reminder.setPotType(bonsai.getPotType());

        String speciesName = null;
        if (bonsai.getSpecies() != null) {
            speciesName = bonsai.getSpecies().getName();
        }
        String normalizedSpecies = SpeciesNormalizer.normalize(speciesName);
        reminder.setSpeciesName(normalizedSpecies);

        List<CareLog> lastWaterLogs = careLogRepository.findTop1ByBonsaiIdAndLogTypeOrderByLogDateDesc(bonsai.getId(), "water");
        LocalDate lastWaterDate = null;
        if (!lastWaterLogs.isEmpty()) {
            lastWaterDate = lastWaterLogs.get(0).getLogDate();
        }
        reminder.setLastWaterDate(lastWaterDate);

        int baseInterval = getBaseWaterInterval(normalizedSpecies);
        reminder.setBaseWaterInterval(baseInterval);

        int potAdjustment = getPotTypeAdjustment(bonsai.getPotType());
        int seasonAdjustment = getSeasonAdjustment();

        int adjustedInterval = baseInterval + potAdjustment + seasonAdjustment;
        adjustedInterval = Math.max(1, Math.min(adjustedInterval, 15));
        reminder.setAdjustedWaterInterval(adjustedInterval);

        StringBuilder reason = new StringBuilder();
        reason.append("基准时差").append(baseInterval).append("天");
        if (potAdjustment != 0) {
            reason.append(potAdjustment > 0 ? "+" : "").append(potAdjustment).append("天（盆型）");
        }
        if (seasonAdjustment != 0) {
            reason.append(seasonAdjustment > 0 ? "+" : "").append(seasonAdjustment).append("天（季节）");
        }
        reminder.setAdjustmentReason(reason.toString());

        LocalDate nextWaterDate;
        int daysUntilNextWater;
        String status;

        if (lastWaterDate == null) {
            nextWaterDate = LocalDate.now();
            daysUntilNextWater = 0;
            status = "urgent";
            reminder.setAdjustmentReason(reminder.getAdjustmentReason() + "，暂无浇水记录");
        } else {
            nextWaterDate = lastWaterDate.plusDays(adjustedInterval);
            daysUntilNextWater = (int) ChronoUnit.DAYS.between(LocalDate.now(), nextWaterDate);

            if (daysUntilNextWater < 0) {
                status = "overdue";
            } else if (daysUntilNextWater == 0) {
                status = "today";
            } else if (daysUntilNextWater <= 1) {
                status = "soon";
            } else {
                status = "normal";
            }
        }

        reminder.setNextWaterDate(nextWaterDate);
        reminder.setDaysUntilNextWater(daysUntilNextWater);
        reminder.setStatus(status);

        return reminder;
    }

    private int getBaseWaterInterval(String speciesName) {
        if (speciesName == null) {
            return 3;
        }

        for (Map.Entry<String, Integer> entry : SPECIES_BASE_INTERVAL.entrySet()) {
            if (speciesName.contains(entry.getKey()) || entry.getKey().contains(speciesName)) {
                return entry.getValue();
            }
        }

        return 3;
    }

    private int getPotTypeAdjustment(String potType) {
        if (potType == null || potType.trim().isEmpty()) {
            return 0;
        }

        for (Map.Entry<String, Integer> entry : POT_TYPE_ADJUSTMENT.entrySet()) {
            if (potType.contains(entry.getKey()) || entry.getKey().contains(potType)) {
                return entry.getValue();
            }
        }

        return 0;
    }

    private int getSeasonAdjustment() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 6 && month <= 8) {
            return -1;
        } else if (month == 12 || month <= 2) {
            return 2;
        } else if (month >= 3 && month <= 5) {
            return 0;
        } else {
            return 1;
        }
    }

    private int getStatusPriority(String status) {
        switch (status) {
            case "overdue":
                return 0;
            case "urgent":
                return 1;
            case "today":
                return 2;
            case "soon":
                return 3;
            case "normal":
                return 4;
            default:
                return 5;
        }
    }
}
