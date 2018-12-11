/*
 * Applitools software.
 */
package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;

/***
 * Encapsulates settings for sending Eyes communication via proxy.
 */
public abstract class AbstractProxySettings {
    protected String uri;
    protected String username;
    protected String password;
    protected int port;

    /**
     * @param uri      The proxy's URI.
     * @param port     The proxy's port
     * @param username The username to be sent to the proxy.
     * @param password The password to be sent to the proxy.
     */
    public AbstractProxySettings(String uri, int port, String username, String password) {
        ArgumentGuard.notNull(uri, "uri");
        this.uri = uri;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * @param uri      The proxy's URI.
     * @param port     The proxy's port
     */
    public AbstractProxySettings(String uri, int port) {
        this(uri, port, null, null);
    }

    /**
     * @param uri      The proxy's URI.
     * @param username The username to be sent to the proxy.
     * @param password The password to be sent to the proxy.
     */
    public AbstractProxySettings(String uri, String username, String password) {
        this(uri, 8888, username, password);
    }

    /**
     * Defines proxy settings with empty username/password.
     * @param uri The proxy's URI.
     */
    @SuppressWarnings("UnusedDeclaration")
    public AbstractProxySettings(String uri) {
        this(uri, 8888, null, null);
    }

    public String getUri() {
        return uri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }
}
