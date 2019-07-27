package com.applitools.eyes.appium;

import com.applitools.eyes.Logger;
import com.applitools.eyes.selenium.wrappers.EyesRemoteWebElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class EyesAppiumElement extends EyesRemoteWebElement {

    private Dimension size;
    private double pixelRatio;
    private EyesAppiumDriver driver;

    public EyesAppiumElement(Logger logger, EyesAppiumDriver driver, WebElement element, double pixelRatio) {
        super(logger, driver, element);
        this.pixelRatio = pixelRatio;
        this.driver = driver;
    }

    protected Dimension getCachedSize() {
        if (size == null) {
            size = webElement.getSize();
        }
        return size;
    }

    @Override
    public Dimension getSize() {
        Dimension size = super.getSize();
        if (pixelRatio == 1.0) {
            return size;
        }
        int scaledWidth;
        int scaledHeight;
        if (driver.getRemoteWebDriver() instanceof IOSDriver) {
            scaledWidth = webElement.getSize().getWidth();
            scaledHeight = webElement.getSize().getHeight();
        } else {
            scaledWidth = (int) Math.ceil(webElement.getSize().getWidth()*pixelRatio);
            scaledHeight = (int) Math.ceil(webElement.getSize().getHeight()*pixelRatio);
        }
        return new Dimension(scaledWidth, scaledHeight);
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

    @Override
    public Point getLocation() {
        Point location = super.getLocation();
        location = new Point(location.getX(), location.getY() - driver.getStatusBarHeight());
        if (pixelRatio == 1.0) {
            return location;
        }
        int scaledX;
        int scaledY;
        if (driver.getRemoteWebDriver() instanceof IOSDriver) {
            scaledX = location.getX();
            scaledY = location.getY();
        } else {
            scaledX = (int) Math.ceil(location.getX()*pixelRatio);
            scaledY = (int) Math.ceil(location.getY()*pixelRatio);
        }
        return new Point(scaledX, scaledY);
    }
}
