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
     * The location of the master Tomcat application server installation,
     * a required parameter for Web application projects.
     * Your .war artifact will be deployed into a copy of the master Tomcat installation and compiled together with it.
     *
     * You may also use the tomcat.home system property, or one of TOMCAT_HOME and CATALINA_HOME environment variables
     * to set this parameter.
     */
    public String tomcatHome;

    /**
     * The name of the war file to be deployed into Tomcat.
     * Default value is the name of your main artifact, which should be a war file.
     * <p>
     * By default, Tomcat uses the war file name as the context path of the respective Web application.
     * If you need your Web application to be on the "/" context path,
     * set warDeployName to "ROOT" value.
     * </p>
     */
    public String warDeployName;

    /**
     * If you do not want your end users to inspect or modify the Tomcat configuration files
     * located in &lt;tomcatHome&gt;/conf/, set this plugin parameter to {@code true}
     * to have them placed into the executable and not appear in the conf/ subdirectory
     * of end user installations of your Web application.
     */
    public boolean hideConfig;

    /**
     * If you want to continue using standard Tomcat scripts such as {@code startup} and {@code shutdown},
     * with the natively compiled Tomcat, set this plugin parameter to {@code true} (default).
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
