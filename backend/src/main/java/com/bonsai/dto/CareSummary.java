package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareSummary {

    private LocalDate lastWaterDate;

    private LocalDate lastPruneDate;

    private LocalDate lastFertilizeDate;

    private LocalDate lastRepotDate;
}
