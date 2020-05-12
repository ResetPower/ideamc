package org.imcl.exceptions;

public class PluginLoaderException extends Exception {
    public PluginLoaderException() {
        super();
    }
    public PluginLoaderException(String msg) {
        super(msg);
    }
    public PluginLoaderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
