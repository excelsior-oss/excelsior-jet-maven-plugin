package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.api.AbstractLog;
import org.apache.maven.plugin.logging.Log;

public class MavenLog extends AbstractLog {

    private final Log mavenLog;

    public MavenLog(Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    @Override
    public void info(CharSequence msg) {
        mavenLog.info(msg);
    }

    @Override
    public void warn(CharSequence msg) {
        mavenLog.warn(msg);
    }

    @Override
    public void warn(CharSequence msg, Throwable t) {
        mavenLog.warn(msg, t);
    }

    @Override
    public void error(CharSequence msg) {
        mavenLog.error(msg);
    }
}
