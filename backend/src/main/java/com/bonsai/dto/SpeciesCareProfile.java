package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesCareProfile {

    private Long speciesId;

    private String speciesName;

    private String speciesIcon;

    private Integer waterCount;

    private Integer fertilizeCount;

    private Integer pruneCount;

    private Double avgWaterInterval;

    private Double avgFertilizeInterval;

    private Double avgPruneInterval;

    private Integer bonsaiCount;
}
