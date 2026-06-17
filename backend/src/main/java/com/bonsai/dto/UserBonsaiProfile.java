package com.bonsai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBonsaiProfile {

    private String mainSpecies;

    private String commonStyle;

    private LocalDate lastCareDate;

    private String representativeWorkImage;

    private String representativeWorkTitle;

    private Long representativeWorkId;

    private Integer bonsaiCount;
}
