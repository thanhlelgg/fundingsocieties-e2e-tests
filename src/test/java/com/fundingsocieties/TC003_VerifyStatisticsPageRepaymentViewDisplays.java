package com.fundingsocieties;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.enums.StatisticTab;
import com.fundingsocieties.enums.TopMenuItem;
import com.fundingsocieties.model.HighChartColumnSeriesAttr;
import com.fundingsocieties.pages.HomePage;
import com.fundingsocieties.pages.StatisticsPage;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.List;

@Slf4j
public class TC003_VerifyStatisticsPageRepaymentViewDisplays extends BaseTest {
    HomePage homePage = new HomePage();
    StatisticsPage statisticsPage = new StatisticsPage();

    @Test(testName = "TC003_VerifyStatisticsPageRepaymentViewDisplays")
    public void tc003_VerifyStatisticsPageRepaymentViewDisplays() {
        log.info("Open funding societies homepage");
        this.homePage.open();

        log.info("Click on Statistics top menu button");
        this.homePage.clickTopMenuItem(TopMenuItem.STATISTICS);

        log.info("Click on Repayment tab");
        this.statisticsPage.openStatisticTab(StatisticTab.REPAYMENT);

        log.info("Collect all amount and store to file");
        final List<HighChartColumnSeriesAttr> highChartColumnSeriesAttrList =
                this.statisticsPage.getRepaymentChartData();
        this.statisticsPage.exportHighChartColumnSeriesData(highChartColumnSeriesAttrList,
                Constants.REPAYMENT_FILEPATH);

        log.info("VP: All tracker columns are displayed correctly");
        this.softAssert.assertTrue(this.statisticsPage.doesChartColumnMatchLegend(highChartColumnSeriesAttrList));
    }
}
