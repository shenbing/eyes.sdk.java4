package com.applitools.eyes.fluent;

import com.applitools.eyes.EyesBase;
import com.applitools.eyes.EyesScreenshot;
import com.applitools.eyes.FloatingMatchSettings;

import java.util.List;

public interface GetFloatingRegion {
    List<FloatingMatchSettings> getRegions(EyesBase eyesBase, EyesScreenshot screenshot);
}
