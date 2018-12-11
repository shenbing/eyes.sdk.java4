package com.applitools.eyes;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestUserAgentParser {

    @Test
    public void test_Android_6_Chrome_60_UserAgent() {

        String uaString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Mobile Safari/537.36";
        UserAgent ua = UserAgent.ParseUserAgentString(uaString, true);

        Assert.assertEquals(ua.getOS(), "Android");
        Assert.assertEquals(ua.getOSMajorVersion(), "6");
        Assert.assertEquals(ua.getOSMinorVersion(), "0");
        Assert.assertEquals(ua.getBrowser(), "Chrome");
        Assert.assertEquals(ua.getBrowserMajorVersion(), "60");
        Assert.assertEquals(ua.getBrowserMinorVersion(), "0");
    }

    @Test
    public void test_Windows_10_Chrome_60_UserAgent() {

        String uaString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36";
        UserAgent ua = UserAgent.ParseUserAgentString(uaString, true);

        Assert.assertEquals(ua.getOS(), "Windows");
        Assert.assertEquals(ua.getOSMajorVersion(), "10");
        Assert.assertEquals(ua.getOSMinorVersion(), "0");
        Assert.assertEquals(ua.getBrowser(), "Chrome");
        Assert.assertEquals(ua.getBrowserMajorVersion(), "60");
        Assert.assertEquals(ua.getBrowserMinorVersion(), "0");
    }

    @Test
    public void test_IPhone_10_3_Safari_602_1_UserAgent() {

        String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1";
        UserAgent ua = UserAgent.ParseUserAgentString(uaString, true);

        Assert.assertEquals(ua.getOS(), "IOS");
        Assert.assertEquals(ua.getOSMajorVersion(), "10");
        Assert.assertEquals(ua.getOSMinorVersion(), "3");
        Assert.assertEquals(ua.getBrowser(), "Safari");
        Assert.assertEquals(ua.getBrowserMajorVersion(), "602");
        Assert.assertEquals(ua.getBrowserMinorVersion(), "1");
    }

    @Test
    public void test_Windows_10_Firefox_54_UserAgent() {

        String uaString = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0";
        UserAgent ua = UserAgent.ParseUserAgentString(uaString, true);

        Assert.assertEquals(ua.getOS(),"Windows");
        Assert.assertEquals(ua.getOSMajorVersion(),"10");
        Assert.assertEquals(ua.getOSMinorVersion(),"0");
        Assert.assertEquals(ua.getBrowser(),"Firefox");
        Assert.assertEquals(ua.getBrowserMajorVersion(),"54");
        Assert.assertEquals(ua.getBrowserMinorVersion(),"0");
    }


}
