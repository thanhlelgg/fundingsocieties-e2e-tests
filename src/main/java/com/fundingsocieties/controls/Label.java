package com.fundingsocieties.controls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Label extends Clickable {

    public Label(final String locator) {
        super(locator);
    }

    public Label(final BaseControl parent, final String locator) {
        super(parent, locator);
    }

    public Label(final String locator, final Object... args) {
        super(locator, args);
    }

    public Label(final BaseControl parent, final String locator, final Object... args) {
        super(parent, locator, args);
    }
}
