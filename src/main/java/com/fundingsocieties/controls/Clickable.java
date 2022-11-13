package com.fundingsocieties.controls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@AllArgsConstructor
public class Clickable
        extends BaseControl {

    private final Logger log = LoggerFactory.getLogger(Clickable.class);

    public Clickable(final String locator) {
        super(locator);
    }

    public Clickable(final WebElement element) {
        super(element);
    }

    public Clickable(final BaseControl parent, final String locator) {
        super(parent, locator);
    }

    public Clickable(final String locator, final Object... args) {
        super(locator, args);
    }

    public Clickable(final BaseControl parent, final String locator, final Object... args) {
        super(parent, locator, args);
    }

    /**
     * Scroll the element into view then click
     */
    public void click() {
        this.scrollElementToCenterScreen();
        this.clickWithoutScroll();
    }

    /**
     * Click the element
     */
    public void clickWithoutScroll() {
        try {
            this.getElement().click();
        } catch (final StaleElementReferenceException exception) {
            this.log.error(exception.getMessage());
            this.clickWithoutScroll();
        }
    }

    /**
     * Click and retry until element disappear
     *
     * @param retryTimes number of retries attempt
     */
    public void click(final int retryTimes) {
        if (retryTimes <= 0) {
            throw new RuntimeException("Element is still visible after multiple attempt");
        }
        this.click();
        if (!this.waitForDisappear(MINIMUM_ELEMENT_TIME_WAIT)) {
            this.click(retryTimes - 1);
        }
    }

    /**
     * Move to element with offset and then click
     *
     * @param x horizontal offset
     * @param y vertical offset
     */
    public void click(final int x, final int y) {
        try {
            this.log.debug("Wait for click on '{}'", this.getLocator().toString());
            new Actions(this.getWebDriver()).moveToElement(this.getElement(), x, y).click().build().perform();
        } catch (final Exception e) {
            this.log.error("Exception occurred when click on '{}'",
                    this.getLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * Click using javascript
     */
    public void clickByJs() {
        try {
            this.log.debug("Wait for click on '{}'", this.getLocator().toString());
            this.jsExecutor().executeScript("arguments[0].click();", this.getElement());
        } catch (final Exception e) {
            this.log.error("Exception occurred when click on '{}'",
                    this.getLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * Performs a double click at middle of the given element
     */
    public void doubleClick() {
        try {
            this.log.debug("Wait for double click on '{}'", this.getLocator().toString());
            new Actions(this.getWebDriver()).doubleClick(this.getElement()).build().perform();
        } catch (final Exception e) {
            this.log.error("Exception occurred when double click on '{}'",
                    this.getLocator().toString(),
                    e);
            throw e;
        }
    }
}
