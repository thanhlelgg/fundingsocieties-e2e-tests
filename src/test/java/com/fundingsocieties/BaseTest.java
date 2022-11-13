package com.fundingsocieties;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.common.FileHelper;
import com.fundingsocieties.driver.DriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

@Slf4j
public class BaseTest {
    protected SoftAssert softAssert;

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(final String browser) {
        DriverUtils.startBrowser(browser);
        DriverUtils.getDriver().manage().window().maximize();
        this.softAssert = new SoftAssert();
    }

    @BeforeSuite
    public void beforeSuite() {
        log.info("Create folder to collect data");
        FileHelper.createFolder(Constants.COLLECTED_DATA_FOLDER);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        if (DriverUtils.getDriver() != null) {
            DriverUtils.getDriver().quit();
        }
        this.softAssert.assertAll();
    }
}
