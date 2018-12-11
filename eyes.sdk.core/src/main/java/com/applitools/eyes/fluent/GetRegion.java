package com.applitools.eyes.fluent;

import com.applitools.eyes.EyesBase;
import com.applitools.eyes.EyesScreenshot;
import com.applitools.eyes.Region;

import java.util.List;

public interface GetRegion {
    List<Region> getRegions(EyesBase eyesBase, EyesScreenshot screenshot, boolean adjustLocation);
}
