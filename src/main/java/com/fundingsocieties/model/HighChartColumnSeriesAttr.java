package com.fundingsocieties.model;

import com.fundingsocieties.common.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.Color;

@AllArgsConstructor
@Getter
public class HighChartColumnSeriesAttr {
    String colorString;
    TooltipAttr tooltipAttr;

    public Color getColor() {
        //support either hex or rgb
        return this.colorString.startsWith("#") ? CommonHelper.hex2Rgb(this.colorString) :
                CommonHelper.rgbFromString(this.colorString);
    }
}
