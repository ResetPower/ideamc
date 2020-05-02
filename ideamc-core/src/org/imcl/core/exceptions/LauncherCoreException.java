package org.imcl.core.exceptions;

public class LauncherCoreException extends Exception {
    public LauncherCoreException() {
        super();
    }
    public LauncherCoreException(String msg) {
        super(msg);
    }
    public LauncherCoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
