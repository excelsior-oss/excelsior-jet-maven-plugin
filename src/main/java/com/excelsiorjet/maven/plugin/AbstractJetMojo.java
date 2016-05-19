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

import com.excelsiorjet.JetHome;
import com.excelsiorjet.JetHomeException;
import com.excelsiorjet.Txt;
import com.excelsiorjet.Utils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.excelsiorjet.Txt.s;
import static com.excelsiorjet.maven.plugin.TomcatConfig.WEBAPPS_DIR;

/**
 * Parent of Excelsior JET Maven Plugin mojos.
 *
 * @author Nikita Lipsky
 */
public abstract class AbstractJetMojo extends AbstractMojo {

    public enum ApplicationType {
        PLAIN,
        TOMCAT
    }

    protected ApplicationType appType;

    /**
     * The Maven Project Object.
     */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    protected MavenProject project;

    /**
     * The main application class.
     */
    @Parameter(property = "mainClass")
    protected String mainClass;

    /**
     * The main application jar.
     * The default is the main project artifact, if it is a jar file.
     */
    @Parameter(property = "mainJar", defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    protected File mainJar;

    /**
     * The main web application archive.
     * The default is the main project artifact, if it is a war file.
     */
    @Parameter(property = "mainWar", defaultValue = "${project.build.directory}/${project.build.finalName}.war")
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
     * </p>
     */
    @Parameter(property = "jetOutputDir", defaultValue = "${project.build.directory}/jet")
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
     * Directory containing additional package files - README, license, media, help files, native libraries, and the like.
     * The plugin will copy its contents recursively to the final application package.
     * <p>
     * By default, the plugin assumes that those files reside in the "src/main/jetresources/packagefiles" subdirectory
     * of your project, but you may also dynamically generate the contents of the package files directory
     * by means of other Maven plugins such as {@code maven-resources-plugin}.
     * </p>
     */
    @Parameter(property = "packageFilesDir", defaultValue = "${project.basedir}/src/main/jetresources/packagefiles")
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
     * By default, they are placed into the "src/main/jetresources" subdirectory of your project.
     * It is recommended to commit the collected profiles (.usg, .startup) to VCS to enable the plugin
     * to re-use them during subsequent builds without performing a Test Run.
     *
     * @see TestRunMojo
     */
    @Parameter(property = "execProfilesDir", defaultValue = "${project.basedir}/src/main/jetresources")
    protected File execProfilesDir;

    /**
     * The base file name of execution profiles. By default, ${project.artifactId} is used.
     */
    @Parameter(property = "execProfilesName", defaultValue = "${project.artifactId}")
    protected String execProfilesName;

    protected static final String BUILD_DIR = "build";
    protected static final String LIB_DIR = "lib";

    protected JetHome checkPrerequisites() throws MojoFailureException {
        Txt.log = getLog();


        switch (project.getPackaging().toLowerCase()) {
            case "jar":
                if (!mainJar.exists()) {
                    throw new MojoFailureException(s("JetMojo.MainJarNotFound.Failure", mainJar.getAbsolutePath()));
                }
                appType = ApplicationType.PLAIN;
                // check main class
                if (Utils.isEmpty(mainClass)) {
                    throw new MojoFailureException(s("JetMojo.MainNotSpecified.Failure"));
                }
                break;
            case "war":
                if (!mainWar.exists()) {
                    throw new MojoFailureException(s("JetMojo.MainWarNotFound.Failure", mainWar.getAbsolutePath()));
                }

                tomcatConfiguration.fillDefaults();

                appType = ApplicationType.TOMCAT;
                break;
            default:
                throw new MojoFailureException(s("JetMojo.BadPackaging.Failure", project.getPackaging()));
        }


        // check jet home
        JetHome jetHomeObj;
        try {
            jetHomeObj = Utils.isEmpty(jetHome)? new JetHome() : new JetHome(jetHome);

        } catch (JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        }

        return jetHomeObj;
    }

    protected void mkdir(File dir) throws MojoExecutionException {
        if (!dir.exists() && !dir.mkdirs()) {
            if (!dir.exists()) {
                throw new MojoExecutionException(s("JetMojo.DirCreate.Error", dir.getAbsolutePath()));
            }
            getLog().warn(s("JetMojo.DirCreate.Warning", dir.getAbsolutePath()));
        }
    }

    protected File getBuildDir() {
        return new File(jetOutputDir, BUILD_DIR);
    }

    protected File createBuildDir() throws MojoExecutionException {
        File buildDir = getBuildDir();
        mkdir(buildDir);
        return buildDir;
    }

    protected File getTomcatHome() {
        return new File(tomcatConfiguration.tomcatHome);
    }

    protected File getTomcatInBuildDir() {
        return new File(getBuildDir(), getTomcatHome().getName());
    }

    protected static class Dependency {
        final String dependency;
        final boolean isLib;

        public Dependency(String dependency, boolean isLib) {
            this.dependency = dependency;
            this.isLib = isLib;
        }
    }

    private void copyDependency(File from, File to, File buildDir, List<Dependency> dependencies, boolean isLib) {
        try {
            Utils.copyFile(from.toPath(), to.toPath());
            dependencies.add(new Dependency(buildDir.toPath().relativize(to.toPath()).toString(), isLib));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies project dependencies.
     *
     * @return list of dependencies relative to buildDir
     */
    protected List<Dependency> copyDependencies(File buildDir, File mainJar) throws MojoExecutionException {
        File libDir = new File(buildDir, LIB_DIR);
        mkdir(libDir);
        ArrayList<Dependency> dependencies = new ArrayList<>();
        try {
            copyDependency(mainJar, new File(buildDir, mainJar.getName()), buildDir, dependencies, false);
            project.getArtifacts().stream()
                    .filter(a -> a.getFile().isFile())
                    .forEach(a ->
                            copyDependency(a.getFile(), new File(libDir, a.getFile().getName()), buildDir,
                                    dependencies, !a.getGroupId().equals(project.getGroupId()))
                    )
            ;
            return dependencies;
        } catch (Exception e) {
            throw new MojoExecutionException(s("JetMojo.ErrorCopyingDependency.Exception"), e);
        }
    }

    /**
     * Copies Tomcat server to the build directory and main project artifact (.war) to
     * "webapps" folder of copied Tomcat.
     *
     * @throws MojoExecutionException
     */
    protected void copyTomcatAndWar() throws MojoExecutionException {
        try {
            Utils.copyDirectory(getTomcatHome().toPath(), getTomcatInBuildDir().toPath());
            String warName = (Utils.isEmpty(tomcatConfiguration.warDeployName))? mainWar.getName() : tomcatConfiguration.warDeployName;
            Utils.copyFile(mainWar.toPath(), new File(getTomcatInBuildDir(), WEBAPPS_DIR + File.separator + warName).toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(s("JetMojo.ErrorCopyingTomcat.Exception"), e);
        }
    }


}
