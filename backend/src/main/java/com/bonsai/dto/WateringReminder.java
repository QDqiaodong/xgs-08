package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WateringReminder {

    private Long bonsaiId;

    private String bonsaiName;

    private String bonsaiCoverImage;

    private String speciesName;

    private String potType;

    private LocalDate lastWaterDate;

    private LocalDate nextWaterDate;

    private Integer daysUntilNextWater;

    private String status;

    private Integer baseWaterInterval;

    private Integer adjustedWaterInterval;

    private String adjustmentReason;
}
