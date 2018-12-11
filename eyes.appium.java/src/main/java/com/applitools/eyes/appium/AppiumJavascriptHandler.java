package com.applitools.eyes.appium;

import com.applitools.eyes.Trigger;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import com.applitools.eyes.triggers.MouseTrigger;

// FIXME: 19/06/2018 Does NOT work with the new appium behavior. Requires fixing.
public class AppiumJavascriptHandler {

    private final EyesWebDriver driver;

    public AppiumJavascriptHandler (EyesWebDriver driver) {
        this.driver = driver;
    }

    public void handle(String script, Object[] args) {
        // Appium commands are sometimes sent as Javascript
        if (AppiumJsCommandExtractor.isAppiumJsCommand(script)) {
            Trigger trigger =
                    AppiumJsCommandExtractor.extractTrigger(driver.getElementIds(),
                            driver.manage().window().getSize(), script, args);

            if (trigger != null) {
                // TODO - Daniel, additional type of triggers
                if (trigger instanceof MouseTrigger) {
                    MouseTrigger mt = (MouseTrigger) trigger;
                    driver.getEyes().addMouseTrigger(mt.getMouseAction(), mt.getControl(), mt.getLocation());
                }
            }
        }
    }

}
