package com.bonsai.entity;

public enum TrainingStage {
    MATERIAL_CULTIVATION("素材培养", "🌱", "#67c23a"),
    INITIAL_SHAPING("初步定型", "✂️", "#e6a23c"),
    FINE_BRANCH_TRAINING("细枝培养", "🌿", "#409eff"),
    ORNAMENTAL_PERIOD("观赏期", "🌸", "#f56c6c");

    private final String label;
    private final String icon;
    private final String color;

    TrainingStage(String label, String icon, String color) {
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
