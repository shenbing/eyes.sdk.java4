package com.applitools.eyes.images;

import com.applitools.eyes.fluent.ICheckSettings;

import java.awt.image.BufferedImage;

public interface IImagesCheckTarget extends ICheckSettings {
    BufferedImage getImage();
}
