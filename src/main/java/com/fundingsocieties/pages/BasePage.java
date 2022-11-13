package com.fundingsocieties.pages;

import com.fundingsocieties.controls.Button;
import com.fundingsocieties.enums.TopMenuItem;

public class BasePage {
    private static final Button dynBtnTopMenuItem = new Button("//li[@class='nav-menu__item']/a[text()='%s']");

    public void clickTopMenuItem(final TopMenuItem menuItem) {
        dynBtnTopMenuItem.setDynamicValue(menuItem.getItemName());
        dynBtnTopMenuItem.waitForElementClickable();
        dynBtnTopMenuItem.click();
    }
}
