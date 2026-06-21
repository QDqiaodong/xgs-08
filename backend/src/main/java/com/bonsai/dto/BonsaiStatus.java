package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonsaiStatus {

    public enum Status {
        NEED_WATER,
        NEED_PRUNE,
        STABLE
    }

    private Status status;

    private String statusLabel;

    private String statusIcon;

    private String statusColor;

    private String message;

    private LocalDate lastWaterDate;

    private Integer daysSinceLastWater;

    private LocalDate lastPruneDate;

    private Integer daysSinceLastPrune;

    private LocalDate lastFertilizeDate;

    private Integer daysSinceLastFertilize;
}
