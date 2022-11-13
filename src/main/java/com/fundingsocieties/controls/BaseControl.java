package com.fundingsocieties.controls;

import com.fundingsocieties.common.Constants;
import com.fundingsocieties.driver.DriverUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseControl {

    protected static final int MINIMUM_ELEMENT_TIME_WAIT = 2;
    protected static final int SCROLL_TIME_WAIT = 100;
    private By locator;
    private BaseControl parent;
    private String driverKey;
    private String dynamicLocator;
    private String stringLocator;
    private WebElement cachedElement;

    public BaseControl(final WebElement webElement) {
        this.cachedElement = webElement;
    }

    public BaseControl(final String locator) {
        this(null, locator);
    }

    public BaseControl(final BaseControl parent, final String locator) {
        this.parent = parent;
        this.dynamicLocator = locator;
        this.stringLocator = locator;
        this.locator = this.getLocatorFromString(locator);
    }

    public BaseControl(final String locator, final Object... args) {
        this(null, locator, args);
    }

    public BaseControl(final BaseControl parent, final String locator, final Object... args) {
        this.parent = parent;
        this.stringLocator = String.format(locator, args);
        this.locator = this.getLocatorFromString(this.stringLocator);
        this.dynamicLocator = locator;
    }

    /**
     * Map all the missing info to the element locator
     *
     * @param args all the missing info
     */
    public void setDynamicValue(final Object... args) {
        this.stringLocator = String.format(this.dynamicLocator, args);
        this.locator = this.getLocatorFromString(this.stringLocator);
    }

    protected WebDriver getWebDriver() {
        return DriverUtils.getDriver();
    }

    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) this.getWebDriver();
    }

    /**
     * Get WebElement of the current Control, if many elements found, return the first one
     * Note: this will try to get the element over and over again if StaleElementReferenceException occurs
     *
     * @return first element found by using current Control locator
     */
    public WebElement getElement() {
        if (this.cachedElement != null) {
            return this.cachedElement;
        }

        final WebElement element;
        try {
            if (this.parent != null) {
                final WebElement eleParent = this.parent.getElement();
                element = eleParent.findElement(this.getLocator());
            } else {
                element = this.getWebDriver().findElement(this.getLocator());
            }
            return element;
        } catch (final StaleElementReferenceException e) {
            log.error("StaleElementReferenceException '{}': {}", this.getFullLocator().toString(), e.getMessage()
                    .split("\n")[0]);
            return this.getElement();
        }
    }

    /**
     * Get all WebElements of the current Control
     *
     * @return all elements found by using current Control locator
     */
    public List<WebElement> getElements() {
        if (this.parent != null) {
            return this.parent.getElement().findElements(this.getLocator());
        }

        return this.getWebDriver().findElements(this.getLocator());
    }

    /**
     * Get all the elements and map it to the required Control type
     *
     * @param clazz Control class
     * @param <T>   Control class
     * @return List of Control
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseControl> List<T> getListElements(final Class<T> clazz) {
        final String js = "function getElementTreeXPath(e){for(var n=[];e&&1==e.nodeType;e=e.parentNode){"
                + "for(var o=0,r=e.previousSibling;r;r=r.previousSibling)"
                + "r.nodeType!=Node.DOCUMENT_TYPE_NODE&&r.nodeName==e.nodeName&&++o;"
                + "var t=e.nodeName.toLowerCase(),a=o?'['+(o+1)+']':'[1]';n.splice(0,0,t+a)}"
                + "return n.length?'/'+n.join('/'):null} return getElementTreeXPath(arguments[0]);";
        final List<T> result = new ArrayList<T>();
        final List<WebElement> list = this.getElements();
        for (final WebElement webElement : list) {
            try {
                final String xpath = (String) this.jsExecutor().executeScript(js, webElement);
                final Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                final T element = (T) ctor.newInstance(xpath);
                result.add(element);
            } catch (final Exception e) {
                log.error("Exception occurred when getting element list by '{}': {}", clazz, e);
            }
        }
        return result;
    }

    /**
     * Get inner text of current element, this will try multiple approach to get the element text before returning empty
     *
     * @return element inner text
     */
    public String getText() {
        String ret = "";
        try {
            log.debug("Get text of element '{}'", this.getFullLocator().toString());
            ret = this.getElement().getText();
            if (ret == null || ret.isEmpty()) {
                ret = this.getElement().getAttribute("value");
                if (ret == null || ret.isEmpty()) {
                    ret = this.getElement().getAttribute("innerText");
                }
            }
        } catch (final Exception e) {
            log.error("Exception occurred when getting text of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            throw e;
        }

        return ret;
    }

    /**
     * Get attribute value by attributes name
     *
     * @param attributeName attributes name
     * @return attributes value
     */
    public String getAttribute(final String attributeName) {
        try {
            log.debug("Get attribute '{}' of element '{}'", attributeName, this.getFullLocator().toString());
            return this.getElement().getAttribute(attributeName);
        } catch (final Exception e) {
            log.error("Exception occurred when getting attribute '{}' of '{}': {}",
                    attributeName,
                    this.getFullLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * Get class name of current element
     *
     * @return class name of current element
     */
    public String getClassName() {
        return this.getAttribute("class");
    }

    /**
     * Find the child element of current element, by searching inside the DOM of current element only
     *
     * @param xpath xpath locator of child element.
     *              should start with a dot, to search from within the current element node
     * @return WebElement
     */
    public WebElement getChildElement(final String xpath) {
        return this.getElement().findElement(By.xpath(xpath));
    }

    /**
     * Find the child element of current element, by searching inside the DOM of current element only
     *
     * @param by By strategy to locate the element
     * @return WebElement
     */
    public WebElement getChildElement(final By by) {
        return this.getElement().findElement(by);
    }

    /**
     * Get all direct child elements of the current element, by searching inside the DOM of current element only
     *
     * @return list of WebElement
     */
    public List<WebElement> getChildElements() {
        return this.getChildElements("./*");
    }

    /**
     * Get all child elements of the current element, by searching inside the DOM of current element only
     *
     * @param xpath xpath locator of child element.
     *              Should start with a dot, to search from within the current element node
     * @return list of WebElement
     */
    public List<WebElement> getChildElements(final String xpath) {
        return this.getElement().findElements(By.xpath(xpath));
    }

    /**
     * Get all child elements of the current element
     *
     * @param by By strategy to locate the element
     * @return list of WebElement
     */
    public List<WebElement> getChildElements(final By by) {
        return this.getElement().findElements(by);
    }

    /**
     * Wait until element clickable
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if the element is clickable, false otherwise.
     * @deprecated use {@link #waitForElementClickable(int)} instead
     */
    public boolean isClickable(final int timeOutInSeconds) {
        try {
            log.debug("Checking element '{}' is clickable", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            return (wait.until(ExpectedConditions.elementToBeClickable(this.getFullLocator())) != null);
        } catch (final Exception e) {
            log.error("Exception occurred when checking clickable of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * Is the element currently enabled or not?
     * This will generally return true for everything but disabled input elements.
     *
     * @return True if the element is enabled, false otherwise.
     */
    public boolean isEnabled() {
        try {
            log.debug("Checking element '{}' is enabled", this.getFullLocator().toString());
            return this.getElement().isEnabled();
        } catch (final Exception e) {
            log.error("Exception occurred when checking enable of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * Check if element presence in the DOM, the element is not necessarily displayed for this to return true
     * * @return True if element presence, false otherwise
     */
    public boolean doesExist() {
        return this.getElements().size() > 0;
    }

    /**
     * Wait until element exists in the DOM, the element is not necessarily displayed for this to return true
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element presence, false otherwise
     * @deprecated use {@link #waitForElementPresence(int)} instead
     */
    public boolean doesExist(final int timeOutInSeconds) {
        try {
            log.debug("Checking element '{}' is exist", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            return (wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(this.getFullLocator())) != null);
        } catch (final Exception e) {
            log.error("Exception occurred when checking exist of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * Wait until element exists in the DOM
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element presence, false otherwise
     */
    public boolean waitForElementPresence(final int timeOutInSeconds) {
        try {
            log.debug("Checking element '{}' is exist", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            return (wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(this.getFullLocator())) != null);
        } catch (final Exception e) {
            log.error("Exception occurred when checking exist of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * Determine whether this element is selected or not.
     * This operation only applies to input elements such as checkboxes, options in a select and radio buttons.
     *
     * @return True if element is selected, false otherwise
     */
    public boolean isSelected() {
        try {
            log.debug("Checking element '{}' is selected", this.getFullLocator().toString());
            return this.getElement().isSelected();
        } catch (final Exception e) {
            log.error("Exception occurred when checking selected of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * Wait until all elements present on the web page that match the locator are visible
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element is visible, false otherwise
     * @deprecated Use {@link #waitForVisibility(int)} instead
     */
    public boolean isVisible(final int timeOutInSeconds) {
        try {
            log.debug("Checking element '{}' is visible", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            return (wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(this.getFullLocator())) != null);
        } catch (final Exception e) {
            log.error("Exception occurred when checking visible of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            return false;
        }
    }

    /**
     * click-and-hold at the location of the source element, moves by a given offset, then releases the mouse.
     *
     * @param xOffset horizontal move offset.
     * @param yOffset vertical move offset.
     */
    public void dragAndDrop(final int xOffset, final int yOffset) {
        final Actions actions = new Actions(this.getWebDriver());
        actions.dragAndDropBy(this.getElement(), xOffset, yOffset).build().perform();
    }

    /**
     * click-and-hold at the location of the source element, moves by a given offset, then releases the mouse.
     *
     * @param target element to move to and release the mouse at.
     */
    public void dragAndDrop(final BaseControl target) {
        final Actions actions = new Actions(this.getWebDriver());
        actions.dragAndDrop(this.getElement(), target.getElement()).build().perform();
    }

    /**
     * Give focus to the current element (if it can be focused)
     */
    public void focus() {
        this.jsExecutor().executeScript("arguments[0].focus();", this.getElement());
    }

    /**
     * hover this element using js
     */
    public void mouseHoverJScript() {
        final String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);}"
                + " else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        this.jsExecutor().executeScript(mouseOverScript, this.getElement());
    }

    /**
     * Moves the mouse to the middle of the element.
     * The element is scrolled into view and its location is calculated using getBoundingClientRect.
     */
    public void moveTo() {
        final Actions actions = new Actions(this.getWebDriver());
        actions.moveToElement(this.getElement()).build().perform();
    }

    /**
     * Moves the mouse from its current position (or 0,0) to the current element with the given offset.
     * If the coordinates provided are outside the viewport (the mouse will end up outside the browser window)
     * then the viewport is scrolled to match.
     *
     * @param x horizontal offset. A negative value means moving the mouse left.
     * @param y vertical offset. A negative value means moving the mouse up.
     */
    public void moveTo(final int x, final int y) {
        final WebElement element = this.getElement();
        final int absX = element.getLocation().x + x;
        final int absY = element.getLocation().y + y;

        final Actions actions = new Actions(this.getWebDriver());
        actions.moveByOffset(absX, absY).build().perform();
    }

    /**
     * Moves the mouse from its current position (or 0,0) to the center of current element
     */
    public void moveToCenter() {
        final WebElement element = this.getElement();
        final int x = element.getLocation().x + element.getSize().width / 2;
        final int y = element.getLocation().y + element.getSize().height / 2;

        final Actions actions = new Actions(this.getWebDriver());
        actions.moveByOffset(x, y).build().perform();
    }

    /**
     * Scroll the element to the center screen
     */
    public void scrollElementToCenterScreen() {
        final String js = "Element.prototype.documentOffsetTop=function(){return this.offsetTop+(this.offsetParent?"
                + "this.offsetParent.documentOffsetTop():0)};var top=arguments[0].documentOffsetTop()"
                + "-window.innerHeight/2;window.scrollTo(0,top);";
        try {
            this.jsExecutor().executeScript(js, this.getElement());
        } catch (final StaleElementReferenceException exception) {
            log.error("StaleElementReferenceException '{}': {}", this.getFullLocator().toString(),
                    exception.getMessage()
                            .split("\n")[0]);
            this.scrollElementToCenterScreen();
        }
    }

    /**
     * Scrolls the current element into the visible area of the browser window
     */
    public void scrollToView() {
        this.scrollToView(0, 0);
    }

    /**
     * Try to scroll the current element into the visible area of the browser window using scrollIntoView method.
     * If failed, scroll using the given fallback offset.
     *
     * @param fallbackOffsetX horizontal offset. A negative value means moving the mouse left.
     * @param fallbackOffsetY vertical offset. A negative value means moving the mouse up.
     */
    public void scrollToView(final int fallbackOffsetX, final int fallbackOffsetY) {
        try {
            this.jsExecutor().executeScript("arguments[0].scrollIntoView(true);", this.getElement());
        } catch (final JavascriptException e) {
            final WebElement element = this.getElement();
            final int x = element.getRect().x + fallbackOffsetX;
            final int y = element.getRect().y + fallbackOffsetY;
            final String js = String.format("window.scrollTo(%s, %s);", x, y);
            this.jsExecutor().executeScript(js);
        } catch (final StaleElementReferenceException exception) {
            log.error("StaleElementReferenceException '{}': {}", this.getFullLocator().toString(),
                    exception.getMessage()
                            .split("\n")[0]);
            this.scrollToView(fallbackOffsetX, fallbackOffsetY);
        }
    }

    /**
     * Retry an action until it no longer failed by Staleness exception, only use this at last resort
     *
     * @param actions
     * @param retryTime
     */
    public void retryIfStaleness(final Runnable actions, final int retryTime) {
        if (retryTime <= 0) {
            log.error(String.format("[retryIfStaleness] Action failed to executed after %s tries", retryTime));
            return;
        }
        try {
            actions.run();
        } catch (final StaleElementReferenceException exception) {
            log.error("StaleElementReferenceException '{}': {}", this.getFullLocator().toString(),
                    exception.getMessage()
                            .split("\n")[0]);
            this.retryIfStaleness(actions, retryTime - 1);
        }
    }

    /**
     * Set current element attribute using js
     *
     * @param attributeName attribute name
     * @param value         attribute value
     */
    public void setAttributeJS(final String attributeName, final String value) {
        try {
            log.debug("Set value '{}' for attribute '{}' of '{}'", value, attributeName,
                    this.getFullLocator().toString());
            this.jsExecutor()
                    .executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                            this.getElement());
        } catch (final Exception e) {
            log.error("Exception occurred when set value '{}' for attribute '{}' of '{}': {}",
                    value,
                    attributeName,
                    this.getFullLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * If this current element is a form, or an element within a form, then this will be submitted to the remote server.
     * If this causes the current page to change, then this method will block until the new page is loaded.
     */
    public void submit() {
        this.getElement().submit();
    }

    /**
     * Wait until the element no longer exists on the DOM
     *
     * @return True if element doesn't exist, false otherwise
     */
    public boolean waitForDisappear() {
        return this.waitForDisappear(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until the element no longer exists on the DOM
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element doesn't exist, false otherwise
     */
    public boolean waitForDisappear(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be disappear", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.numberOfElementsToBe(this.getFullLocator(), 0));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be disappear: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element visible in the DOM
     *
     * @param timeOutInSeconds
     * @return
     * @deprecated use {@link #waitForElementPresence(int)} instead, will be removed in the next major update
     */
    public boolean waitForDisplay(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be displayed", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.presenceOfElementLocated(this.getFullLocator()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be displayed: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element is clickable
     *
     * @return True if clickable, false otherwise
     */
    public boolean waitForElementClickable() {
        return this.waitForElementClickable(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is clickable
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if clickable, false otherwise
     */
    public boolean waitForElementClickable(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be clickable", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.elementToBeClickable(this.getFullLocator()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be clickable: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element is disabled
     *
     * @return True if element is disabled, False otherwise
     */
    public boolean waitForElementDisabled() {
        return this.waitForElementDisabled(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is disabled
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element is disabled, False otherwise
     */
    public boolean waitForElementDisabled(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be disabled", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(driver -> !this.getElement().isEnabled());
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be disabled: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element is enabled
     *
     * @return True if element is enabled, false otherwise
     */
    public boolean waitForElementEnabled() {
        return this.waitForElementEnabled(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is enabled
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element is enabled, false otherwise
     */
    public boolean waitForElementEnabled(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be enabled", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(driver -> this.getElement().isEnabled());
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be enabled: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element is visible
     *
     * @return True if element is visible, false otherwise
     */
    public boolean waitForVisibility() {
        return this.waitForVisibility(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is visible
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element is visible, false otherwise
     */
    public boolean waitForVisibility(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for visibility of '{}'", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(this.getFullLocator()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for visibility of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until element is not visible
     *
     * @return True if element is not visible, false otherwise
     */
    public boolean waitForInvisibility() {
        return this.waitForInvisibility(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is not visible
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if element is not visible, false otherwise
     */
    public boolean waitForInvisibility(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for invisibility of '{}'", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(this.getFullLocator()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for invisibility of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until all elements are not visible
     *
     * @param timeOutInSeconds timeout in seconds
     * @return True if all elements are not visible, false otherwise
     */
    public boolean waitForAllElementsInvisibility(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for all elements invisibility of '{}'", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.invisibilityOfAllElements(this.getElements()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for all elements invisibility of '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until number of element to be more than input number
     *
     * @param number           number of element which you want to wait
     * @param timeOutInSeconds timeout in seconds
     * @return True if number of element to be more than input number, false otherwise
     */
    public boolean waitForNumberOfElementsToBeMoreThan(final int number, final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for number of elements '{}' to be more than '{}'",
                    this.getFullLocator().toString(), number);
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(this.getFullLocator(), number));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for number of elements '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until number of element to be less than input number
     *
     * @param number           number of element which you want to wait
     * @param timeOutInSeconds timeout in seconds
     * @return True if number of element to be less than input number, false otherwise
     */
    public boolean waitForNumberOfElementsToBeLessThan(final int number, final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for number of elements '{}' to be less than '{}'",
                    this.getFullLocator().toString(), number);
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.numberOfElementsToBeLessThan(this.getFullLocator(), number));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for number of elements '{}': {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until text is not present in the element
     *
     * @param text text to not be expected
     * @return True if text is not present, false otherwise
     */
    public boolean waitForTextToBeNotPresent(final String text) {
        return this.waitForTextToBeNotPresent(text, Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until text is not present in the element
     *
     * @param text             text to not be expected
     * @param timeOutInSeconds timeout in seconds
     * @return True if text is not present, false otherwise
     */
    public boolean waitForTextToBeNotPresent(final String text, final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be not present in '{}'", text, this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(this.getElement(), text)));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to not be present in '{}': {}",
                    text,
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until text present in the element
     *
     * @param text text to be expected
     * @return True if text present, false otherwise
     */
    public boolean waitForTextToBePresent(final String text) {
        return this.waitForTextToBePresent(text, Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until text present in the element
     *
     * @param text             text to be expected
     * @param timeOutInSeconds timeout in seconds
     * @return True if text present, false otherwise
     */
    public boolean waitForTextToBePresent(final String text, final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be present in '{}'", text, this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.textToBePresentInElement(this.getElement(), text));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be present in '{}': {}",
                    text,
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until text matches the pattern in the element
     *
     * @param pattern text to be expected
     * @return True if text present, false otherwise
     */
    public boolean waitForTextToMatch(final String pattern) {
        return this.waitForTextToMatch(pattern, Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until text matches the pattern in the element
     *
     * @param pattern          regex to be expected
     * @param timeOutInSeconds timeout in seconds
     * @return True if text present, false otherwise
     */
    public boolean waitForTextToMatch(final String pattern, final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for text to match '{}' in '{}'", pattern, this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.textMatches(this.getFullLocator(), Pattern.compile(pattern)));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for text to match '{}' in '{}': {}",
                    pattern,
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until value is not present in attribute
     *
     * @param attribute attribute name
     * @param value     attribute value to not be expected
     * @return True if value is not present in attribute, false otherwise
     */
    public boolean waitForValueNotPresentInAttribute(final String attribute, final String value) {
        return this.waitForValueNotPresentInAttribute(attribute, value, Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until value is not present in attribute
     *
     * @param attribute        attribute name
     * @param value            attribute value to not be expected
     * @param timeOutInSeconds timeout in seconds
     * @return True if value is not present in attribute, false otherwise
     */
    public boolean waitForValueNotPresentInAttribute(final String attribute,
                                                     final String value,
                                                     final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be not present in '{}' of '{}'", value, attribute,
                    this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            if (this.cachedElement != null) {
                wait.until(ExpectedConditions.not(ExpectedConditions.attributeToBe(
                        this.cachedElement, attribute, value)));
            } else {
                wait.until(ExpectedConditions.not(ExpectedConditions.attributeToBe(
                        this.getFullLocator(), attribute, value)));
            }

        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be not present in '{}' of '{}': {}",
                    value,
                    attribute,
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until value is presented in attribute
     *
     * @param attribute attribute name
     * @param value     attribute value to be expected
     * @return True if value is present in attribute, false otherwise
     */
    public boolean waitForValuePresentInAttribute(final String attribute, final String value) {
        return this.waitForValuePresentInAttribute(attribute, value, Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is stale
     *
     * @return
     */
    public boolean waitForStalenessOfElement() {
        return this.waitForStalenessOfElement(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Wait until element is stale
     *
     * @param timeOutInSeconds
     * @return
     */
    public boolean waitForStalenessOfElement(final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.info("Wait for '{}' to be stale", this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.stalenessOf(
                    getWebDriver().findElement(this.getFullLocator())));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be stale: {}",
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    /**
     * Wait until value is presented in attribute
     *
     * @param attribute        attribute name
     * @param value            attribute value to be expected
     * @param timeOutInSeconds timeout in seconds
     * @return True if value is present in attribute, false otherwise
     */
    public boolean waitForValuePresentInAttribute(final String attribute,
                                                  final String value,
                                                  final int timeOutInSeconds) {
        boolean ret = true;
        try {
            log.debug("Wait for '{}' to be present in {} of {}", value, attribute, this.getFullLocator().toString());
            final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeOutInSeconds));
            wait.until(ExpectedConditions.attributeToBe(this.getFullLocator(), attribute, value));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for '{}' to be present in '{}' of '{}': {}",
                    value,
                    attribute,
                    this.getFullLocator().toString(),
                    e);
            ret = false;
        }
        return ret;
    }

    private By getLocatorFromString(final String locator) {
        final String body = this.getLocatorBody(locator);
        final String type = this.getLocatorType(locator);

        //xpath is the default locator type, if none specified
        if (type.equals(locator)) {
            return By.xpath(locator);
        }

        switch (type.toLowerCase()) {
            case "css":
                return By.cssSelector(body);
            case "id":
                return By.id(body);
            case "class":
                return By.className(body);
            case "link":
                return By.linkText(body);
            case "xpath":
                return By.xpath(body);
            case "text":
                return By.xpath(String.format("//*[contains(text(), '%s')]", body));
            case "name":
                return By.name(body);
            default:
                throw new RuntimeException(String.format("Locator type: %s is not supported.", type));
        }
    }

    private String stringLocatorToXpath(final String locator) {
        final String body = this.getLocatorBody(locator);
        final String type = this.getLocatorType(locator);

        //xpath is the default locator type, if none specified
        if (type.equals(locator)) {
            return locator;
        }

        switch (type.toLowerCase()) {
            case "css":
                //todo: support css to xpath when it's necessary to do so
                throw new RuntimeException("Css to Xpath is not supported right now. Please another locator strategy");
            case "id":
                return String.format("//*[@id='%s']", body);
            case "class":
                return String.format("//*[@class='%s']", body);
            case "link":
                return String.format("//a[text()='%s']", body);
            case "xpath":
                return body;
            case "text":
                return String.format("//*[contains(text(), '%s')]", body);
            case "name":
                return String.format("//*[@name='%s']", body);
            default:
                throw new RuntimeException(String.format("Locator type: %s is not supported.", type));
        }
    }

    private String getLocatorBody(final String locator) {
        return locator.replaceAll("^[\\w\\s]*=(.*)", "$1").trim();
    }

    private String getLocatorType(final String locator) {
        final String locatorType = locator.replaceAll("(^[\\w\\s]*)=.*", "$1").trim();
        return locatorType.equals(locator) ? "xpath" : locatorType;
    }

    /**
     * This will find the locator that can be used directly from Web Driver to find element,
     * useful when we use the constructor with parent locator,
     * where we need to get direct locator to use in "waitFor..." functions
     *
     * @return full locator which can be used directly from Web Driver to find element
     */
    public By getFullLocator() {
        if (this.parent == null) {
            return this.getLocator();
        }
        final String parentLocator = this.parent.getStringLocator();
        final String parentLocatorType = this.getLocatorType(parentLocator);
        final String parentLocatorBody = this.getLocatorBody(parentLocator);
        final String childLocator = this.getStringLocator();
        final String locatorType = this.getLocatorType(childLocator);
        String locatorBody = this.getLocatorBody(childLocator);
        //child locator xpath start with a dot, remove them before combine locator
        if (locatorType.equals("xpath")) {
            locatorBody = locatorBody.replaceAll("(^\\.)", "");
        }
        //currently, we don't support translation between css and another strategy
        if ((parentLocatorType.equals("css") && !locatorType.equals("css"))
                || (!parentLocatorType.equals("css") && locatorType.equals("css"))) {
            throw new RuntimeException(
                    "Can't find the full locator, if parent locator is css, "
                            + "then child locator must also be css and vice versa");
        } else if (parentLocatorType.equals("css")) {
            return By.cssSelector(String.format("%s %s", parentLocatorBody, locatorBody));
        }

        return By.xpath(this.stringLocatorToXpath(parentLocatorBody) + this.stringLocatorToXpath(locatorBody));
    }

    /**
     * Get the value of a given CSS property
     * Note that shorthand CSS properties (e.g. background, font, border...) are not returned,
     * you should directly access the longhand properties (e.g. background-color) to access the desired values.
     *
     * @param cssProperty css property name to get
     * @return css value
     */
    public String getCSSValue(final String cssProperty) {
        try {
            log.debug("Get css value '{}' of element '{}'", cssProperty, this.getFullLocator().toString());
            return this.getElement().getCssValue(cssProperty);
        } catch (final Exception e) {
            log.error("Exception occurred when getting css '{}' of '{}': {}",
                    cssProperty,
                    this.getFullLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * Use this method to simulate typing into an element, which may set its value.
     *
     * @param keys keys to be entered
     */
    public void sendKeys(final String keys) {
        try {
            log.debug("Send keys '{}' of element '{}'", keys, this.getFullLocator().toString());
            this.getElement().sendKeys(keys);
        } catch (final Exception e) {
            log.error("Exception occurred when sending keys '{}' of '{}': {}",
                    keys,
                    this.getFullLocator().toString(),
                    e);
            throw e;
        }
    }

    /**
     * Check if is element is displayed, this won't wait for element.
     * To wait until element visible use {@link #waitForVisibility(int)}
     *
     * @return True if element is displayed, false otherwise
     */
    public boolean isDisplayed() {
        return this.doesExist() && this.getElement().isDisplayed();
    }

    /**
     * Check if element is visible, this will wait for the element with the default timeout
     *
     * @return True if visible, False otherwise
     * @deprecated use {@link #waitForVisibility()} instead
     */
    public boolean isVisible() {
        return this.waitForVisibility(Constants.DEFAULT_TIME_WAIT);
    }

    /**
     * Scroll until the element is present with default time wait
     */
    public void scrollToFindElement() {
        this.scrollToFindElement(SCROLL_TIME_WAIT);
    }

    /**
     * Scroll until the element is present
     *
     * @param timeoutInSeconds timeout in seconds
     */
    public void scrollToFindElement(final int timeoutInSeconds) {
        try {
            final WebDriverWait webDriverWait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeoutInSeconds));
            webDriverWait.until(this.ecScrollElementToView(this));
            this.scrollToView();
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for scrolling to find '{}': {}",
                    this.getFullLocator().toString(),
                    e);
        }
    }

    /**
     * Scroll until the element is present
     *
     * @param timeoutInSeconds timeout in seconds
     */
    public void waitForValueChanges(final int timeoutInSeconds) {
        try {
            final WebDriverWait webDriverWait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(timeoutInSeconds));
            webDriverWait.until(this.ecElementTextChanges(this, this.getText()));
        } catch (final Exception e) {
            log.warn("Exception occurred when waiting for scrolling to find '{}': {}",
                    this.getFullLocator().toString(),
                    e);
        }
    }

    private ExpectedCondition<Boolean> ecElementTextChanges(final BaseControl control, final String currentText) {
        return driver -> {
            if (!control.waitForVisibility(1) || control.getText().equals("")) {
                return false;
            }
            return control.getText().equals(currentText);
        };
    }

    private ExpectedCondition<Boolean> ecScrollElementToView(final BaseControl control) {
        final int maxScrollHeight = ((Long) DriverUtils.execJavaScript("return document.body.scrollHeight")).intValue();
        //use array because only final variable can be accessed in lambda
        final boolean[] isAtBottom = {false};
        final int[] numOfScroll = {1};
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                boolean ret = false;

                final List<WebElement> elements = driver.findElements(control.getFullLocator());
                if (elements.size() > 0) {
                    log.info("[ecScrollElementToView] found elements");
                    ret = true;
                } else {
                    if (isAtBottom[0]) {
                        throw new RuntimeException("[ecScrollElementToView] At the end of page, Element not found!");
                    }
                    final int y = BaseControl.this.getWebDriver().manage().window().getSize().getHeight();
                    int scrollToYAxis = y * numOfScroll[0];
                    if (scrollToYAxis > maxScrollHeight) {
                        scrollToYAxis = maxScrollHeight;
                        isAtBottom[0] = true;
                    }
                    final Point endPoint = new Point(0, scrollToYAxis);
                    numOfScroll[0]++;
                    DriverUtils.scrollTo(endPoint);
                }
                return ret;
            }
        };
    }
}

