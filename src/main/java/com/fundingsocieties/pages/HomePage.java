package com.fundingsocieties.pages;

import com.fundingsocieties.driver.DriverUtils;

public class HomePage extends BasePage {
    private static final String PAGE_URL = "https://fundingsocieties.com/";

    public void open() {
        DriverUtils.navigateTo(PAGE_URL);
    }
}
