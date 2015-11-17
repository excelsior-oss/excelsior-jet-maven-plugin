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

import com.excelsiorjet.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.excelsiorjet.Txt.s;

/**
 *  Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
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
    protected String mainClass;

    /**
     * The main application jar.
     * The default is the main project artifact, which must be a jar file.
     */
    @Parameter(property = "mainJar", defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    protected File mainJar;

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
     * Target executable name. If not set, the main class name is used.
     */
    @Parameter(property = "outputName")
    protected String outputName;

    /**
     * Windows .ico file to associate with the resulting executable file.
     */
    @Parameter(property = "icon", defaultValue = "${project.basedir}/src/main/jetresources/icon.ico")
    protected File icon;

    /**
     * If set to {@code true}, the resulting executable file will not show a console on Windows.
     */
    @Parameter(property = "hideConsole")
    protected boolean hideConsole;

    //packaging types
    private static final String ZIP = "zip";
    private static final String NONE = "none";
    private static final String EXCELSIOR_INSTALLER = "excelsior-installer";

    /**
     * Packaging type of the resulting bundle. Permitted values are
     * <ul>
     *     <li>zip  - a zip archive with the self-contained application package</li>
     *     <li>
     *         excelsior-installer - self-extracting installer with standard GUI for Windows
     *         and command-line interface for Linux
     *     </li>
     *     <li>none - skip packaging</li>
     * </ul>
     */
    @Parameter(property = "packaging", defaultValue = ZIP)
    protected String packaging;

    /**
     * The vendor of the application. Required for Windows version information and Excelsior Installer.
     * By default, {@code ${project.organization.name}} is used.
     * If it is not set, the second part of POM's groupId identifier is used with first letter capitalized.
     */
    @Parameter(property = "vendor", defaultValue = "${project.organization.name}")
    protected String vendor;

    /**
     * The product name. Required for Windows version information and Excelsior Installer.
     * By default, {@code ${project.oname}} is used.
     * If it is not set, the POM's artifactId identifier is used.
     */
    @Parameter(property = "product", defaultValue = "${project.name}")
    protected String product;

    /**
     * The product version. Required for Excelsior Installer.
     * For Windows version information {@link #windowsExeVersion} Mojo parameter is used.
     */
    @Parameter(property = "version", defaultValue = "${project.version}")
    protected String version;

    /**
     * If set to {@code true}, Windows version information will be assigned to the final executable.
     */
    @Parameter(property = "addWindowsVersionInfo", defaultValue = "true")
    protected boolean addWindowsVersionInfo;

    /**
     * The Windows executable version. Must have {@code v1.v2.v3.v4} format where vi is a digit.
     * By default, {@code ${project.version}} is used. If it does not meet required format, it is coerced to it.
     * Thus version "1.2.3-SNAPSHOT" becomes "1.2.3.0"
     */
    @Parameter(property = "windowsExeVersion", defaultValue = "${project.version}")
    protected String windowsExeVersion;

    /**
     * The Windows executable version info legal copyright.
     * By default, {@code Copyright �{$project.inceptionYear},[curYear] [vendor]} is used.
     */
    @Parameter(property = "windowsExeCopyright")
    protected String windowsExeCopyright;

    /**
     * The Windows executable description.
     */
    @Parameter(property = "windowsExeDescription", defaultValue = "${project.name}")
    protected String windowsExeDescription;

    /**
     * The license agreement file in ANSI format. Used for Excelsior Installer.
     */
    @Parameter(property = "eula", defaultValue = "${project.basedir}/src/main/jetresources/eula.txt")
    protected File eula;

    /**
     * The license agreement file in UTF-16LE format. Used for Excelsior Installer.
     */
    @Parameter(property = "unicodeEula", defaultValue = "${project.basedir}/src/main/jetresources/unicodeEula.txt")
    protected File unicodeEula;

    /**
     * The splash for Excelsior Installer in BMP format.
     */
    @Parameter(property = "installerSplash", defaultValue = "${project.basedir}/src/main/jetresources/installerSplash.bmp")
    protected File installerSplash;


    private static final String BUILD_DIR = "build";
    private static final String LIB_DIR = "lib";
    private static final String APP_DIR = "app";

    private void checkVersionInfo(JetHome jetHome) throws JetHomeException {
        if (!Utils.isWindows()) {
            addWindowsVersionInfo = false;
        }
        if (addWindowsVersionInfo && (jetHome.getEdition() == JetEdition.STANDARD)) {
            getLog().warn(s("JetMojo.NoVersionInfoInStandard.Warning"));
            addWindowsVersionInfo = false;
        }
        if (addWindowsVersionInfo || EXCELSIOR_INSTALLER.equals(packaging)) {
            if (Utils.isEmpty(vendor)) {
                //no organization name. Get it from groupId that cannot be empty.
                String[] groupId = project.getGroupId().split("\\.");
                if (groupId.length >= 2) {
                    vendor = groupId[1];
                } else {
                    vendor = groupId[0];
                }
                vendor = Character.toUpperCase(vendor.charAt(0)) + vendor.substring(1);
            }
            if (Utils.isEmpty(product)) {
                // no project name get it from artifactId.
                product = project.getArtifactId();
            }
        }
        if (addWindowsVersionInfo) {
            //Coerce windowsExeVersion to v1.v2.v3.v4 format.
            String[] versions = windowsExeVersion.split("\\.");
            String[] finalVersions = new String[]{"0", "0", "0", "0"};
            for (int i = 0; i < Math.min(versions.length, 4); ++i) {
                try {
                    finalVersions[i] = Integer.decode(versions[i]).toString();
                } catch (NumberFormatException e) {
                    int minusPos = versions[i].indexOf('-');
                    if (minusPos > 0) {
                        String v = versions[i].substring(0, minusPos);
                        try {
                            finalVersions[i] = Integer.decode(v).toString();
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
            }
            String finalVersion = String.join(".", finalVersions);
            if (!windowsExeVersion.equals(finalVersion)) {
                getLog().warn(s("JetMojo.NotCompatibleExeVersion.Warning", windowsExeVersion, finalVersion));
                windowsExeVersion = finalVersion;
            }

            if (windowsExeCopyright == null) {
                String inceptionYear = project.getInceptionYear();
                String curYear = new SimpleDateFormat("yyyy").format(new Date());
                String years = Utils.isEmpty(inceptionYear)? curYear : inceptionYear + "," + curYear;
                windowsExeCopyright = "Copyright \\x00a9 " + years + " " + vendor;
            }
            if (windowsExeDescription == null) {
                windowsExeDescription = product;
            }
        }
    }

    private JetHome checkPrerequisites() throws MojoFailureException {
        // first check that main jar were built
        if (!mainJar.exists()) {
            String error;
            if (!"jar".equalsIgnoreCase(project.getPackaging())) {
                error = s("JetMojo.BadPackaging.Failure", project.getPackaging());
            } else {
                error = s("JetMojo.MainJarNotFound.Failure", mainJar.getAbsolutePath());
            }
            getLog().error(error);
            throw new MojoFailureException(error);
        }

        // check main class
        if (Utils.isEmpty(mainClass)) {
            throw new MojoFailureException(s("JetMojo.MainNotSpecified.Failure"));
        }

        //normalize main and set outputName
        mainClass = mainClass.replace('.', '/');
        if (outputName == null) {
            int lastSlash = mainClass.lastIndexOf('/');
            outputName = lastSlash < 0 ? mainClass : mainClass.substring(lastSlash + 1);
        }

        //check packaging type
        switch (packaging) {
             case ZIP: case NONE: case EXCELSIOR_INSTALLER: break;
             default: throw new MojoFailureException(s("JetMojo.UnknownPackagingMode.Failure", packaging));
        }

        if (eula.exists() && unicodeEula.exists()) {
            throw new MojoFailureException(s("JetMojo.BothEulaParameters.Failure"));
        }

        // check jet home && version info
        JetHome jetHomeObj;
        try {
            jetHomeObj = Utils.isEmpty(jetHome)? new JetHome() : new JetHome(jetHome);

            checkVersionInfo(jetHomeObj);
        } catch (JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        }

        return jetHomeObj;
    }

    private void mkdir(File dir) throws MojoExecutionException {
        if (!dir.exists() && !dir.mkdirs()) {
            if (!dir.exists()) {
                throw new MojoExecutionException(s("JetMojo.DirCreate.Error", dir.getAbsolutePath()));
            }
            getLog().warn(s("JetMojo.DirCreate.Warning", dir.getAbsolutePath()));
        }
    }

    private void copyDependency(File from, File to, File buildDir, ArrayList<String> dependencies) {
        try {
            if (!to.exists()) {
                Files.copy(from.toPath(), to.toPath());
            }
            dependencies.add(buildDir.toPath().relativize(to.toPath()).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies project dependencies.
     *
     * @return list of dependencies relative to buildDir
     */
    private ArrayList<String> copyDependencies(File buildDir, File mainJar) throws MojoExecutionException {
        File libDir = new File(buildDir, LIB_DIR);
        mkdir(libDir);
        ArrayList<String> dependencies = new ArrayList<>();
        try {
            copyDependency(mainJar, new File(buildDir, mainJar.getName()), buildDir, dependencies);
            project.getArtifacts().stream()
                    .map(Artifact::getFile)
                    .filter(File::isFile)
                    .forEach(f -> copyDependency(f, new File(libDir, f.getName()), buildDir, dependencies))
            ;
            return dependencies;
        } catch (Exception e) {
            throw new MojoExecutionException(s("JetMojo.ErrorCopyingDependency.Exception"), e);
        }
    }

    /**
     * Invokes the Excelsior JET AOT compiler.
     */
    private void compile(JetHome jetHome, File buildDir, ArrayList<String> compilerArgs) throws MojoFailureException, CmdLineToolException {
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

        if (addWindowsVersionInfo) {
            compilerArgs.add("-versioninfocompanyname=" + vendor);
            compilerArgs.add("-versioninfoproductname=" + product);
            compilerArgs.add("-versioninfoproductversion=" + windowsExeVersion);
            compilerArgs.add("-versioninfolegalcopyright=" + windowsExeCopyright);
            compilerArgs.add("-versioninfofiledescription=" + windowsExeDescription);
        }

        if (new JetCompiler(jetHome, compilerArgs.toArray(new String[compilerArgs.size()]))
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Build.Failure"));
        }
    }

    /**
     * Packages the generated executable and required Excelsior JET runtime files
     * as a self-contained directory
     */
    private void createAppDir(JetHome jetHome, File buildDir, File appDir) throws CmdLineToolException, MojoFailureException {
        if (new JetPackager(jetHome,
                 "-add-file", Utils.mangleExeName(outputName), "/",
                 "-target", appDir.getAbsolutePath())
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Package.Failure"));
        }
    }

    /**
     * Packages the generated executable and required Excelsior JET runtime files
     * as a excelsior installer file.
     */
    private File packWithEI(JetHome jetHome, File buildDir) throws CmdLineToolException, MojoFailureException {
        File target = new File(jetOutputDir, Utils.mangleExeName(project.getBuild().getFinalName()));
        ArrayList<String> xpackArgs = new ArrayList<>();
        if (eula.exists()) {
            xpackArgs.add("-eula"); xpackArgs.add(eula.getAbsolutePath());
        } else if (unicodeEula.exists()) {
            xpackArgs.add("-unicode-eula"); xpackArgs.add(unicodeEula.getAbsolutePath());
        }
        if (installerSplash.exists()) {
            xpackArgs.add("-splash"); xpackArgs.add(installerSplash.getAbsolutePath());
        }
        xpackArgs.addAll(Arrays.asList(
                        "-add-file", Utils.mangleExeName(outputName), "/",
                        "-backend", "excelsior-installer",
                        "-company", vendor,
                        "-product", product,
                        "-version", version,
                        "-target", target.getAbsolutePath())
        );
        if (new JetPackager(jetHome, xpackArgs.toArray(new String[xpackArgs.size()]))
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Package.Failure"));
        }
        return target;
    }

    static void compressZipfile(File sourceDir, File outputFile) throws IOException {
        ZipArchiveOutputStream zipFile = new ZipArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFile)));
        compressDirectoryToZipfile(sourceDir.getAbsolutePath(), sourceDir.getAbsolutePath(), zipFile);
        IOUtils.closeQuietly(zipFile);
    }

    private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipArchiveOutputStream out) throws IOException {
        File[] files = new File(sourceDir).listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
            } else {
                ZipArchiveEntry entry = new ZipArchiveEntry(file.getAbsolutePath().substring(rootDir.length()+1));
                if (Utils.isUnix() && file.canExecute()) {
                    entry.setUnixMode(0100777);
                }
                out.putArchiveEntry(entry);
                InputStream in = new BufferedInputStream(new FileInputStream(sourceDir + File.separator +  file.getName()));
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                out.closeArchiveEntry();
            }
        }
    }

    private void packageBuild(JetHome jetHome, File buildDir, File packageDir) throws IOException, MojoFailureException, CmdLineToolException {
        switch (packaging){
            case ZIP:
                getLog().info(s("JetMojo.ZipApp.Info"));
                File targetZip = new File(jetOutputDir, project.getBuild().getFinalName() + ".zip");
                compressZipfile(packageDir, targetZip);
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetZip.Info", targetZip.getAbsolutePath()));
                break;
            case EXCELSIOR_INSTALLER :
                File target = packWithEI(jetHome, buildDir);
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetEI.Info", target.getAbsolutePath()));
                break;
            default:
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetDir.Info", packageDir.getAbsolutePath()));
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Txt.log = getLog();

        JetHome jetHome = checkPrerequisites();

        // creating output dirs
        File buildDir = new File(jetOutputDir, BUILD_DIR);
        mkdir(buildDir);
        File appDir = new File(jetOutputDir, APP_DIR);
        //cleanup packageDir
        try {
            Utils.cleanDirectory(appDir);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        ArrayList<String> compilerArgs = copyDependencies(buildDir, mainJar);

        try {
            compile(jetHome, buildDir, compilerArgs);

            createAppDir(jetHome, buildDir, appDir);

            packageBuild(jetHome, buildDir, appDir);

        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoExecutionException(s("JetMojo.Unexpected.Error"), e);
        }
    }
}
