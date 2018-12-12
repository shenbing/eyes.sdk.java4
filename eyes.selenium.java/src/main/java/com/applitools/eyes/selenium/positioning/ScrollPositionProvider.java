package com.applitools.eyes.selenium.positioning;

import com.applitools.eyes.*;
import com.applitools.eyes.positioning.PositionMemento;
import com.applitools.eyes.positioning.PositionProvider;
import com.applitools.eyes.selenium.EyesSeleniumUtils;
import com.applitools.eyes.selenium.exceptions.EyesDriverOperationException;
import com.applitools.eyes.selenium.frames.Frame;
import com.applitools.utils.ArgumentGuard;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class ScrollPositionProvider implements SeleniumScrollingPositionProvider {


    protected final Logger logger;
    protected final IEyesJsExecutor executor;

    public ScrollPositionProvider(Logger logger, IEyesJsExecutor executor) {
        ArgumentGuard.notNull(logger, "logger");
        ArgumentGuard.notNull(executor, "executor");

        this.logger = logger;
        this.executor = executor;

        logger.verbose("creating ScrollPositionProvider");
    }

    /**
     * @return The scroll position of the current frame.
     */
    public Location getCurrentPosition() {
        logger.verbose("ScrollPositionProvider - getCurrentPosition()");
        Location result;
        try {
            result = EyesSeleniumUtils.getCurrentScrollPosition(executor);
        } catch (WebDriverException e) {
            throw new EyesDriverOperationException("Failed to extract current scroll position!", e);
        }
        logger.verbose("Current position: " + result);
        return result;
    }

    /**
     * Go to the specified location.
     * @param location The position to scroll to.
     */
    public void setPosition(Location location) {
        logger.verbose("ScrollPositionProvider - Scrolling to " + location);
        EyesSeleniumUtils.setCurrentScrollPosition(executor, location);
        logger.verbose("ScrollPositionProvider - Done scrolling!");
    }

    @Override
    public void setPosition(WebElement element) {
        Point loc = element.getLocation();
        setPosition(new Location(loc.x, loc.y));
    }

    @Override
    public void setPosition(Frame frame) {
        setPosition(frame.getReference());
    }

    @Override
    public void scrollToBottomRight() {
        logger.verbose("setting position of to bottom-right");
        // TODO: 12/12/2018 Optimize to a single JS call
        RectangleSize entireSize = getEntireSize();
        setPosition(new Location(entireSize.getWidth(),entireSize.getHeight()));
    }

    /**
     *
     * @return The entire size of the container which the position is relative
     * to.
     */
    public RectangleSize getEntireSize() {
        RectangleSize result = EyesSeleniumUtils.getCurrentFrameContentEntireSize(executor);
        logger.verbose("ScrollPositionProvider - Entire size: " + result);
        return result;
    }

    public PositionMemento getState() {
        return new ScrollPositionMemento(getCurrentPosition());
    }

    public void restoreState(PositionMemento state) {
        ScrollPositionMemento s = (ScrollPositionMemento) state;
        setPosition(new Location(s.getX(), s.getY()));
    }
}
