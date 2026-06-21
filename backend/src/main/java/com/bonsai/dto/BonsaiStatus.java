package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonsaiStatus {

    public enum Status {
        NEED_WATER,
        NEED_PRUNE,
        STABLE
    }

    public enum AnomalyType {
        LONG_TIME_NO_CARE("长期无养护记录", "warning-o", "#ff976a"),
        FREQUENT_FERTILIZING("短期重复施肥", "warning", "#ee0a24");

        private final String label;
        private final String icon;
        private final String color;

        AnomalyType(String label, String icon, String color) {
            this.label = label;
            this.icon = icon;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public String getIcon() {
            return icon;
        }

        public String getColor() {
            return color;
        }
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

    private boolean hasAnomaly;

    private List<AnomalyType> anomalies = new ArrayList<>();

    private List<String> anomalyMessages = new ArrayList<>();

    public void addAnomaly(AnomalyType type, String message) {
        this.hasAnomaly = true;
        this.anomalies.add(type);
        this.anomalyMessages.add(message);
    }
}
