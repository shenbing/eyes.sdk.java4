package com.applitools.eyes.selenium;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public class TestCreditCards extends TestSetup{

    @Factory(dataProvider = "dp", dataProviderClass = TestsDataProvider.class)
    public TestCreditCards(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;
        super.forceFPS = false;
        super.testedPageSize = new RectangleSize(1260, 600);

        testSuitName = "Eyes Selenium SDK - Credit Cards";
        testedPageUrl = "https://creditcards.com/v2/zero-interest";
    }

    @Test
    public void TestPage() {

        driver.findElement(By.cssSelector("p[data-tagular-uid='117']")).click();
        By selector = By.cssSelector("body > div.boxy > main > div.boxy__product-box.product-list > div:nth-child(2)");

        eyes.check("region", Target.region(selector).fully());
    }
}
