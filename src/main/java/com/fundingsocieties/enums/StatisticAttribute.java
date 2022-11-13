package com.fundingsocieties.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatisticAttribute {
    TOTAL_FUNDED("Total funded"),
    NO_OF_FINANCING("No. offinancing"),
    DEFAULT_RATE("Defaultrate"),
    FULFILLMENT_RATE("Financingfulfillment rate");

    private String attrText;
}
