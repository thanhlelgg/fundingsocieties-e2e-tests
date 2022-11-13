package com.fundingsocieties.pages;

import com.fundingsocieties.common.CommonHelper;
import com.fundingsocieties.common.Constants;
import com.fundingsocieties.common.ElementHelper;
import com.fundingsocieties.common.FileHelper;
import com.fundingsocieties.common.WaitHelper;
import com.fundingsocieties.controls.BaseControl;
import com.fundingsocieties.controls.Button;
import com.fundingsocieties.controls.Label;
import com.fundingsocieties.enums.ChartToggle;
import com.fundingsocieties.enums.StatisticAttribute;
import com.fundingsocieties.enums.StatisticTab;
import com.fundingsocieties.model.HighChartColumnSeriesAttr;
import com.fundingsocieties.model.TooltipAttr;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebElement;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class StatisticsPage extends BasePage {
    private final Label dynLblStatisticDetail =
            new Label("//div[@class='detailCaption' and normalize-space()='%s']/preceding-sibling::font");
    private final Button dynBtnStatisticTab =
            new Button("//button[contains(@class,'tab-button') and normalize-space()='%s']");
    private final Label dynLblChartToggle = new Label("css=.tab-container label[for='%s']");
    private final BaseControl highChartsMarker = new BaseControl("css=.highcharts-markers path:not([visibility])");
    private final Label lblChartTooltip = new Label("css=.highcharts-tooltip text");
    private final Label lblTotalFundedChartQuarterName =
            new Label("(//*[@class='highcharts-axis-labels highcharts-xaxis-labels'])[1]//*[local-name()='text']");
    private final BaseControl repaymentHighChartColumnSeries =
            new BaseControl("css=.highcharts-series.highcharts-column-series rect.highcharts-point");
    private final Label dynLabelRepaymentHighChartLegends = new Label("css=.highcharts-legend-item:nth-child(%s)");
    private final Button btnPiePieceInChart = new Button("css=.highcharts-pie-series path:not([visibility])");
    private final BaseControl pieChart = new BaseControl("css=.highcharts-series.highcharts-pie-series");

    public boolean isStatisticDetailDisplayed(final StatisticAttribute attrName) {
        this.dynLblStatisticDetail.setDynamicValue(attrName.getAttrText());
        return this.dynLblStatisticDetail.waitForVisibility();
    }

    public String getStatisticDetail(final StatisticAttribute attrName) {
        this.dynLblStatisticDetail.setDynamicValue(attrName.getAttrText());
        this.dynLblStatisticDetail.waitForVisibility();
        return this.dynLblStatisticDetail.getText();
    }

    public String getTotalFundedAmountNumberOnly() {
        final String regex = "\\$(.*)\\s";
        return CommonHelper.getMatchGroup(getStatisticDetail(StatisticAttribute.TOTAL_FUNDED), regex, 1);
    }

    public boolean isStatisticTabDisplayed(final StatisticTab tabName) {
        this.dynBtnStatisticTab.setDynamicValue(tabName.getTabText());
        return this.dynBtnStatisticTab.waitForVisibility();
    }

    public void openStatisticTab(final StatisticTab tabName) {
        this.dynBtnStatisticTab.setDynamicValue(tabName.getTabText());
        this.dynBtnStatisticTab.waitForVisibility();
        this.dynBtnStatisticTab.click();
    }

    public void selectToggle(final ChartToggle toggle) {
        this.dynLblChartToggle.setDynamicValue(toggle.getToggleForAttr());
        this.dynLblChartToggle.waitForVisibility();
        this.dynLblChartToggle.click();
    }

    public List<TooltipAttr> getFinancingChartData() {
        final List<TooltipAttr> financingApprovedData = new ArrayList<>();
        this.highChartsMarker.waitForVisibility();
        for (final WebElement marker : this.highChartsMarker.getElements()) {
            int retryCount = 3;
            marker.click();
            //for some reason sometime first or second click on the mark doesn't show the tooltip,
            // so we retry sometime until it appears
            while (!this.lblChartTooltip.waitForVisibility(Constants.LOW_TIMEOUT_IN_SECONDS) && retryCount-- >= 0) {
                log.info("Retry: " + retryCount);
                marker.click();
            }
            final String tooltipText = this.lblChartTooltip.getText();
            financingApprovedData.add(new TooltipAttr(tooltipText));
        }
        assert financingApprovedData.size() > 0 : "Unable to get data from chart";
        return financingApprovedData;
    }

    public List<String> getQuarterNameListFromTooltips(final List<TooltipAttr> tooltipAttrList) {
        return tooltipAttrList.stream().map(TooltipAttr::getTooltipName).collect(
                Collectors.toList());
    }

    public List<String> getQuarterNameListFromChart() {
        this.lblTotalFundedChartQuarterName.waitForVisibility();
        return this.lblTotalFundedChartQuarterName.getElements().stream().map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void exportChartDataWithHeader(final String tooltipHeader, final List<TooltipAttr> tooltipAttrList,
                                          final String outputFilePath) {
        final String[] header = new String[tooltipAttrList.size() + 1];
        header[0] = tooltipHeader;
        final String[] data = new String[tooltipAttrList.size() + 1];
        //tooltip attribute name is all the same in a chart
        data[0] = tooltipAttrList.get(0).getAttrName();
        for (int i = 1; i <= tooltipAttrList.size(); i++) {
            header[i] = tooltipAttrList.get(i - 1).getTooltipName();
            data[i] = tooltipAttrList.get(i - 1).getAttrValue();
        }
        FileHelper.writeCsvFile(outputFilePath, header, Collections.singletonList(data));
    }

    public void exportStatisticDetails() {
        final String[] header = {"Total funded", "No. of financing", "Default rate", "Financing fulfillment rate"};
        final String[] data = {getStatisticDetail(StatisticAttribute.TOTAL_FUNDED),
                getStatisticDetail(StatisticAttribute.NO_OF_FINANCING),
                getStatisticDetail(StatisticAttribute.DEFAULT_RATE),
                getStatisticDetail(StatisticAttribute.FULFILLMENT_RATE)};
        FileHelper.writeCsvFile(Constants.STATISTIC_DETAILS_FILEPATH, header, Collections.singletonList(data));
    }

    public List<HighChartColumnSeriesAttr> getRepaymentChartData() {
        final List<HighChartColumnSeriesAttr> highChartColumnSeriesAttrs = new ArrayList<>();
        assert this.repaymentHighChartColumnSeries.waitForVisibility() : "Repayment high chart marker not found!";
        for (final WebElement column : this.repaymentHighChartColumnSeries.getElements()) {
            final String columnColor = column.getAttribute("fill");
            column.click();
            this.lblChartTooltip.waitForVisibility();
            final TooltipAttr tooltipAttr = new TooltipAttr(this.lblChartTooltip.getText());
            highChartColumnSeriesAttrs.add(new HighChartColumnSeriesAttr(columnColor, tooltipAttr));
        }
        return highChartColumnSeriesAttrs;
    }

    public void exportHighChartColumnSeriesData(final List<HighChartColumnSeriesAttr> highChartColumnSeriesAttrs,
                                                final String filePath) {
        final String[] header =
                highChartColumnSeriesAttrs.stream().map(attr -> attr.getTooltipAttr().getAttrName())
                        .toArray(String[]::new);
        final String[] value =
                highChartColumnSeriesAttrs.stream().map(attr -> attr.getTooltipAttr().getAttrValue())
                        .toArray(String[]::new);
        FileHelper.writeCsvFile(filePath, header, Collections.singletonList(value));
    }

    public boolean doesChartColumnMatchLegend(final List<HighChartColumnSeriesAttr> highChartColumnSeriesList) {
        if (highChartColumnSeriesList.size() == 0) {
            throw new RuntimeException("Column not found!");
        }

        for (int i = 0; i < highChartColumnSeriesList.size(); i++) {
            this.dynLabelRepaymentHighChartLegends.setDynamicValue(i + 1);
            final WebElement legendElement = this.dynLabelRepaymentHighChartLegends.getElement();
            final WebElement rectElement = legendElement.findElement(By.cssSelector("rect"));
            final WebElement textElement = legendElement.findElement(By.cssSelector("text"));
            final String columnValueName = highChartColumnSeriesList.get(i).getTooltipAttr().getAttrName();
            final Color columnColor = highChartColumnSeriesList.get(i).getColor();
            final String legendName = textElement.getText();
            final Color legendColor = CommonHelper.hex2Rgb(rectElement.getAttribute("fill"));
            if (!columnValueName.equals(legendName)) {
                log.info(String.format("Column name doesn't match legend. Column value: %s. Legends: %s",
                        columnValueName, legendName));
                return false;
            }
            if (!columnColor.equals(legendColor)) {
                log.info(String.format("Column color doesn't match legend. Column color: %s. Legend color: %s",
                        columnColor, legendColor));
                return false;
            }
        }
        return true;
    }

    public List<TooltipAttr> getIndustryChartData() {
        final List<TooltipAttr> industryChartData = new ArrayList<>();
        assert this.pieChart.waitForVisibility() : "Unable to find industry chart";
        this.btnPiePieceInChart.waitForElementClickable();
        for (final WebElement pieElement : this.btnPiePieceInChart.getElements()) {
            ElementHelper.moveTo(pieElement);
            WaitHelper.waitForElementAttributeToBe(pieElement, "class", "point-inactive", 1);
            if (pieElement.getAttribute("class").contains("point-inactive")) {
                try {
                    //Unable to hover on item, try to click it
                    pieElement.click();
                } catch (final ElementClickInterceptedException e) {
                    //Technical debt, explained in README
                    log.warn("Can't select item because it's too small. Skip.");
                    continue;
                }
                if (pieElement.getAttribute("class").contains("point-inactive")) {
                    //Click didn't work either, skip this slice
                    continue;
                }
            }
            this.lblChartTooltip.waitForVisibility();
            this.lblChartTooltip.waitForValueChanges(Constants.LOW_TIMEOUT_IN_SECONDS);
            final String tooltipText = this.lblChartTooltip.getText();
            industryChartData.add(new TooltipAttr(tooltipText));
        }
        Collections.sort(industryChartData);
        return industryChartData;
    }

    public boolean isPieChartSliceDuplicated(final List<TooltipAttr> tooltipAttrList) {
        final List<String> pieName =
                tooltipAttrList.stream().map(TooltipAttr::getTooltipName).sorted().collect(Collectors.toList());
        for (int i = 0; i < pieName.size() - 1; i++) {
            if (Objects.equals(pieName.get(i), pieName.get(i + 1))) {
                log.info(String.format("Duplicate item %s at index %s", pieName.get(i + 1), i + 1));
                return false;
            }
        }
        return true;
    }

    public double getPieChartTotalPercentage(final List<TooltipAttr> tooltipAttrList) {
        return Math.round(tooltipAttrList.stream().mapToDouble(TooltipAttr::getValueAsNumber).sum());
    }
}
