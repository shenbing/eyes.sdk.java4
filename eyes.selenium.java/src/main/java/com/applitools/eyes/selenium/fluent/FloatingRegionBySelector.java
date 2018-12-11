package com.applitools.eyes.selenium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.fluent.GetFloatingRegion;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.EyesSeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FloatingRegionBySelector implements GetFloatingRegion {

    private By selector;
    private int maxUpOffset;
    private int maxDownOffset;
    private int maxLeftOffset;
    private int maxRightOffset;

    public FloatingRegionBySelector(By regionSelector, int maxUpOffset, int maxDownOffset, int maxLeftOffset, int maxRightOffset) {

        this.selector = regionSelector;
        this.maxUpOffset = maxUpOffset;
        this.maxDownOffset = maxDownOffset;
        this.maxLeftOffset = maxLeftOffset;
        this.maxRightOffset = maxRightOffset;
    }

    @Override
    public List<FloatingMatchSettings> getRegions(EyesBase eyesBase, EyesScreenshot screenshot) {
        List<WebElement> elements = ((Eyes) eyesBase).getDriver().findElements(this.selector);
        List<FloatingMatchSettings> values = new ArrayList<>();

        for (WebElement element : elements) {
            Point locationAsPoint = element.getLocation();
            RectangleSize size = EyesSeleniumUtils.getElementVisibleSize(element);

            // Element's coordinates are context relative, so we need to convert them first.
            Location adjustedLocation = screenshot.getLocationInScreenshot(new Location(locationAsPoint.getX(), locationAsPoint.getY()),
                    CoordinatesType.CONTEXT_RELATIVE);

            values.add(new FloatingMatchSettings(adjustedLocation.getX(), adjustedLocation.getY(), size.getWidth(),
                    size.getHeight(), maxUpOffset, maxDownOffset, maxLeftOffset, maxRightOffset));
        }

        return values;
    }
}
