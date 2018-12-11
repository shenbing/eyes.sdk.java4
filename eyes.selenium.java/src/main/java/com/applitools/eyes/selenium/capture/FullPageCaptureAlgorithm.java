package com.applitools.eyes.selenium.capture;

import com.applitools.eyes.*;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.CutProvider;
import com.applitools.eyes.positioning.PositionMemento;
import com.applitools.eyes.positioning.PositionProvider;
import com.applitools.eyes.selenium.exceptions.EyesDriverOperationException;
import com.applitools.eyes.selenium.positioning.NullRegionPositionCompensation;
import com.applitools.eyes.selenium.positioning.RegionPositionCompensation;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class FullPageCaptureAlgorithm {
    private static final int MIN_SCREENSHOT_PART_HEIGHT = 10;

    private final Logger logger;
    private final RegionPositionCompensation regionPositionCompensation;
    private final int waitBeforeScreenshots;
    private final DebugScreenshotsProvider debugScreenshotsProvider;
    private final EyesScreenshotFactory screenshotFactory;
    private final PositionProvider originProvider;
    private final ScaleProviderFactory scaleProviderFactory;
    private final CutProvider cutProvider;
    private final int stitchingOverlap;
    private final ImageProvider imageProvider;

    public FullPageCaptureAlgorithm(Logger logger, RegionPositionCompensation regionPositionCompensation,
                                    int waitBeforeScreenshots, DebugScreenshotsProvider debugScreenshotsProvider,
                                    EyesScreenshotFactory screenshotFactory, PositionProvider originProvider,
                                    ScaleProviderFactory scaleProviderFactory, CutProvider cutProvider,
                                    int stitchingOverlap, ImageProvider imageProvider) {

        ArgumentGuard.notNull(logger, "logger");

        this.logger = logger;
        this.waitBeforeScreenshots = waitBeforeScreenshots;
        this.debugScreenshotsProvider = debugScreenshotsProvider;
        this.screenshotFactory = screenshotFactory;
        this.originProvider = originProvider;
        this.scaleProviderFactory = scaleProviderFactory;
        this.cutProvider = cutProvider;
        this.stitchingOverlap = stitchingOverlap;
        this.imageProvider = imageProvider;

        this.regionPositionCompensation =
                regionPositionCompensation != null
                        ? regionPositionCompensation
                        : new NullRegionPositionCompensation();
    }

    private void saveDebugScreenshotPart(BufferedImage image, Region region, String name) {

        String suffix = String.format("part-%s-%d_%d_%dx%d",
                name, region.getLeft(), region.getTop(), region.getWidth(), region.getHeight());

        debugScreenshotsProvider.save(image, suffix);
    }

    /**
     * Returns a stitching of a region.
     * @param region           The region to stitch. If {@code Region.EMPTY}, the entire image will be stitched.
     * @param fullArea         The wanted area of the resulting image. If unknown, pass in {@code null} or {@code RectangleSize.EMPTY}.
     * @param positionProvider A provider of the scrolling implementation.
     * @return An image which represents the stitched region.
     */
    public BufferedImage getStitchedRegion(Region region, Region fullArea, PositionProvider positionProvider) {
        logger.verbose("getStitchedRegion()");

        ArgumentGuard.notNull(region, "region");
        ArgumentGuard.notNull(positionProvider, "positionProvider");

        logger.verbose(String.format("positionProvider: %s ; Region: %s", positionProvider.getClass(), region));

        // Saving the original position (in case we were already in the outermost frame).
        PositionMemento originalStitchedState = positionProvider.getState();

        PositionMemento originalPosition = originProvider.getState();
        originProvider.setPosition(Location.ZERO); // first scroll to 0,0 so CSS stitching works.

        logger.verbose("Getting top/left image...");
        BufferedImage image = imageProvider.getImage();
        debugScreenshotsProvider.save(image, "original");

        // FIXME - scaling should be refactored
        ScaleProvider scaleProvider = scaleProviderFactory.getScaleProvider(image.getWidth());
        // Notice that we want to cut/crop an image before we scale it, we need to change
        double pixelRatio = 1 / scaleProvider.getScaleRatio();

        // FIXME - cropping should be overlaid, so a single cut provider will only handle a single part of the image.
        CutProvider scaledCutProvider = cutProvider.scale(pixelRatio);
        if (!(scaledCutProvider instanceof NullCutProvider)) {
            image = scaledCutProvider.cut(image);
            debugScreenshotsProvider.save(image, "original-cut");
        }

        Region regionInScreenshot = getRegionInScreenshot(region, image, pixelRatio);

        if (!regionInScreenshot.isSizeEmpty()) {
            image = ImageUtils.getImagePart(image, regionInScreenshot);
            saveDebugScreenshotPart(image, region, "cropped");
        }

        if (pixelRatio != 1.0) {
            image = ImageUtils.scaleImage(image, 1.0 / pixelRatio);
            debugScreenshotsProvider.save(image, "scaled");
        }

        if (fullArea == null || fullArea.isEmpty()) {
            RectangleSize entireSize;
            try {
                entireSize = positionProvider.getEntireSize();
                logger.verbose("Entire size of region context: " + entireSize);
            } catch (EyesDriverOperationException e) {
                logger.log("WARNING: Failed to extract entire size of region context" + e.getMessage());
                logger.log("Using image size instead: " + image.getWidth() + "x" + image.getHeight());
                entireSize = new RectangleSize(image.getWidth(), image.getHeight());
            }

            // Notice that this might still happen even if we used
            // "getImagePart", since "entirePageSize" might be that of a frame.
            if (image.getWidth() >= entireSize.getWidth() && image.getHeight() >= entireSize.getHeight()) {
                originProvider.restoreState(originalPosition);

                return image;
            }

            fullArea = new Region(Location.ZERO, entireSize);
        }

        // These will be used for storing the actual stitched size (it is
        // sometimes less than the size extracted via "getEntireSize").
        Location lastSuccessfulLocation;
        RectangleSize lastSuccessfulPartSize;

        // The screenshot part is a bit smaller than the screenshot size,
        // in order to eliminate duplicate bottom scroll bars, as well as fixed
        // position footers.
        RectangleSize partImageSize =
                new RectangleSize(image.getWidth(),
                        Math.max(image.getHeight() - stitchingOverlap, MIN_SCREENSHOT_PART_HEIGHT));

        logger.verbose(String.format("entire page region: %s, image part size: %s", fullArea, partImageSize));

        // Getting the list of sub-regions composing the whole region (we'll
        // take screenshot for each one).
        Iterable<Region> imageParts = fullArea.getSubRegions(partImageSize);

        logger.verbose("Creating stitchedImage container.");
        //Notice stitchedImage uses the same type of image as the screenshots.
        BufferedImage stitchedImage = new BufferedImage(
                fullArea.getWidth(), fullArea.getHeight(), image.getType());

        logger.verbose("Done! Adding initial screenshot..");
        // Starting with the screenshot we already captured at (0,0).
        Raster initialPart = image.getData();
        logger.verbose(String.format("Initial part:(0,0)[%d x %d]",
                initialPart.getWidth(), initialPart.getHeight()));
        stitchedImage.getRaster().setRect(0, 0, initialPart);
        logger.verbose("Done!");

        lastSuccessfulLocation = new Location(0, 0);
        lastSuccessfulPartSize = new RectangleSize(initialPart.getWidth(), initialPart.getHeight());

        // Take screenshot and stitch for each screenshot part.
        logger.verbose("Getting the rest of the image parts...");
        BufferedImage partImage = null;
        for (Region partRegion : imageParts) {
            // Skipping screenshot for 0,0 (already taken)
//            if (partRegion.getLeft() == 0 && partRegion.getTop() == 0) {
//                continue;
//            }
            logger.verbose(String.format("Taking screenshot for %s", partRegion));
            // Set the position to the part's top/left.
            positionProvider.setPosition(partRegion.getLocation());
            // Giving it time to stabilize.
            GeneralUtils.sleep(waitBeforeScreenshots);
            // Screen size may cause the scroll to only reach part of the way.
            Location originPosition = positionProvider.getCurrentPosition();
            Location targetPosition = originPosition.offset(-fullArea.getLeft(), -fullArea.getTop());
            logger.verbose(String.format("Origin Position is set to %s", originPosition));

            // Actually taking the screenshot.
            logger.verbose("Getting image...");
            partImage = imageProvider.getImage();
            debugScreenshotsProvider.save(partImage,
                    "original-scrolled-" + positionProvider.getCurrentPosition().toStringForFilename());

            // FIXME - cropping should be overlaid (see previous comment re cropping)
            if (!(scaledCutProvider instanceof NullCutProvider)) {
                logger.verbose("cutting...");
                partImage = scaledCutProvider.cut(partImage);
                debugScreenshotsProvider.save(partImage,
                        "original-scrolled-cut-" + positionProvider.getCurrentPosition().toStringForFilename());
            }

            if (!regionInScreenshot.isSizeEmpty()) {
                logger.verbose("cropping...");
                partImage = ImageUtils.getImagePart(partImage, regionInScreenshot);
                saveDebugScreenshotPart(partImage, partRegion, "original-scrolled-"
                        + positionProvider.getCurrentPosition().toStringForFilename());
            }

            if (pixelRatio != 1.0) {
                logger.verbose("scaling...");
                // FIXME - scaling should be refactored
                partImage = ImageUtils.scaleImage(partImage, 1.0 / pixelRatio);
                saveDebugScreenshotPart(partImage, partRegion,
                        "original-scrolled-" + positionProvider.getCurrentPosition().toStringForFilename() + "-scaled-");
            }

            // Stitching the current part.
            logger.verbose("Stitching part into the image container...");
            stitchedImage.getRaster().setRect(targetPosition.getX(), targetPosition.getY(), partImage.getData());
            logger.verbose("Done!");

            lastSuccessfulLocation = originPosition;
        }

        if (partImage != null) {
            lastSuccessfulPartSize = new RectangleSize(partImage.getWidth(), partImage.getHeight());
        }

        logger.verbose("Stitching done!");
        positionProvider.restoreState(originalStitchedState);
        originProvider.restoreState(originalPosition);

        // If the actual image size is smaller than the extracted size, we crop the image.
        int actualImageWidth = lastSuccessfulLocation.getX() + lastSuccessfulPartSize.getWidth();
        int actualImageHeight = lastSuccessfulLocation.getY() + lastSuccessfulPartSize.getHeight();
        logger.verbose("Extracted entire size: " + fullArea.getSize());
        logger.verbose("Actual stitched size: " + actualImageWidth + "x" + actualImageHeight);

        if (actualImageWidth < stitchedImage.getWidth() || actualImageHeight < stitchedImage.getHeight()) {
            logger.verbose("Trimming unnecessary margins..");
            stitchedImage = ImageUtils.getImagePart(stitchedImage,
                    new Region(0, 0,
                            Math.min(actualImageWidth, stitchedImage.getWidth()),
                            Math.min(actualImageHeight, stitchedImage.getHeight())));
            logger.verbose("Done!");
        }

        debugScreenshotsProvider.save(stitchedImage, "stitched");
        return stitchedImage;
    }

    private Region getRegionInScreenshot(Region region, BufferedImage image, double pixelRatio) {
        logger.verbose("Creating screenshot object...");
        // We need the screenshot to be able to convert the region to screenshot coordinates.
        EyesScreenshot screenshot = screenshotFactory.makeScreenshot(image);
        logger.verbose("Getting region in screenshot...");

        // Region regionInScreenshot = screenshot.convertRegionLocation(regionProvider.getRegion(), regionProvider.getCoordinatesType(), CoordinatesType.SCREENSHOT_AS_IS);
        Region regionInScreenshot = screenshot.getIntersectedRegion(region, CoordinatesType.SCREENSHOT_AS_IS);

        logger.verbose("Region in screenshot: " + regionInScreenshot);
        regionInScreenshot = regionInScreenshot.scale(pixelRatio);
        logger.verbose("Scaled region: " + regionInScreenshot);

        regionInScreenshot = regionPositionCompensation.compensateRegionPosition(regionInScreenshot, pixelRatio);

        // Handling a specific case where the region is actually larger than
        // the screenshot (e.g., when body width/height are set to 100%, and
        // an internal div is set to value which is larger than the viewport).
        regionInScreenshot.intersect(new Region(0, 0, image.getWidth(), image.getHeight()));
        logger.verbose("Region after intersect: " + regionInScreenshot);
        return regionInScreenshot;
    }
}
