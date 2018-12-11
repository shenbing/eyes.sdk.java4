package com.applitools.eyes.appium;

import com.applitools.eyes.CutProvider;
import com.applitools.eyes.Logger;
import com.applitools.eyes.ScaleProviderFactory;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.debug.DebugScreenshotsProvider;

public class AndroidFullPageCaptureAlgorithm extends AppiumFullPageCaptureAlgorithm {

    public AndroidFullPageCaptureAlgorithm(Logger logger,
        AppiumScrollPositionProvider scrollProvider,
        ImageProvider imageProvider, DebugScreenshotsProvider debugScreenshotsProvider,
        ScaleProviderFactory scaleProviderFactory, CutProvider cutProvider,
        EyesScreenshotFactory screenshotFactory, int waitBeforeScreenshots) {

        super(logger, scrollProvider, imageProvider, debugScreenshotsProvider,
            scaleProviderFactory, cutProvider, screenshotFactory, waitBeforeScreenshots);

        // Android returns pixel coordinates which are already scaled according to the pixel ratio
        this.coordinatesAreScaled = true;
    }

}
