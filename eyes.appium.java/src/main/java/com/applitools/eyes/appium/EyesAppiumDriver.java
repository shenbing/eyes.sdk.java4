package com.applitools.eyes.appium;

import com.applitools.eyes.Logger;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Trigger;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.positioning.ImageRotation;
import com.applitools.eyes.selenium.wrappers.EyesRemoteWebElement;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import com.applitools.eyes.triggers.MouseTrigger;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.*;

public class EyesAppiumDriver extends EyesWebDriver {

    private Map<String, Object> sessionDetails;

    public EyesAppiumDriver(Logger logger, Eyes eyes, AppiumDriver driver) {
        super(logger, eyes, driver);
    }

    @Override
    public AppiumDriver getRemoteWebDriver () { return (AppiumDriver) this.driver; }


    public EyesAppiumElement getEyesElement (WebElement element) {
        if (element instanceof EyesAppiumElement) {
            return (EyesAppiumElement) element;
        }
        // this next if-block is just for sanity checking until everything is working
        if (element instanceof EyesRemoteWebElement) {
            throw new Error("Programming error: should not have sent an EyesRemoteWebElement in");
        }
        return new EyesAppiumElement(logger, this, element);
    }

    private Map<String, Object> getCachedSessionDetails () {
        if(sessionDetails == null) {
            logger.verbose("Retrieving session details and caching the result...");
            sessionDetails = getRemoteWebDriver().getSessionDetails();
        }
        return sessionDetails;
    }

    public HashMap<String, Integer> getViewportRect () {
        Map<String, Long> rectMap = (Map<String, Long>) getCachedSessionDetails().get("viewportRect");
        HashMap<String, Integer> intRectMap = new HashMap<String, Integer>();
        intRectMap.put("width", rectMap.get("width").intValue());
        intRectMap.put("height", rectMap.get("height").intValue());
        return intRectMap;
    }

    public int getStatusBarHeight() {
        return ((Long) getCachedSessionDetails().get("statBarHeight")).intValue();
    }

    public double getDevicePixelRatio () {
        return ((Long) getCachedSessionDetails().get("pixelRatio")).doubleValue();
    }

    /**
     * @param forceQuery If true, we will perform the query even if we have a cached viewport size.
     * @return The viewport size of the default content (outer most frame).
     */
    public RectangleSize getDefaultContentViewportSize(boolean forceQuery) {
        logger.verbose("getDefaultContentViewportSize(forceQuery: " + forceQuery + ")");

        if (defaultContentViewportSize != null && !forceQuery) {
            logger.verbose("Using cached viewport size: " + defaultContentViewportSize);
            return defaultContentViewportSize;
        }

        HashMap<String, Integer> rect = getViewportRect();
        double dpr = getDevicePixelRatio();
        defaultContentViewportSize = (new RectangleSize(rect.get("width"), rect.get("height"))).scale(1/dpr);
        logger.verbose("Done! Viewport size: " + defaultContentViewportSize);

        return defaultContentViewportSize;
    }

    public Object executeScript(String script, Object... args) {

        // Appium commands are sometimes sent as Javascript
        if (AppiumJsCommandExtractor.isAppiumJsCommand(script)) {
            Trigger trigger =
                    AppiumJsCommandExtractor.extractTrigger(getElementIds(),
                            getRemoteWebDriver().manage().window().getSize(), script, args);

            if (trigger != null) {
                // TODO - Daniel, additional types of triggers
                if (trigger instanceof MouseTrigger) {
                    MouseTrigger mt = (MouseTrigger) trigger;
                    eyes.addMouseTrigger(mt.getMouseAction(), mt.getControl(), mt.getLocation());
                }
            }
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        Object result = driver.executeScript(script, args);
        return result;
    }

    public Object executeAsyncScript(String script, Object... args) {

        // Appium commands are sometimes sent as Javascript
        if (AppiumJsCommandExtractor.isAppiumJsCommand(script)) {
            Trigger trigger =
                    AppiumJsCommandExtractor.extractTrigger(elementsIds,
                            driver.manage().window().getSize(), script, args);

            if (trigger != null) {
                // TODO - Daniel, additional type of triggers
                if (trigger instanceof MouseTrigger) {
                    MouseTrigger mt = (MouseTrigger) trigger;
                    eyes.addMouseTrigger(mt.getMouseAction(),
                            mt.getControl(), mt.getLocation());
                }
            }
        }

        return driver.executeAsyncScript(script, args);
    }

    /**
     * Rotates the image as necessary. The rotation is either manually forced
     * by passing a non-null ImageRotation, or automatically inferred.
     * @param driver   The underlying driver which produced the screenshot.
     * @param image    The image to normalize.
     * @param rotation The degrees by which to rotate the image:
     *                 positive values = clockwise rotation,
     *                 negative values = counter-clockwise,
     *                 0 = force no rotation,
     *                 null = rotate automatically as needed.
     * @return A normalized image.
     */
    public static BufferedImage normalizeRotation(Logger logger, WebDriver driver,
                                           BufferedImage image, ImageRotation rotation) {
        ArgumentGuard.notNull(driver, "driver");
        ArgumentGuard.notNull(image, "image");
        int degrees;
        if (rotation != null) {
            degrees = rotation.getRotation();
        } else {
            degrees = EyesAppiumUtils.tryAutomaticRotation(logger, driver, image);
        }

        return ImageUtils.rotateImage(image, degrees);
    }

    public <X> X getScreenshotAs(OutputType<X> xOutputType)
            throws WebDriverException {
        // Get the image as base64.
        String screenshot64 = driver.getScreenshotAs(OutputType.BASE64);
        BufferedImage screenshot = ImageUtils.imageFromBase64(screenshot64);
        screenshot = EyesAppiumDriver.normalizeRotation(logger, driver, screenshot, rotation);

        // Return the image in the requested format.
        screenshot64 = ImageUtils.base64FromImage(screenshot);
        return xOutputType.convertFromBase64Png(screenshot64);
    }
}
