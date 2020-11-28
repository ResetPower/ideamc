package org.imcl.logs;

import org.imcl.logs.constraints.ConstraintsKt;
import org.imcl.logs.files.FileChecker;

public class LogManager {
    public static Logger getLogger() {
        if (!ConstraintsKt.isLoggerLoaded()) {
            FileChecker.check();
            ConstraintsKt.setLoggerLoaded(true);
        }
        return new Logger();
    }
}
