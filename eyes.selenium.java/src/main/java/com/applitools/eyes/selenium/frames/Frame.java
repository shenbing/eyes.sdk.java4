/*
 * Applitools software.
 */
package com.applitools.eyes.selenium.frames;

import com.applitools.eyes.Location;
import com.applitools.eyes.Logger;
import com.applitools.eyes.RectangleSize;
import com.applitools.utils.ArgumentGuard;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Encapsulates a frame/iframe. This is a generic type class,
 * and it's actual type is determined by the reference used by the user in
 * order to switch into the frame.
 */
public final class Frame {
    // A user can switch into a frame by either its name,
    // index or by passing the relevant web element.
    private final WebElement reference;
    private final Location location;
    private final RectangleSize outerSize;
    private final RectangleSize innerSize;
    private final Location originalLocation;
    private final JavascriptExecutor jsExecutor;
    private final Logger logger;

    private WebElement scrollRootElement;
    private String originalOverflow;

    /**
     * @param logger           A Logger instance.
     * @param reference        The web element for the frame, used as a reference to switch into the frame.
     * @param location         The location of the frame within the current frame.
     * @param outerSize        The frame element outerSize (i.e., the outerSize of the frame on the screen, not the internal document outerSize).
     * @param innerSize        The frame element inner outerSize (i.e., the outerSize of the frame actual outerSize, without borders).
     * @param originalLocation The scroll location of the frame.
     * @param jsExecutor       The Javascript Executor to use. Usually that will be the WebDriver.
     */
    public Frame(Logger logger, WebElement reference,
                 Location location, RectangleSize outerSize, RectangleSize innerSize,
                 Location originalLocation, JavascriptExecutor jsExecutor) {
        ArgumentGuard.notNull(logger, "logger");
        ArgumentGuard.notNull(reference, "reference");
        ArgumentGuard.notNull(location, "location");
        ArgumentGuard.notNull(outerSize, "outerSize");
        ArgumentGuard.notNull(innerSize, "innerSize");
        ArgumentGuard.notNull(originalLocation, "originalLocation");
        ArgumentGuard.notNull(jsExecutor, "jsExecutor");

        logger.verbose(String.format(
                "Frame(logger, reference, %s, %s, %s, %s)",
                location, outerSize, innerSize, originalLocation));

        this.logger = logger;
        this.reference = reference;
        this.location = location;
        this.outerSize = outerSize;
        this.innerSize = innerSize;
        this.originalLocation = originalLocation;
        this.jsExecutor = jsExecutor;
    }

    public WebElement getReference() {
        return reference;
    }

    public Location getLocation() {
        return location;
    }

    public RectangleSize getOuterSize() {
        return outerSize;
    }

    public RectangleSize getInnerSize() {
        return innerSize;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public WebElement getScrollRootElement() {
        return scrollRootElement;
    }

    public void hideScrollbars(WebDriver driver) {
        if (scrollRootElement == null) {
            logger.verbose("no scroll root element. selecting default.");
            scrollRootElement = driver.findElement(By.tagName("html"));
        }
        logger.verbose("hiding scrollbars of element: " + scrollRootElement);
        originalOverflow = (String) jsExecutor.executeScript("var origOF = arguments[0].style.overflow; arguments[0].style.overflow='hidden'; return origOF;", scrollRootElement);
    }

    public void returnToOriginalOverflow(WebDriver driver) {
        if (scrollRootElement == null) {
            logger.verbose("no scroll root element. selecting default.");
            scrollRootElement = driver.findElement(By.tagName("html"));
        }
        logger.verbose("returning overflow of element to its original value: " + scrollRootElement);
        jsExecutor.executeScript("arguments[0].style.overflow='" + originalOverflow + "';", scrollRootElement);
    }

}
