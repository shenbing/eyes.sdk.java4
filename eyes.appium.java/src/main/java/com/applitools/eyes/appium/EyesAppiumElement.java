package com.applitools.eyes.appium;

import com.applitools.eyes.Logger;
import com.applitools.eyes.selenium.wrappers.EyesRemoteWebElement;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public class EyesAppiumElement extends EyesRemoteWebElement {

    private Dimension size;

    public EyesAppiumElement (Logger logger, EyesWebDriver driver, WebElement element) {
        super(logger, driver, element);
    }

    protected Dimension getCachedSize() {
        if (size == null) {
            size = webElement.getSize();
        }
        return size;
    }

    @Override
    public int getClientWidth () {
        return getCachedSize().width;
    }

    @Override
    public int getClientHeight () {
        return getCachedSize().height;
    }

    @Override
    public int getComputedStyleInteger (String propStyle) {
        return 0;
    }

}
