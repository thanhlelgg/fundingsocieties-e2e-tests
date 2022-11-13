package com.fundingsocieties.model;

import com.fundingsocieties.common.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TooltipAttr implements Comparable<TooltipAttr> {
    private String tooltipName;
    private String attrName;
    private String attrValue;

    public TooltipAttr(final String rawText) {
        this.tooltipName = getTooltipName(rawText);
        this.attrName = getValueName(rawText);
        this.attrValue = getValue(rawText);
    }

    private String getTooltipName(final String tooltipText) {
        final String regex = "(.*)●";
        return CommonHelper.getMatchGroup(tooltipText, regex, 1);
    }

    private String getValueName(final String tooltipText) {
        final String regex = "●(.*):";
        return CommonHelper.getMatchGroup(tooltipText, regex, 1).trim();
    }

    private String getValue(final String tooltipText) {
        final String regex = ": ([\\d|,|\\.]+)";
        return CommonHelper.getMatchGroup(tooltipText, regex, 1);
    }

    public double getValueAsNumber() {
        return Double.parseDouble(this.attrValue.trim().replace(",", ""));
    }

    @Override
    public int compareTo(final TooltipAttr tooltipAttr) {
        return Double.compare(getValueAsNumber(), tooltipAttr.getValueAsNumber());
    }
}
