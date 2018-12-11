package com.applitools.eyes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;

public class ConnectionFactory implements HttpURLConnectionFactory {

    private Proxy proxy;
    private AbstractProxySettings abstractProxySettings;

    public ConnectionFactory(AbstractProxySettings abstractProxySettings) {
        this.abstractProxySettings = abstractProxySettings;
    }

    private void initializeProxy() {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(abstractProxySettings.getUri(), abstractProxySettings.getPort()));
    }

    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        initializeProxy();
        return (HttpURLConnection) url.openConnection(proxy);
    }
}