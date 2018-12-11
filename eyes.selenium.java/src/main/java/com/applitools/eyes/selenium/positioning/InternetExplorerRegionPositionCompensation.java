package com.applitools.eyes.selenium.positioning;

import com.applitools.eyes.Region;

public class InternetExplorerRegionPositionCompensation implements RegionPositionCompensation {

    @Override
    public Region compensateRegionPosition(Region region, double pixelRatio) {
        return region.offset(0, (int)Math.ceil(pixelRatio));
    }
}
