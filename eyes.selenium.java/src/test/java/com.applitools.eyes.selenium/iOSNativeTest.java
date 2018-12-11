package com.applitools.eyes.selenium;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.ServerConnector;
import com.applitools.eyes.StdoutLogHandler;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class iOSNativeTest {

    public static void main(String[] args) throws Exception {

        // Set desired capabilities.
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("deviceName", "iPhone 7 Simulator");
        capabilities.setCapability("platformVersion", "10.0");
        capabilities.setCapability("app", "https://store.applitools.com/download/iOS.TestApp.app.zip");
        capabilities.setCapability("clearSystemFiles", true);
        capabilities.setCapability("noReset", true);

        // Open the app.
//        WebDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        final String SAUCE_ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
        final String SAUCE_USERNAME = System.getenv("SAUCE_USERNAME");
        final String url = "https://" + SAUCE_USERNAME + ":" + SAUCE_ACCESS_KEY + "@ondemand.saucelabs.com:443/wd/hub";
        WebDriver driver = new IOSDriver(new URL(url), capabilities);

        // Initialize the eyes SDK and set your private API key.
        Eyes eyes = new Eyes();
        eyes.setServerConnector(new ServerConnector());
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setSaveDebugScreenshots(true);
//        eyes.setForceFullPageScreenshot(true);
        try {

            // Start the test.
            eyes.open(driver, "Contacts!", "My first Appium native Java test!");

            // Visual validation.
            eyes.checkWindow("Contact list!");

            // End the test.
            eyes.close();

        } finally {

            // Close the app.
            driver.quit();

            // If the test was aborted before eyes.close was called, ends the test as aborted.
            eyes.abortIfNotClosed();
        }
    }
}
