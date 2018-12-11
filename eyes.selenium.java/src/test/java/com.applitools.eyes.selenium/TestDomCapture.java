package com.applitools.eyes.selenium;

import com.applitools.IDomCaptureListener;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public final class TestDomCapture {
    private static String domJson;
    public static void main(String[] args) throws Exception {

        // Open a Chrome browser.
        WebDriver driver = new ChromeDriver();

        // Initialize the eyes SDK and set your private API key.
        Eyes eyes = new Eyes();

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setServerUrl("https://eyes.applitools.com/");
//        eyes.setProxy(new ProxySettings("http://127.0.0.1:8888"));

        // Switch sendDom flag on
        eyes.setSendDom(true);
//        try {

        eyes.open(driver, "DOM Capture Test", "This is a Smerf's test",
                new RectangleSize(800, 600));

        // Navigate the browser to the "hello world!" web-site.
        driver.get("https://nikita-andreev.github.io/applitools/dom_capture.html?aaa");

        eyes.setOnDomCapture(new IDomCaptureListener() {
            @Override
            public void onDomCaptureComplete(String dom) {
                domJson = dom;
            }
        });

        eyes.checkWindow("Test DOM diffs");


        ObjectMapper mapper = new ObjectMapper();
        JsonNode targetJsonObj = mapper.readTree(domJson);

        String sourceJsonAsString = GeneralUtils.readToEnd(TestDomCapture.class.getResourceAsStream("/domcapture.json"));
        JsonNode sourceJsonObj = mapper.readTree(sourceJsonAsString);

        driver.quit();
        eyes.close();
        if(!sourceJsonObj.equals(targetJsonObj)){
            throw new Exception("Dom capture json was not equal to target json");
        }

    }
}