package com.applitools.eyes.selenium.positioning;

import com.applitools.eyes.positioning.ScrollingPositionProvider;
import com.applitools.eyes.selenium.frames.Frame;
import org.openqa.selenium.WebElement;

public interface SeleniumScrollingPositionProvider extends ScrollingPositionProvider {
    void setPosition(Frame frame);
    void setPosition(WebElement element);
}
