package com.applitools.eyes.appium;

import com.applitools.eyes.Location;
import com.applitools.eyes.Logger;
import com.applitools.eyes.positioning.PositionMemento;
import com.applitools.eyes.selenium.positioning.ScrollPositionMemento;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import java.time.Duration;

import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

public class AndroidScrollPositionProvider extends AppiumScrollPositionProvider {

    private Location curScrollPos;
    private Location scrollableViewLoc;

    public AndroidScrollPositionProvider(Logger logger, EyesAppiumDriver driver) {
        super(logger, driver);
    }

    @Override
    public Location getScrollableViewLocation() {
        logger.verbose("Getting the location of the scrollable view..");
        if (scrollableViewLoc == null) {
            WebElement activeScroll;
            try {
                activeScroll = EyesAppiumUtils.getFirstScrollableView(driver);
            } catch (NoSuchElementException e) {
                logger.verbose("WARNING: could not find a scrollable view, using (0,0)");
                return new Location(0, 0);
            }
            Point scrollLoc = activeScroll.getLocation();
            scrollableViewLoc = new Location(scrollLoc.x, scrollLoc.y);
        }
        logger.verbose("The location of the scrollable view is " + scrollableViewLoc);
        return scrollableViewLoc;
    }

    @Override
    public Location getCurrentPosition(boolean absolute) {
        logger.verbose("AndroidScrollPositionProvider - getCurrentPosition()");
        Location loc = getScrollableViewLocation();
        if (curScrollPos == null) {
            logger.verbose("There was no current scroll position registered, so setting it for the first time");
            ContentSize contentSize = getCachedContentSize();
            LastScrollData scrollData = EyesAppiumUtils.getLastScrollData(driver);
            logger.verbose("Last scroll data from the server was: " + scrollData);
            curScrollPos = getScrollPosFromScrollData(contentSize, scrollData, 0, false);
        }
        Location pos;
        if (absolute) {
            pos = new Location(loc.getX() + curScrollPos.getX(), loc.getY() + curScrollPos.getY());
        } else {
            pos = new Location(curScrollPos.getX(), curScrollPos.getY());
        }
        logger.verbose("The current scroll position is " + pos);
        return pos;
    }

    public void setPosition(Location location) {
        if (location.getY() != 0 && location.getX() != 0) {
            logger.log("Warning: tried to set position on an Android scroll view, which is not possible");
            // FIXME: 12/12/2018 remove this
//            return getCurrentPosition();
        }

        if (location.getY() == curScrollPos.getY() && location.getX() == curScrollPos.getX()) {
            logger.log("Already at the desired position, doing nothing");
            // FIXME: 12/12/2018 remove this
//            return curScrollPos;
        } else {
            logger.verbose(
                "Setting position to 0, 0 by scrolling all the way back to the top");
            Location lastScrollPos = curScrollPos;
            while (curScrollPos.getY() > 0) {
                scroll(false);
                if (lastScrollPos.getY() == curScrollPos.getY()) {
                    // if we wound up in the same place after a scroll, abort
                    break;
                }
                lastScrollPos = curScrollPos;
            }
            // FIXME: 12/12/2018 remove this
//            return new Location(0,0 );
        }
    }

    public void setPosition(WebElement element) {
        logger.log("Warning: can only scroll back to elements that have already been seen");
        try {
            WebElement activeScroll = EyesAppiumUtils.getFirstScrollableView(driver);
            EyesAppiumUtils.scrollBackToElement((AndroidDriver) driver, (RemoteWebElement) activeScroll,
                (RemoteWebElement) element);

            LastScrollData lastScrollData = EyesAppiumUtils.getLastScrollData(driver);
            logger.verbose("After scrolling back to first child, lastScrollData was: " + lastScrollData);
            curScrollPos = new Location(lastScrollData.scrollX, lastScrollData.scrollY);
        } catch (NoSuchElementException e) {
            logger.verbose("Could not set position because there was no scrollable view; doing nothing");
        }
    }

    public void restoreState(PositionMemento state) {
        setPosition(new Location(((ScrollPositionMemento) state).getX(), ((ScrollPositionMemento) state).getY()));
    }

    private void scroll(boolean isDown) {
        ContentSize contentSize = getCachedContentSize();
        int extraPadding = (int) (contentSize.height * 0.1); // scroll 10% less than the max
        int startX = contentSize.left + (contentSize.width / 2);
        int startY = contentSize.top + contentSize.height - contentSize.touchPadding - extraPadding;
        int endX = startX;
        int endY = contentSize.top + contentSize.touchPadding + extraPadding;

        // if we're scrolling up, just switch the Y vars
        if (!isDown) {
            int temp = endY;
            endY = startY;
            startY = temp;
        }

        int supposedScrollAmt = startY - endY; // how much we will scroll if we don't hit a barrier

        TouchAction scrollAction = new TouchAction(driver);
        scrollAction.press(new PointOption().withCoordinates(startX, startY)).waitAction(new WaitOptions().withDuration(Duration.ofMillis(1500)));
        scrollAction.moveTo(new PointOption().withCoordinates(endX, endY));
        scrollAction.release();
        driver.performTouchAction(scrollAction);

        // because Android scrollbars are visible a bit after touch, we should wait for them to
        // disappear before handing control back to the screenshotter
        try { Thread.sleep(750); } catch (InterruptedException ign) {}

        LastScrollData lastScrollData = EyesAppiumUtils.getLastScrollData(driver);
        logger.verbose("After scroll lastScrollData was: " + lastScrollData);
        curScrollPos = getScrollPosFromScrollData(contentSize, lastScrollData, supposedScrollAmt, isDown);
    }

    public Location scrollDown(boolean returnAbsoluteLocation) {
        scroll(true);
        return getCurrentPosition(returnAbsoluteLocation);
    }

    private Location getScrollPosFromScrollData(ContentSize contentSize, LastScrollData scrollData, int supposedScrollAmt, boolean isDown) {
        logger.verbose("Getting scroll position from last scroll data (" + scrollData + ") and " +
            "contentSize (" + contentSize + ")");

        // if we didn't get last scroll data, it should be because we were already at the end of
        // the scroll view. This means, unfortunately, we don't have any data about how much
        // we had to scroll to reach the end. So let's make it up based on the contentSize
        if (scrollData == null) {
            logger.verbose("Did not get last scroll data; assume there was no more scroll");
            if (isDown) {
                logger.verbose(
                    "Since we're scrolling down, setting Y location to the last page of the scrollableOffset");
                return new Location(curScrollPos.getX(),
                    contentSize.scrollableOffset);
            }

            logger.verbose("Since we're scrolling up, just say we reached 0, 0");
            return new Location(curScrollPos == null ? 0 : curScrollPos.getX(), 0);
        }

        // if we got scrolldata from a ScrollView (not List or Grid), actively set the scroll
        // position with correct x/y values
        if (scrollData.scrollX != -1 && scrollData.scrollY != -1) {
            logger.verbose("Setting scroll position based on pixel values from scroll data");
            return new Location(scrollData.scrollX, scrollData.scrollY);
        }

        // otherwise, if we already have a scroll position, just assume we scrolled exactly as much
        // as the touchaction was supposed to. unfortunately it's not really that simple, because we
        // might think we scrolled a full page but we hit a barrier and only scrolled a bit. so take
        // a peek at the fromIndex of the scrolldata; if the position based on the fromIndex is
        // wildly different than what we thought we scrolled, go with the fromIndex-based position

        // we really need the number of items per row to do this math correctly.
        // since we don't have that, just use the average item height, which means we might get
        // part-rows for gridviews that have multiple items per row
        double avgItemHeight = contentSize.getScrollContentHeight() / scrollData.itemCount;
        int curYPos = curScrollPos == null ? 0 : curScrollPos.getY();
        int yPosByIndex = (int) avgItemHeight * scrollData.fromIndex;
        int yPosByAssumption = curYPos + supposedScrollAmt;
        int newYPos;
        logger.verbose("By assumption we are now at " + yPosByAssumption + " pixels, and by item " +
            "index we are now at " + yPosByIndex + " pixels");
        if (((double) Math.abs(yPosByAssumption - yPosByIndex) / contentSize.height) > 0.1) {
            // if the difference is more than 10% of the view height, go with index-based
            newYPos = yPosByIndex;
            logger.verbose("Estimating that current scroll Y position is " + newYPos + ", based on item count of " +
                scrollData.itemCount + ", avg item height of " + avgItemHeight + ", and scrolled-to " +
                "fromIndex of " + scrollData.fromIndex);
        } else {
            newYPos = yPosByAssumption;
            logger.verbose("Assuming we scrolled down exactly " + supposedScrollAmt + " pixels");
        }

        return new Location(curScrollPos == null ? 0 : curScrollPos.getX(), newYPos);
    }

}
