package com.applitools.eyes.images;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Region;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class test {
    public static void main(String[] args) {
        Eyes eyes = new Eyes();

        BufferedImage img;

        try {
            // Start visual testing
            eyes.open("Applitools Website", "Sanity Test", new RectangleSize(785, 1087));

            // Load page image and validate
            String version = "";
            img = ImageIO.read(new URL("https://store.applitools.com/download/contact_us.png/" + version));
            // Visual validation point #1
            eyes.check("Contact page", Target.image(img));
            eyes.check("tag", Target.image(img).ignore(new Region(20, 20, 40, 40)));

            // Load another page and validate
            img = ImageIO.read(new URL("https://store.applitools.com/download/resources.png/" + version));
            // Visual validation point #2
            eyes.checkImage(img, "Resources page");

            // End visual testing. Validate visual correctness.
            eyes.close();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } finally {
            eyes.abortIfNotClosed();
        }
    }
}
