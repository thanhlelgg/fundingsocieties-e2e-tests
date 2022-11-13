package com.fundingsocieties.common;

import com.fundingsocieties.driver.DriverUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;


public class ElementHelper {
    private ElementHelper() {
    }
    
    public static void moveTo(final WebElement element) {
        final Actions actions = new Actions(DriverUtils.getDriver());
        actions.moveToElement(element).build().perform();
    }
}
