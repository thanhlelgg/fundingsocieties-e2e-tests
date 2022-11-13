package com.fundingsocieties.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopMenuItem {
    INVESTMENT("Investment"),
    STATISTICS("Statistics");
    
    private String itemName;
}
