package com.fundingsocieties.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatisticTab {
    GENERAL("General"),
    REPAYMENT("Repayment"),
    DISBURSEMENT("Disbursement");

    private String tabText;
}
