package com.fundingsocieties;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.enums.ChartToggle;
import com.fundingsocieties.enums.StatisticAttribute;
import com.fundingsocieties.enums.StatisticTab;
import com.fundingsocieties.enums.TopMenuItem;
import com.fundingsocieties.model.TooltipAttr;
import com.fundingsocieties.pages.HomePage;
import com.fundingsocieties.pages.StatisticsPage;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j
public class TC002_VerifyStatisticsPageGeneralViewDisplays extends BaseTest {
    HomePage homePage = new HomePage();
    StatisticsPage statisticsPage = new StatisticsPage();
    
    @Test(testName = "TC002_VerifyStatisticsPageGeneralViewDisplays")
    public void tc002_VerifyStatisticsPageGeneralViewDisplays() {
        log.info("Open funding societies homepage");
        this.homePage.open();

        log.info("Click on Statistics top menu button");
        this.homePage.clickTopMenuItem(TopMenuItem.STATISTICS);

        log.info("Click on General tab");
        this.statisticsPage.openStatisticTab(StatisticTab.GENERAL);

        log.info("Click on Total approved button, collect all amount and store to file");
        this.statisticsPage.selectToggle(ChartToggle.TOTAL_APPROVED);
        final List<TooltipAttr> financingApprovedData = this.statisticsPage.getFinancingChartData();
        this.statisticsPage.exportChartDataWithHeader("Quarter", financingApprovedData,
                Constants.FUNDING_APPROVED_FILEPATH);

        log.info("VP: all quarter name is displayed correctly in the tooltips");
        this.softAssert.assertEquals(this.statisticsPage.getQuarterNameListFromTooltips(financingApprovedData),
                this.statisticsPage.getQuarterNameListFromChart(), "Quarter name on tooltips doesn't match the xaxis");

        log.info("VP: the total approved of the last quarter equals to No. of financing on statistics view");
        this.softAssert.assertEquals(financingApprovedData.get(financingApprovedData.size() - 1).getAttrValue(),
                this.statisticsPage.getStatisticDetail(StatisticAttribute.NO_OF_FINANCING),
                "Total approved doesn't match");

        log.info("Click on Amount disbursed button, collect all amount and store to file");
        this.statisticsPage.selectToggle(ChartToggle.AMOUNT_DISBURSED);
        final List<TooltipAttr> amountDisbursedData = this.statisticsPage.getFinancingChartData();
        this.statisticsPage.exportChartDataWithHeader("Quarter", amountDisbursedData,
                Constants.AMOUNT_DISBURSED_FILEPATH);

        log.info("VP: all quarter name is displayed correctly in the tooltips");
        this.softAssert.assertEquals(this.statisticsPage.getQuarterNameListFromTooltips(amountDisbursedData),
                this.statisticsPage.getQuarterNameListFromChart(), "Quarter name on tooltips doesn't match the xaxis");

        log.info("VP: the last quarter amount equals to Total funded on statistics view");
        this.softAssert.assertEquals(amountDisbursedData.get(amountDisbursedData.size() - 1).getAttrValue(),
                this.statisticsPage.getTotalFundedAmountNumberOnly(),
                "Total approved doesn't match");

        log.info("Click on Default rate button, collect all amount and store to file");
        this.statisticsPage.selectToggle(ChartToggle.DEFAULT_RATE);
        final List<TooltipAttr> defaultRateData = this.statisticsPage.getFinancingChartData();
        this.statisticsPage.exportChartDataWithHeader("Quarter", defaultRateData, Constants.DEFAULT_RATE_FILEPATH);

        log.info("VP: all quarter name is displayed correctly in the tooltips");
        this.softAssert.assertEquals(this.statisticsPage.getQuarterNameListFromTooltips(defaultRateData),
                this.statisticsPage.getQuarterNameListFromChart(), "Quarter name on tooltips doesn't match the xaxis");

        log.info("VP: the last quarter amount equals to Default rate on statistics view");
        this.softAssert.assertEquals(defaultRateData.get(defaultRateData.size() - 1).getAttrValue(),
                this.statisticsPage.getStatisticDetail(StatisticAttribute.DEFAULT_RATE).replace("%", ""),
                "Total approved doesn't match");
    }
}
