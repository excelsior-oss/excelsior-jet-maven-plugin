package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.Utils;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

import static com.excelsiorjet.Txt.s;

/**
 * Tomcat web applications specific parameters
 */
public class TomcatConfig {

    static final String WEBAPPS_DIR = "webapps";

    /**
     * The location of Tomcat application server installation
     * that is required parameter for Web applications native compilation.
     * Your .war artifact will be deployed into a copy of the Tomcat and compiled together with it.
     *
     * You may use the tomcat.home system property or either TOMCAT_HOME or CATALINA_HOME environment variables
     * to set the parameter.
     */
    public String tomcatHome;

    /**
     * The name of war to be deployed into Tomcat.
     * Default value is the name of your main artifact that should be war file.
     * <p>
     * By default, Tomcat uses name of war as context path of respective web application.
     * If you need for your web application to be on "/" context path,
     * set warDeployName to "ROOT" value.
     * </p>
     */
    public String warDeployName;

    /**
     * If you do not want your end users to inspect or modify the Tomcat configuration files,
     * those located in Tomcat-home/conf/, you may set this plugin parameter to {@code true}
     * to place them into the executable so the files will not appear in conf/ subdirectory
     * of end user installations of your Web application.
     */
    public boolean hideConfig;

    /**
     * If you use standard Tomcat scripts from Tomcat-home/bin/, such as startup, shutdown, etc.,
     * and wish to continue using them with the compiled Tomcat, set this plugin parameter to {@code true} (default),
     * As a result, the scripts working with the compiled Tomcat will be created in "target/jet/app/bin"
     * along with the executable.
     */
    public boolean genScripts = true;

    void fillDefaults() throws MojoFailureException {
        // check Tomcat home
        if (Utils.isEmpty(tomcatHome)) {
            tomcatHome = System.getProperty("tomcat.home");
            if (Utils.isEmpty(tomcatHome)) {
                tomcatHome = System.getenv("TOMCAT_HOME");
                if (Utils.isEmpty(tomcatHome)) {
                    tomcatHome = System.getenv("CATALINA_HOME");
                }
            }
        }

        if (Utils.isEmpty(tomcatHome)) {
            throw new MojoFailureException(s("JetMojo.TomcatNotSpecified.Failure"));
        }

        if (!new File(tomcatHome).exists()) {
            throw new MojoFailureException(s("JetMojo.TomcatDoesNotExist.Failure", tomcatHome));
        }

        if (!new File(tomcatHome, WEBAPPS_DIR).exists()) {
            throw new MojoFailureException(s("JetMojo.TomcatWebappsDoesNotExist.Failure", tomcatHome));
        }

        if (!Utils.isEmpty(warDeployName) && !warDeployName.endsWith(".war")) {
            warDeployName = warDeployName + ".war";
        }
    }
}
