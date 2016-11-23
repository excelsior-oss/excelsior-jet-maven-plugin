/*
 * Copyright (c) 2015, Excelsior LLC.
 *
 *  This file is part of Excelsior JET Maven Plugin.
 *
 *  Excelsior JET Maven Plugin is free software:
 *  you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Excelsior JET Maven Plugin is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Excelsior JET Maven Plugin.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.api.tasks.ApplicationType;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.tasks.JetTaskFailureException;
import com.excelsiorjet.api.tasks.config.DependencySettings;
import com.excelsiorjet.api.tasks.config.ProjectDependency;
import com.excelsiorjet.api.tasks.config.TomcatConfig;
import com.excelsiorjet.api.util.Utils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.excelsiorjet.api.util.Txt.s;
import static java.util.Collections.emptyList;

/**
 * Parent of Excelsior JET Maven Plugin mojos.
 *
 * @author Nikita Lipsky
 */
public abstract class AbstractJetMojo extends AbstractMojo {

    /**
     * The Maven Project Object.
     */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File targetDir;

    /**
     * Application type. Permitted values are:
     * <dl>
     * <dt>plain</dt>
     * <dd>plain Java application, that runs standalone,
     * default type for {@code <packaging>jar</packaging>} packaging type</dd>
     * <dt>invocation-dynamic-library</dt>
     * <dd>dynamic library callable from a non-Java environment</dd>
     * <dt>windows-service</dt>
     * <dd>Windows service (Windows only)</dd>
     * <dt>tomcat</dt>
     * <dd>servlet-based Java application, that runs within Tomcat servlet container,
     * default type for {@code <packaging>war</packaging>} packaging type</dd>
     * </dl>
     */
    @Parameter(property = "appType")
    protected String appType;

    /**
     * The main application class.
     */
    @Parameter(property = "mainClass")
    protected String mainClass;

    /**
     * The main application jar.
     * The default is the main project artifact, if it is a jar file.
     */
    @Parameter(property = "mainJar")
    protected File mainJar;

    /**
     * The main web application archive.
     * The default is the main project artifact, if it is a war file.
     */
    @Parameter(property = "mainWar")
    protected File mainWar;

    /**
     * Excelsior JET installation directory.
     * If unspecified, the plugin uses the following algorithm to set the value of this property:
     * <ul>
     *   <li> If the jet.home system property is set, use its value</li>
     *   <li> Otherwise, if the JET_HOME environment variable is set, use its value</li>
     *   <li> Otherwise scan the PATH environment variable for a suitable Excelsior JET installation</li>
     * </ul>
     */
    @Parameter(property = "jetHome", defaultValue = "${jet.home}")
    protected String jetHome;

    /**
     * Directory for temporary files generated during the build process
     * and the target directory for the resulting package.
     * <p>
     * The plugin will place the final self-contained package in the "app" subdirectory
     * of {@code jetOutputDir}. You may deploy it to other systems using a simple copy operation.
     * For convenience, the plugin will also create a ZIP archive {@code ${project.build.finalName}.zip}
     * with the same content, if the {@code packaging} parameter is set to {@code zip}.
     *
     * Default value is ${project.build.directory}/jet
     * </p>
     */
    @Parameter(property = "jetOutputDir")
    protected File jetOutputDir;

    /**
     * Tomcat web applications specific parameters.
     *
     * @see TomcatConfig#tomcatHome
     * @see TomcatConfig#warDeployName
     * @see TomcatConfig#hideConfig
     * @see TomcatConfig#genScripts
     */
    @Parameter(property = "tomcatConfiguration")
    protected TomcatConfig tomcatConfiguration;

    /**
     * Directory containing Excelsior JET specific resource files such as application icons, installer splash,  etc.
     * It is recommended to place the directory in the source root directory.
     * The default value is "src/main/jetresources" subdirectory of the Maven project.
     */
    @Parameter(property = "jetResourcesDir", defaultValue = "${project.basedir}/src/main/jetresources")
    protected File jetResourcesDir;


    /**
     * Directory containing additional package files - README, license, media, help files, native libraries, and the like.
     * The plugin will copy its contents recursively to the final application package.
     * <p>
     * By default, the plugin assumes that those files reside in the {@code packagefiles} subdirectory of
     * {@link #jetResourcesDir} of your project, but you may also dynamically generate the contents
     * of the package files directory by means of other Maven plugins such as {@code maven-resources-plugin}.
     *
     * </p>
     */
    @Parameter(property = "packageFilesDir")
    protected File packageFilesDir;

    /**
     * Defines system properties and JVM arguments to be passed to the Excelsior JET JVM at runtime, e.g.:
     * {@code -Dmy.prop1 -Dmy.prop2=value -ea -Xmx1G -Xss128M -Djet.gc.ratio=11}.
     * <p>
     * Please note that only some of the non-standard Oracle HotSpot JVM arguments
     * (those prefixed with {@code -X}) are recognized.
     * For instance, the {@code -Xms} argument setting the initial Java heap size on HotSpot
     * has no meaning for the Excelsior JET JVM, which has a completely different
     * memory management policy. At the same time, Excelsior JET provides its own system properties
     * for GC tuning, such as {@code -Djet.gc.ratio}.
     * For more details, consult the {@code README} file of the plugin or the Excelsior JET User's Guide.
     * </p>
     */
    @Parameter(property = "jvmArgs")
    protected String[] jvmArgs;

    /**
     * The target location for application execution profiles gathered during Test Run.
     * By default, they are placed into the {@link #jetResourcesDir} directory.
     * It is recommended to commit the collected profiles (.usg, .startup) to VCS to enable the plugin
     * to re-use them during subsequent builds without performing a Test Run.
     *
     * @see TestRunMojo
     */
    @Parameter(property = "execProfilesDir")
    protected File execProfilesDir;

    /**
     * The base file name of execution profiles. By default, ${project.artifactId} is used.
     *
     * Default value is ${project.artifactId}
     */
    @Parameter(property = "execProfilesName")
    protected String execProfilesName;

    /**
     * Command line arguments that will be passed to the application during startup accelerator profiling
     * and the test run.
     * You may also set the parameter via the {@code jet.runArgs} system property, where arguments
     * are comma separated (use "\" to escape commas within arguments,
     * i.e. {@code -Djet.runArgs="arg1,Hello\, World"} will be passed to your application as {@code arg1 "Hello, World"})
     */
    @Parameter(property = "runArgs")
    protected String[] runArgs;

    /**
     * List of settings of project dependencies.
     *
     * @see DependencySettings#optimize
     * @see DependencySettings#protect
     * @see DependencySettings#pack
     * @see DependencySettings#isLibrary
     * @see DependencySettings#path
     * @see DependencySettings#packagePath
     */
    @Parameter(property = "dependencies")
    protected DependencySettings[] dependencies;

    /**
     * If set to {@code true}, project dependencies are ignored.
     */
    @Parameter(property = "ignoreProjectDependencies")
    protected boolean ignoreProjectDependencies;

    public List<ProjectDependency> getDependencies() {
        if (ignoreProjectDependencies) {
            return Collections.emptyList();
        } else {
            return project.getArtifacts().stream().
                    map(artifact -> new ProjectDependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getFile(), false)).
                    collect(Collectors.toList());
        }
    }

    protected JetProject getJetProject() throws JetTaskFailureException {
        JetProject.configureEnvironment(new MavenLog(getLog()), ResourceBundle.getBundle("MavenStrings", Locale.ENGLISH));
        validateSettings();

        return new JetProject(project.getArtifactId(), project.getGroupId(), project.getVersion(), getAppType(),
                targetDir, jetResourcesDir)
                        .mainJar(mainJar)
                        .mainWar(mainWar)
                        .mainClass(mainClass)
                        .tomcatConfiguration(tomcatConfiguration)
                        .projectDependencies(getDependencies())
                        .artifactName(project.getBuild().getFinalName())
                        .jetOutputDir(jetOutputDir)
                        .packageFilesDir(packageFilesDir)
                        .execProfilesDir(execProfilesDir)
                        .execProfilesName(execProfilesName)
                        .jvmArgs(jvmArgs)
                        .runArgs(this.runArgs)
                .dependencies(Arrays.asList(dependencies));
    }

    private ApplicationType getAppType() throws JetTaskFailureException {
        if (!Utils.isEmpty(appType)) {
            return JetProject.checkAndGetAppType(appType);
        }
        switch (project.getPackaging()) {
            case "jar": return ApplicationType.PLAIN;
            case "war": return ApplicationType.TOMCAT;
            default:
                throw new JetTaskFailureException(s("JetApi.BadPackaging.Failure", project.getPackaging()));
        }
    }

    private void validateSettings() throws JetTaskFailureException {
        if (getAppType() == ApplicationType.TOMCAT) {
            if (ignoreProjectDependencies) {
                throw new JetTaskFailureException(s("JetApi.IgnoreProjectDependenciesShouldNotBeSetForTomcatApplications"));
            }
        }
    }
}
