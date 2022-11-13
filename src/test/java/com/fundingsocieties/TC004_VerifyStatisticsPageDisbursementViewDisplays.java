package com.fundingsocieties;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.enums.StatisticTab;
import com.fundingsocieties.enums.TopMenuItem;
import com.fundingsocieties.model.TooltipAttr;
import com.fundingsocieties.pages.HomePage;
import com.fundingsocieties.pages.StatisticsPage;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j
public class TC004_VerifyStatisticsPageDisbursementViewDisplays extends BaseTest {
    HomePage homePage = new HomePage();
    StatisticsPage statisticsPage = new StatisticsPage();

    @Test(testName = "TC004_VerifyStatisticsPageDisbursementViewDisplays")
    public void tc004_VerifyStatisticsPageDisbursementViewDisplays() {
        log.info("Open funding societies homepage");
        this.homePage.open();

        log.info("Click on Statistics top menu button");
        this.homePage.clickTopMenuItem(TopMenuItem.STATISTICS);

        log.info("Click on Disbursement tab");
        this.statisticsPage.openStatisticTab(StatisticTab.DISBURSEMENT);

        log.info("Hover on each slice, collect all industry names and store to file");
        final List<TooltipAttr> industryChartDataData = this.statisticsPage.getIndustryChartData();
        this.statisticsPage.exportChartDataWithHeader("Industry name", industryChartDataData,
                Constants.INDUSTRY_DATA_FILEPATH);

        log.info("VP: industry name is not duplicated");
        this.softAssert.assertTrue(this.statisticsPage.isPieChartSliceDuplicated(industryChartDataData),
                "Industry name is duplicated");

        log.info("VP: total percentage of all slice should be added up to 100%");
        this.softAssert.assertEquals(this.statisticsPage.getPieChartTotalPercentage(industryChartDataData), 100D,
                "Total pie chart percentage is not correct");
    }
}
