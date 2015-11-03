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
package com.excelsior.jet.maven.plugin;

import com.excelsior.jet.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static com.excelsior.jet.Txt.s;

/**
 *  Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Mojo( name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JetMojo extends AbstractMojo {

    /**
     * The Maven Project Object.
     */
    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;

    /**
     * The main application class.
     */
    @Parameter(property = "mainClass", required = true)
    private String mainClass;

    /**
     * Excelsior JET installation directory.
     * If it is not specified, it is detected by the following algorithm:
     * <ul>
     *   <li> first -Djet.home system property is checked, if it is set, jet home is set from it</li>
   	 *   <li> then JET_HOME environment variable is checked, if it is set, jet home is set from it </li>
   	 *   <li> finally PATH environment variable is scanned for appropriate jet home </li>
     * </ul>
     */
    @Parameter(property = "jetHome", defaultValue = "${jet.home}")
    private String jetHome;

    /**
     * Directory for Excelsior JET temporary files build process
     * and the target directory for resulting package.
     * <p/>
     * "app" subdirectory of jetOutputDir will contain the final self-contained package
     * that you may deploy to another PCs by simple copy operation.
     * For convenience, "${project.build.finalName}.zip" is also created that is ZIP archive
     * of "app" directory, if 'zipOutput' property is set to {@code true}.
     */
    @Parameter(property = "jetOutputDir", defaultValue = "${project.build.directory}/jet")
    private File jetOutputDir;

    /**
     * Target executable name. If not set, the main class name is used for executable name.
     */
    @Parameter(property = "outputName")
    private String outputName;

    /**
     * Windows .ico file to associate with resulting executable file.
     */
    @Parameter(property = "icon", defaultValue = "${project.basedir}/src/main/jetresources/icon.ico")
    private File icon;

    /**
     * If set to {@code true} the resulting executable file will not show console on Windows.
     */
    @Parameter(property = "hideConsole")
    boolean hideConsole;

    /**
     * Controls creating zip archive containing self-contained package as the final step of build process.
     */
    @Parameter(property = "zipOutput", defaultValue = "true")
    private boolean zipOutput;

    private static final String BUILD_DIR = "build";
    private static final String LIB_DIR = "lib";
    private static final String PACKAGE_DIR = "app";

    public void execute() throws MojoExecutionException, MojoFailureException {
        Txt.log = getLog();

        // checking prerequisites

        // first check that main jar were built
        Build build = project.getBuild();
        File jar = new File(build.getDirectory(), build.getFinalName() + ".jar");
        if (!jar.exists()) {
            getLog().error(s("JetMojo.MainJarNotFound.Error", jar.getAbsolutePath()));
            throw new MojoFailureException(s("JetMojo.MainJarNotFound.Failure"));
        }

        // check main class
        if (Utils.isEmpty(mainClass)) {
            throw new MojoFailureException(s("JetMojo.MainNotSpecified.Failure"));
        }

        // check jet home
        JetHome jetHomeObj;
        try {
            jetHomeObj = Utils.isEmpty(jetHome)? new JetHome() : new JetHome(jetHome);
        } catch (JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        }

        // creating output dirs
        File buildDir = new File(jetOutputDir, BUILD_DIR);
        buildDir.mkdirs();
        File libDir = new File(buildDir, LIB_DIR);
        libDir.mkdirs();
        File packageDir = new File(jetOutputDir, PACKAGE_DIR);
        Utils.cleanDirectory(packageDir);

        // copying project dependencies
        ArrayList<String> compilerArgs = new ArrayList<>();
        try {
            Files.copy(jar.toPath(), new File(buildDir, jar.getName()).toPath());
            compilerArgs.add(jar.getName());
            for (Artifact artifact: project.getArtifacts()) {
                File file = artifact.getFile();
                if (file.isFile()) {
                    File dest = new File(libDir, file.getName());
                    if (!dest.exists()) {
                        Files.copy(file.toPath(), dest.toPath());
                    }
                    compilerArgs.add(LIB_DIR + "/" + file.getName());
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(s("JetMojo.ErrorCopyingDependency.Exception"), e);
        }

        mainClass = mainClass.replace('.', '/');
        if (outputName == null) {
            int lastSlash = mainClass.lastIndexOf('/');
            outputName = lastSlash < 0 ? mainClass : mainClass.substring(lastSlash + 1);
        }

        try {
            // compiling application with AOT compiler
            if (Utils.isWindows()) {
                if (icon.isFile()) {
                    compilerArgs.add(icon.getAbsolutePath());
                }
                if (hideConsole) {
                    compilerArgs.add("-gui+");
                }
            }
            compilerArgs.add("-main=" + mainClass);
            compilerArgs.add("-outputname=" + outputName);
            compilerArgs.add("-decor=ht");
            if (new JetCompiler(jetHomeObj, compilerArgs.toArray(new String[compilerArgs.size()]))
                    .workingDirectory(buildDir).withLog(getLog()).execute() != 0)
            {
                throw new MojoFailureException(s("JetMojo.Build.Failure"));
            }

            // packaging built executable with required Excelsior JET runtime files
            // into a self-contained directory
            if (new JetPackager(jetHomeObj,
                    "-add-file", Utils.mangleExeName(outputName), "/",
                    "-target", packageDir.getAbsolutePath())
                    .workingDirectory(buildDir).withLog(getLog()).execute() != 0)
            {
                throw new MojoFailureException(s("JetMojo.Package.Failure"));
            }

            if (zipOutput) {
                getLog().info(s("JetMojo.ZipApp.Info"));
                File targetZip = new File(jetOutputDir, build.getFinalName() + ".zip");
                Utils.compressZipfile(packageDir, targetZip);
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetZip.Info", targetZip.getAbsolutePath()));
            } else {
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetDir.Info", packageDir.getAbsolutePath()));
            }


        } catch (CmdLineToolException | IOException e) {
            getLog().error(e.getMessage());
            throw new MojoExecutionException(s("JetMojo.Unexpected.Error"), e);
        }
    }
}
