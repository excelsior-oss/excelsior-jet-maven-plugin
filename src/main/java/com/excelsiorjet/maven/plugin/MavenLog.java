package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.api.log.Log;

/**
 * Implementation of {@code AbstractLog} that redirects logs into maven system log
 */
class MavenLog extends Log {

    private final org.apache.maven.plugin.logging.Log mavenLog;

    MavenLog(org.apache.maven.plugin.logging.Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    @Override public void debug(String msg, Throwable t) {
       mavenLog.debug(msg, t);
    }

    @Override
    public void info(String msg) {
        mavenLog.info(msg);
    }

    @Override
    public void warn(String msg) {
        mavenLog.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable t) {
        mavenLog.warn(msg, t);
    }

    @Override
    public void error(String msg) {
        mavenLog.error(msg);
    }
}
