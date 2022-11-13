package com.fundingsocieties.common;

import com.fundingsocieties.driver.DriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Slf4j
public class WaitHelper {

    private WaitHelper() {
    }

    public static void waitForElementAttributeToBe(final WebElement element, final String attribute, final String value,
                                                   final int timeOutInSeconds) {
        try {
            final WebDriverWait wait = new WebDriverWait(DriverUtils.getDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.attributeToBe(element, attribute, value));
        } catch (final Exception e) {
            log.debug("[waitForElementAttributeToBe] Expected element attribute is not present");
        }
    }
}
