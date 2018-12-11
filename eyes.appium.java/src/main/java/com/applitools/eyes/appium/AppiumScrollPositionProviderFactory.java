package com.applitools.eyes.appium;

import com.applitools.eyes.Logger;

public class AppiumScrollPositionProviderFactory {

    private Logger logger;
    private EyesAppiumDriver driver;

    public AppiumScrollPositionProviderFactory(Logger logger, EyesAppiumDriver driver) {
        this.logger = logger;
        this.driver = driver;
    }

    public AppiumScrollPositionProvider getScrollPositionProvider() {
        if (EyesAppiumUtils.isAndroid(driver.getRemoteWebDriver())) {
            return new AndroidScrollPositionProvider(logger, driver);
        } else if (EyesAppiumUtils.isIOS(driver.getRemoteWebDriver())) {
            return new IOSScrollPositionProvider(logger, driver);
        }
        throw new Error("Could not find driver type to get scroll position provider");
    }

}
