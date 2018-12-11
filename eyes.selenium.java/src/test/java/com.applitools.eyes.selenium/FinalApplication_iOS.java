package com.applitools.eyes.selenium;

import com.applitools.eyes.StdoutLogHandler;
import java.awt.AWTException;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author mohamedabdulkadar.m
 */
public class FinalApplication_iOS {
    public static final String USERNAME = System.getenv("SAUCE_USERNAME");
    public static final String ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
    static WebDriver driver;
    public static final String URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.saucelabs.com:80/wd/hub";
    static WebElement wb;
    static long start = System.currentTimeMillis();
    static long stop;

    static public void main(String[] args) throws MalformedURLException, InterruptedException, AWTException {
        DesiredCapabilities caps = DesiredCapabilities.iphone();
        caps.setCapability("appiumVersion", "1.6.5");
        caps.setCapability("deviceName", "iPhone Simulator");
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("platformVersion", "10.0");
        caps.setCapability("platformName", "iOS");
        caps.setCapability("browserName", "Safari");
        driver = new RemoteWebDriver(new URL(URL), caps);
        System.out.println("caps finished");
        function();
    }

    public static void function() throws InterruptedException, AWTException {
        Eyes eyes = new Eyes();
        eyes.setSaveNewTests(false);
        eyes.setForceFullPageScreenshot(true);
        eyes.setStitchMode(StitchMode.CSS);

        eyes.setSaveDebugScreenshots(true);
        eyes.setDebugScreenshotsPath("c:\\temp\\logs");
        eyes.setDebugScreenshotsPrefix("IOS_10_0_Safari_10_0_");

        //eyes.setImageCut(new FixedCutProvider(URL_BAR_SIZE,NAVIGATION_BAR_SIZE, 0, 0));
        eyes.setLogHandler(new StdoutLogHandler(true));
        driver= eyes.open(driver, "sample", "IOS");
        try {
            driver.get("http://atom:mota@lgi-www-sat.trimm.net/test/ziggo/title-with-icon.html");
//            Thread.sleep(5000);
            eyes.checkWindow();
            WebElement element = driver.findElement(By.xpath("html/body/div[2]"));
            System.out.println("this is the element location: " + element.getLocation().toString());
            System.out.println("this is the element Height: " + element.getSize().getHeight());
            System.out.println("this is the element Width: " + element.getSize().getWidth());
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500);
            eyes.checkRegion(driver.findElement(By.xpath("html/body/div[2]")), true);

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
            stop = System.currentTimeMillis();
            System.out.println("TIME TAKEN IN SECONDS =" + ((stop - start) / 1000));
        }

    }
}