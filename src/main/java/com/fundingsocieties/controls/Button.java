package com.fundingsocieties.controls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebElement;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Button
        extends Clickable {

    public Button(final String locator) {
        super(locator);
    }

    public Button(final WebElement element) {
        super(element);
    }

    public Button(final BaseControl parent, final String locator) {
        super(parent, locator);
    }

    public Button(final String locator, final Object... args) {
        super(locator, args);
    }

    public Button(final BaseControl parent, final String locator, final Object... args) {
        super(parent, locator, args);
    }
}
