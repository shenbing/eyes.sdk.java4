/*
 * Applitools SDK for Appium integration.
 */
package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.capture.ImageProviderFactory;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.positioning.PositionProvider;
import com.applitools.eyes.positioning.ScrollingPositionProvider;
import com.applitools.eyes.scaling.FixedScaleProviderFactory;
import com.applitools.eyes.selenium.EyesSeleniumUtils;
import com.applitools.eyes.selenium.SeleniumJavaScriptExecutor;
import com.applitools.eyes.selenium.capture.EyesWebDriverScreenshot;
import com.applitools.eyes.selenium.capture.EyesWebDriverScreenshotFactory;
import com.applitools.eyes.selenium.capture.FullPageCaptureAlgorithm;
import com.applitools.eyes.selenium.positioning.RegionPositionCompensationFactory;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import java.awt.image.BufferedImage;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;


public class Eyes extends com.applitools.eyes.selenium.Eyes {

    private static final String NATIVE_APP = "NATIVE_APP";
    private EyesAppiumDriver driver;
    protected AppiumScrollPositionProvider positionProvider; // hiding EyesBase.positionProvider, because Appium _only_ has a scroll position provider

    public Eyes() {
        init();
        doNotGetTitle = true;
    }

    private void init() {
        // FIXME: 19/06/2018 Not relevant anymore (both the JS handler and treating EyesSeleniumUtils as static)
//        EyesSeleniumUtils.setJavascriptHandler(new AppiumJavascriptHandler(this.driver));
    }

    @Override
    public EyesAppiumDriver getEyesDriver() {
        return driver;
    }

    @Override
    protected void initPositionProvider(boolean hardReset) {
        logger.verbose("Initializing position providers.");
        setPositionProvider(new AppiumScrollPositionProviderFactory(logger, getEyesDriver()).getScrollPositionProvider());
    }

    protected void initImageProvider() {
        imageProvider = ImageProviderFactory.getImageProvider(this, logger, getEyesDriver(), true);
    }


    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    protected void initDriver(WebDriver driver) {
        if (driver instanceof AppiumDriver) {
            logger.verbose("Found an instance of AppiumDriver, so using EyesAppiumDriver instead");
            this.driver = new EyesAppiumDriver(logger, this, (AppiumDriver) driver);
        } else {
            logger.verbose("Did not find an instance of AppiumDriver, using regular logic");
            /* TODO
               when a breaking change of this library can be published, we can do away with
               this else clause */
            super.initDriver(driver);
        }
    }

    @Override
    public AppiumScrollPositionProvider getPositionProvider() {
        return positionProvider;
    }

    @Override
    public void setPositionProvider(PositionProvider positionProvider) {
        logger.verbose("Setting Appium position provider");
        this.positionProvider = (AppiumScrollPositionProvider) positionProvider;
    }

    @Override
    protected ScaleProviderFactory getScaleProviderFactory() {
        // in the context of appium, we know the pixel ratio by getting it directly from the appium
        // server, so there's no need to figure anything out on the fly. just return a Fixed one
        return new FixedScaleProviderFactory(1 / devicePixelRatio, scaleProviderHandler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This override also checks for mobile operating system.
     */
    @Override
    protected AppEnvironment getAppEnvironment() {

        AppEnvironment appEnv = super.getAppEnvironment();
        RemoteWebDriver underlyingDriver = driver.getRemoteWebDriver();
        // If hostOs isn't set, we'll try and extract and OS ourselves.
        if (appEnv.getOs() == null) {
            logger.log("No OS set, checking for mobile OS...");
            if (EyesAppiumUtils.isMobileDevice(underlyingDriver)) {
                String platformName = null;
                logger.log("Mobile device detected! Checking device type..");
                if (EyesAppiumUtils.isAndroid(underlyingDriver)) {
                    logger.log("Android detected.");
                    platformName = "Android";
                } else if (EyesAppiumUtils.isIOS(underlyingDriver)) {
                    logger.log("iOS detected.");
                    platformName = "iOS";
                } else {
                    logger.log("Unknown device type.");
                }
                // We only set the OS if we identified the device type.
                if (platformName != null) {
                    String os = platformName;
                    String platformVersion = EyesAppiumUtils.getPlatformVersion(underlyingDriver);
                    if (platformVersion != null) {
                        String majorVersion =
                            platformVersion.split("\\.", 2)[0];

                        if (!majorVersion.isEmpty()) {
                            os += " " + majorVersion;
                        }
                    }

                    logger.verbose("Setting OS: " + os);
                    appEnv.setOs(os);
                }
            } else {
                logger.log("No mobile OS detected.");
            }
        }
        logger.log("Done!");
        return appEnv;
    }

    @Override
    protected double extractDevicePixelRatio() {
        return getEyesDriver().getDevicePixelRatio();
    }

    @Override
    protected ScrollingPositionProvider getScrollPositionProvider() {
        // ensure we reuse the scroll position provider because it can have expensive state
        if (positionProvider == null) {
            AppiumScrollPositionProviderFactory scrollFactory = new AppiumScrollPositionProviderFactory(logger, getEyesDriver());
            setPositionProvider(scrollFactory.getScrollPositionProvider());
        }
        return positionProvider;
    }

    @Override
    protected EyesWebDriverScreenshot getFullPageScreenshot() {

        logger.verbose("Full page Appium screenshot requested.");

        EyesScreenshotFactory screenshotFactory = new EyesWebDriverScreenshotFactory(logger, getEyesDriver());
        ScaleProviderFactory scaleProviderFactory = updateScalingParams();

        AppiumScrollPositionProvider scrollPositionProvider = (AppiumScrollPositionProvider) getScrollPositionProvider();

        AppiumCaptureAlgorithmFactory algoFactory = new AppiumCaptureAlgorithmFactory(getEyesDriver(), logger,
            scrollPositionProvider, imageProvider, debugScreenshotsProvider, scaleProviderFactory,
            cutProviderHandler.get(), screenshotFactory, getWaitBeforeScreenshots());

        FullPageCaptureAlgorithm algo = algoFactory.getAlgorithm();

        Location originalScrollViewPosition = scrollPositionProvider.getScrollableViewLocation();
        BufferedImage fullPageImage = algo
            .getStitchedRegion(Region.EMPTY, getStitchOverlap(), regionPositionCompensation);

//        // FIXME: 26/04/2018 Not sure this is the correct way to get the scrollable region (make sure position is not related to content offset)
//        Region scrollableViewRegion = scrollPositionProvider.getScrollableViewRegion();
//        BufferedImage fullPageImage = algo
//            .getStitchedRegion(scrollableViewRegion, getStitchOverlap(), regionPositionCompensation);

        return new EyesWebDriverScreenshot(logger, driver, fullPageImage, null,
            originalScrollViewPosition, scrollPositionProvider);
    }

    protected EyesWebDriverScreenshot getSimpleScreenshot() {
        ScaleProviderFactory scaleProviderFactory = updateScalingParams();
//        ensureElementVisible(this.targetElement);

        logger.verbose("Screenshot requested...");
        BufferedImage screenshotImage = imageProvider.getImage();
        debugScreenshotsProvider.save(screenshotImage, "original");

        ScaleProvider scaleProvider = scaleProviderFactory.getScaleProvider(screenshotImage.getWidth());
        if (scaleProvider.getScaleRatio() != 1.0) {
            logger.verbose("scaling...");
            screenshotImage = ImageUtils.scaleImage(screenshotImage, scaleProvider);
            debugScreenshotsProvider.save(screenshotImage, "scaled");
        }

        CutProvider cutProvider = cutProviderHandler.get();
        if (!(cutProvider instanceof NullCutProvider)) {
            logger.verbose("cutting...");
            screenshotImage = cutProvider.cut(screenshotImage);
            debugScreenshotsProvider.save(screenshotImage, "cut");
        }

        logger.verbose("Creating screenshot object...");
        return new EyesWebDriverScreenshot(logger, getEyesDriver(), screenshotImage);
    }

    // TODO override implementation of getFrameOrElementScreenshot
}