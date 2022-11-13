package com.fundingsocieties.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChartToggle {
    TOTAL_APPROVED("toggle-approved"),
    AMOUNT_DISBURSED("toggle-disbursed"),
    DEFAULT_RATE("toggle-default");
    
    private String toggleForAttr;
}
