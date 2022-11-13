package com.fundingsocieties;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.enums.StatisticAttribute;
import com.fundingsocieties.enums.StatisticTab;
import com.fundingsocieties.enums.TopMenuItem;
import com.fundingsocieties.pages.HomePage;
import com.fundingsocieties.pages.StatisticsPage;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

@Slf4j
public class TC001_VerifyStatisticsPageMainDisplays extends BaseTest {
    HomePage homePage = new HomePage();
    StatisticsPage statisticsPage = new StatisticsPage();

    @Test(testName = "TC001_VerifyStatisticsPageMainDisplays")
    public void tc001_Verify_Statistics_Page_Main_Displays() {
        log.info("Open funding societies homepage");
        this.homePage.open();

        log.info("Click on Statistics top menu button");
        this.homePage.clickTopMenuItem(TopMenuItem.STATISTICS);

        log.info("VP. Total funded, no of financing, default rate and financing fulfillment rate are displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticDetailDisplayed(StatisticAttribute.TOTAL_FUNDED),
                "Total funded is not displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticDetailDisplayed(StatisticAttribute.NO_OF_FINANCING),
                "No of financing is not displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticDetailDisplayed(StatisticAttribute.DEFAULT_RATE),
                "Default rate is not displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticDetailDisplayed(StatisticAttribute.FULFILLMENT_RATE),
                "Fulfillment rate is not displayed");

        log.info("Export statistic details to file: " + Constants.STATISTIC_DETAILS_FILEPATH);
        this.statisticsPage.exportStatisticDetails();

        log.info("VP. General, Repayment, Disbursement tabs are displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticTabDisplayed(StatisticTab.GENERAL),
                "General tab is not displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticTabDisplayed(StatisticTab.REPAYMENT),
                "Repayment tab is not displayed");
        this.softAssert.assertTrue(this.statisticsPage.isStatisticTabDisplayed(StatisticTab.DISBURSEMENT),
                "Disbursement tab is not displayed");
    }
}
