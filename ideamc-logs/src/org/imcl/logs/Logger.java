package org.imcl.logs;

import org.imcl.logs.files.FileChecker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    Logger() {
    }
    protected String getDate() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    public void info(String s) {
        String classname = new Exception().getStackTrace()[1].getClassName();
        String text = "["+getDate()+"] ["+classname+"/INFO] "+s;
        FileChecker.newLog(text);
        System.out.println(text);
    }
    public void debug(String s) {
        String classname = new Exception().getStackTrace()[1].getClassName();
        String text = "["+getDate()+"] ["+classname+"/DEBUG] "+s;
        FileChecker.newLog(text);
        System.out.println(text);
    }
    public void warn(String s) {
        String classname = new Exception().getStackTrace()[1].getClassName();
        String text = "["+getDate()+"] ["+classname+"/WARN] "+s;
        FileChecker.newLog(text);
        System.out.println(text);
    }
    public void error(String s) {
        String classname = new Exception().getStackTrace()[1].getClassName();
        String text = "["+getDate()+"] ["+classname+"/ERROR] "+s;
        FileChecker.newLog(text);
        System.err.println(text);
    }
}
