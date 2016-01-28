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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.excelsiorjet.Txt.s;
import static com.excelsiorjet.EncodingDetector.detectEncoding;

/**
 *  Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo( name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JetMojo extends AbstractJetMojo {

    public static final String AUTO_DETECT_EULA_ENCODING = "autodetect";
    public static final String UNICODE_EULA_FLAG = "-unicode-eula";
    public static final String EULA_FLAG = "-eula";

    private static final Set<String> VALID_EULA_ENCODING_VALUES = new LinkedHashSet<String>() {{
        add(StandardCharsets.US_ASCII.name());
        add(StandardCharsets.UTF_16LE.name());
        add(AUTO_DETECT_EULA_ENCODING);
    }};

    /**
     * Target executable name. If not set, the main class name is used.
     */
    @Parameter(property = "outputName")
    protected String outputName;

    /**
     * (Windows) .ico file to associate with the resulting executable file.
     */
    @Parameter(property = "icon", defaultValue = "${project.basedir}/src/main/jetresources/icon.ico")
    protected File icon;

    /**
     * (Windows) If set to {@code true}, the resulting executable file will not have a console upon startup.
     */
    @Parameter(property = "hideConsole")
    protected boolean hideConsole;

    /**
     * (32-bit only) If set to {@code true}, the Global Optimizer is enabled
     * providing higher performance and lower memory usage for the compiled application.
     * Performing the Test Run is mandatory when Global Optimizer is enabled.
     * Global Optimizer is enabled automatically when you use Java Runtime Slim-Down.
     *
     * @see TestRunMojo
     * @see #javaRuntimeSlimDown
     */
    @Parameter(property = "globalOptimizer")
    protected boolean globalOptimizer;

    /**
     * (32-bit only) Java Runtime Slim-Down configuration parameters.
     *
     * @see SlimDownConfig#detachedBaseURL
     * @see SlimDownConfig#detachComponents
     * @see SlimDownConfig#detachedPackage
     */
    @Parameter(property = "javaRuntimeSlimDown")
    protected SlimDownConfig javaRuntimeSlimDown;

    /**
     * If set to {@code true}, the multi-app mode is enabled for the resulting executable
     * (executable with Java command line syntax).
     */
    @Parameter(property = "multiApp", defaultValue = "false")
    protected boolean multiApp;

    /**
     * Enable/disable startup accelerator.
     * If it is enabled, the compiled application will run after build
     * for {@link #profileStartupTimeout} seconds for collecting startup profile.
     */
    @Parameter(property = "profileStartup", defaultValue = "true")
    protected boolean profileStartup;

    /**
     * The duration of the after build profiling session in seconds after which the application
     * will be automatically terminated.
     */
    @Parameter(property = "profileStartupTimeout", defaultValue = "20")
    protected int profileStartupTimeout;

    /**
     * Add optional JET Runtime components to the package. Available optional components:
     * runtime_utilities, fonts, awt_natives, api_classes, jce,
     * accessibility, javafx, javafx-webkit, nashorn, cldr
     */
    @Parameter(property = "optRtFiles")
    protected String[] optRtFiles;

    //packaging types
    private static final String ZIP = "zip";
    private static final String NONE = "none";
    private static final String EXCELSIOR_INSTALLER = "excelsior-installer";

    /**
     * Application packaging mode. Permitted values are:
     * <dl>
     *   <dt>zip</dt>
     *   <dd>zip archive with a self-contained application package (default)</dd>
     *   <dt>excelsior-installer</dt>
     *   <dd>self-extracting installer with standard GUI for Windows
     *     and command-line interface for Linux</dd>
     *   <dt>none</dt>
     *   <dd>skip packaging altogether</dd>
     * </dl>
     */
    @Parameter(property = "packaging", defaultValue = ZIP)
    protected String packaging;

    /**
     * Application vendor name. Required for Windows version-information resource and Excelsior Installer.
     * By default, {@code ${project.organization.name}} is used.
     * If it is not set, the second part of the POM {@code groupId} identifier is used, with first letter capitalized.
     */
    @Parameter(property = "vendor", defaultValue = "${project.organization.name}")
    protected String vendor;

    /**
     * Product name. Required for Windows version-information resource and Excelsior Installer.
     * By default, {@code ${project.oname}} is used.
     * If it is not set, the POM's artifactId identifier is used.
     */
    @Parameter(property = "product", defaultValue = "${project.name}")
    protected String product;

    /**
     * Product version. Required for Excelsior Installer.
     * Note: To specify a different (more precise) version number for the Windows executable version-information resource,
     * use the {@link #winVIVersion} Mojo parameter.
     */
    @Parameter(property = "version", defaultValue = "${project.version}")
    protected String version;

    /**
     * (Windows) If set to {@code true}, a version-information resource will be added to the final executable.
     *
     * @see #vendor vendor
     * @see #product product
     * @see #winVIVersion winVIVersion
     * @see #winVICopyright winVICopyright
     * @see #winVIDescription winVIDescription
     */
    @Parameter(property = "addWindowsVersionInfo", defaultValue = "true")
    protected boolean addWindowsVersionInfo;

    /**
     * (Windows) Version number string for the version-information resource.
     * (Both {@code ProductVersion} and {@code FileVersion} resource strings are set to the same value.)
     * Must have {@code v1.v2.v3.v4} format where {@code vi} is a number.
     * If not set, {@code ${project.version}} is used. If the value does not meet the required format,
     * it is coerced. For instance, "1.2.3-SNAPSHOT" becomes "1.2.3.0"
     *
     * @see #version version
     */
    @Parameter(property = "winVIVersion", defaultValue = "${project.version}")
    protected String winVIVersion;

    /**
     * (Windows) Legal copyright notice string for the version-information resource.
     * By default, {@code "Copyright © {$project.inceptionYear},[curYear] [vendor]"} is used.
     */
    @Parameter(property = "winVICopyright")
    protected String winVICopyright;

    /**
     * (Windows) File description string for the version-information resource.
     */
    @Parameter(property = "winVIDescription", defaultValue = "${project.name}")
    protected String winVIDescription;

    /**
     * The license agreement file. Used for Excelsior Installer.
     * File containing the end-user license agreement, for Excelsior Installer to display during installation.
     * The file must be a plain text file either in US-ASCII or UTF-16LE encoding.
     * If not set, and the file {@code ${project.basedir}/src/main/jetresources/eula.txt} exists,
     * that file is used by convention.
     *
     * @see #eulaEncoding eulaEncoding
     */
    @Parameter(property = "eula", defaultValue = "${project.basedir}/src/main/jetresources/eula.txt")
    protected File eula;

    /**
     * Encoding of the EULA file. Permitted values:
     * <ul>
     *     <li>{@code US-ASCII}</li>
     *     <li>{@code UTF-16LE}</li>
     *     <li>{@code autodetect} (Default value)</li>
     * </ul>
     * If set to {@code autodetect}, the plugin looks for a byte order mark (BOM) in the file specified by {@link #eula}, and:
     * <ul>
     * <li>assumes US-ASCII encoding if no BOM is present,</li>
     * <li>assumes UTF-16LE encoding if the respective BOM ({@code 0xFF 0xFE}) is present, or </li>
     * <li>halts execution with error if some other BOM is present.</li>
     * </ul>
     * @see <a href="https://en.wikipedia.org/wiki/Byte_order_mark">Byte order mark</a>
     * @see #eula eula
     */
    @Parameter(property = "eulaEncoding", defaultValue = AUTO_DETECT_EULA_ENCODING)
    protected String eulaEncoding;

    /**
     * (Windows) Excelsior Installer splash screen image in BMP format.
     * If not set, and the file {@code ${project.basedir}/src/main/jetresources/installerSplash.bmp} exists,
     * that file is used by convention.
     */
    @Parameter(property = "installerSplash", defaultValue = "${project.basedir}/src/main/jetresources/installerSplash.bmp")
    protected File installerSplash;

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
                // no project name, get it from artifactId.
                product = project.getArtifactId();
            }
        }
        if (addWindowsVersionInfo) {
            //Coerce winVIVersion to v1.v2.v3.v4 format.
            String[] versions = winVIVersion.split("\\.");
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
            if (!winVIVersion.equals(finalVersion)) {
                getLog().warn(s("JetMojo.NotCompatibleExeVersion.Warning", winVIVersion, finalVersion));
                winVIVersion = finalVersion;
            }

            if (winVICopyright == null) {
                String inceptionYear = project.getInceptionYear();
                String curYear = new SimpleDateFormat("yyyy").format(new Date());
                String years = Utils.isEmpty(inceptionYear)? curYear : inceptionYear + "," + curYear;
                winVICopyright = "Copyright \\x00a9 " + years + " " + vendor;
            }
            if (winVIDescription == null) {
                winVIDescription = product;
            }
        }
    }

    private void checkGlobalAndSlimDownParameters(JetHome jetHome) throws JetHomeException, MojoFailureException {
        if (globalOptimizer) {
            if (jetHome.is64bit()) {
                getLog().warn(s("JetMojo.NoGlobalIn64Bit.Warning"));
                globalOptimizer = false;
            }
        }

        if ((javaRuntimeSlimDown != null) && !javaRuntimeSlimDown.isEnabled()) {
            javaRuntimeSlimDown = null;
        }

        if (javaRuntimeSlimDown != null) {
            if (jetHome.is64bit()) {
                getLog().warn(s("JetMojo.NoSlimDownIn64Bit.Warning"));
                javaRuntimeSlimDown = null;
            } else {
                if (javaRuntimeSlimDown.detachedBaseURL == null) {
                    throw new MojoFailureException(s("JetMojo.DetachedBaseURLMandatory.Failure"));
                }

                if (javaRuntimeSlimDown.detachedPackage == null) {
                    javaRuntimeSlimDown.detachedPackage = project.getBuild().getFinalName() + ".pkl";
                }

                globalOptimizer = true;
            }

        }

        if (globalOptimizer) {
            TestRunExecProfiles execProfiles = new TestRunExecProfiles(execProfilesDir, execProfilesName);
            if (!execProfiles.getUsg().exists()) {
                throw new MojoFailureException(s("JetMojo.NoTestRun.Failure"));
            }
        }
    }

    @Override
    protected JetHome checkPrerequisites() throws MojoFailureException {
        JetHome jetHomeObj = super.checkPrerequisites();

        //normalize main and set outputName
        mainClass = mainClass.replace('.', '/');
        if (outputName == null) {
            int lastSlash = mainClass.lastIndexOf('/');
            outputName = lastSlash < 0 ? mainClass : mainClass.substring(lastSlash + 1);
        }

        //check eula settings
        if (!VALID_EULA_ENCODING_VALUES.contains(eulaEncoding)) {
            throw new MojoFailureException(s("JetMojo.Package.Eula.UnsupportedEncoding", eulaEncoding));
        }


        //check packaging type
        switch (packaging) {
             case ZIP: case NONE: break;
             case EXCELSIOR_INSTALLER:
                 if (Utils.isOSX()) {
                     getLog().warn(s("JetMojo.NoExcelsiorInstallerOnOSX.Warning"));
                     packaging = ZIP;
                 }
                 break;
             default: throw new MojoFailureException(s("JetMojo.UnknownPackagingMode.Failure", packaging));
        }

        // check version info
        try {
            checkVersionInfo(jetHomeObj);

            if (multiApp && (jetHomeObj.getEdition() == JetEdition.STANDARD)) {
                getLog().warn(s("JetMojo.NoMultiappInStandard.Warning"));
                multiApp = false;
            }

            if (profileStartup) {
                if (jetHomeObj.getEdition() == JetEdition.STANDARD) {
                    getLog().warn(s("JetMojo.NoStartupAcceleratorInStandard.Warning"));
                    profileStartup = false;
                } else if (Utils.isOSX()) {
                    getLog().warn(s("JetMojo.NoStartupAcceleratorOnOSX.Warning"));
                    profileStartup = false;
                }
            }

            checkGlobalAndSlimDownParameters(jetHomeObj);

        } catch (JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        }

        return jetHomeObj;
    }

    private String createJetCompilerProject(File buildDir, ArrayList<String> compilerArgs, List<Dependency> dependencies, ArrayList<String> modules) throws MojoExecutionException {
        String prj = outputName + ".prj";
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File (buildDir, prj)))))
        {
            compilerArgs.forEach(out::println);
            for (Dependency dep: dependencies) {
                out.println("!classpathentry " + dep.dependency);
                out.println("  -optimize=" + (dep.isLib?"autodetect":"all"));
                out.println("  -protect=" + (dep.isLib?"nomatter":"all"));
                out.println("!end");
            }
            for(String mod: modules) {
                out.println("!module " + mod);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        return prj;
    }

    /**
     * Invokes the Excelsior JET AOT compiler.
     */
    private void compile(JetHome jetHome, File buildDir, List<Dependency> dependencies) throws MojoFailureException, CmdLineToolException, MojoExecutionException {
        ArrayList<String> compilerArgs = new ArrayList<>();
        ArrayList<String> modules = new ArrayList<>();
        if (Utils.isWindows()) {
            if (icon.isFile()) {
                modules.add(icon.getAbsolutePath());
            }
            if (hideConsole) {
                compilerArgs.add("-gui+");
            }
        }
        compilerArgs.add("-main=" + mainClass);
        compilerArgs.add("-outputname=" + outputName);
        compilerArgs.add("-decor=ht");

        if (profileStartup) {
            compilerArgs.add("-saprofmode=ALWAYS");
            compilerArgs.add("-saproftimeout=" + profileStartupTimeout);
        }

        if (addWindowsVersionInfo) {
            compilerArgs.add("-versioninfocompanyname=" + vendor);
            compilerArgs.add("-versioninfoproductname=" + product);
            compilerArgs.add("-versioninfoproductversion=" + winVIVersion);
            compilerArgs.add("-versioninfolegalcopyright=" + winVICopyright);
            compilerArgs.add("-versioninfofiledescription=" + winVIDescription);
        }

        if (multiApp) {
            compilerArgs.add("-multiapp+");
        }

        if (globalOptimizer) {
            compilerArgs.add("-global+");
        }

        TestRunExecProfiles execProfiles = new TestRunExecProfiles(execProfilesDir, execProfilesName);
        if (execProfiles.getStartup().exists()) {
            compilerArgs.add("-startupprofile=" + execProfiles.getStartup().getAbsolutePath());
        }
        if (execProfiles.getUsg().exists()) {
            modules.add(execProfiles.getUsg().getAbsolutePath());
        }

        String prj = createJetCompilerProject(buildDir, compilerArgs, dependencies, modules);

        if (new JetCompiler(jetHome, "=p", prj)
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Build.Failure"));
        }
    }

    private ArrayList<String> getCommonXPackArgs() {
        ArrayList<String> xpackArgs = new ArrayList<>();

        xpackArgs.addAll(Arrays.asList(
            "-add-file", Utils.mangleExeName(outputName), "/"
        ));

        if (optRtFiles != null && optRtFiles.length > 0) {
            xpackArgs.add("-add-opt-rt-files");
            xpackArgs.add(String.join(",", optRtFiles));
        }

        if (javaRuntimeSlimDown != null) {

            xpackArgs.addAll(Arrays.asList(
                "-detached-base-url", javaRuntimeSlimDown.detachedBaseURL,
                "-detach-components",
                  (javaRuntimeSlimDown.detachComponents != null && javaRuntimeSlimDown.detachComponents.length > 0)?
                          String.join(",", javaRuntimeSlimDown.detachComponents) : "auto",
                "-detached-package", new File(jetOutputDir, javaRuntimeSlimDown.detachedPackage).getAbsolutePath()
            ));
        }

        return xpackArgs;
    }

    /**
     * Packages the generated executable and required Excelsior JET runtime files
     * as a self-contained directory
     */
    private void createAppDir(JetHome jetHome, File buildDir, File appDir) throws CmdLineToolException, MojoFailureException {
        ArrayList<String> xpackArgs = getCommonXPackArgs();
        xpackArgs.addAll(Arrays.asList(
            "-target", appDir.getAbsolutePath()
        ));
        if (new JetPackager(jetHome, xpackArgs.toArray(new String[xpackArgs.size()]))
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
        ArrayList<String> xpackArgs = getCommonXPackArgs();
        if (eula.exists()) {
            xpackArgs.add(eulaFlag());
            xpackArgs.add(eula.getAbsolutePath());
        }
        if (Utils.isWindows() && installerSplash.exists()) {
            xpackArgs.add("-splash"); xpackArgs.add(installerSplash.getAbsolutePath());
        }
        xpackArgs.addAll(Arrays.asList(
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

    private String eulaFlag() throws MojoFailureException {
        String detectedEncoding;
        try {
            detectedEncoding = detectEncoding(eula);
        } catch (IOException e) {
            throw new MojoFailureException(s("JetMojo.Package.Eula.UnableToDetectEncoding", eula.getAbsolutePath()), e);
        }

        if (!AUTO_DETECT_EULA_ENCODING.equals(eulaEncoding)) {
            if (!detectedEncoding.equals(eulaEncoding)) {
                throw new MojoFailureException(s("JetMojo.Package.Eula.EncodingDoesNotMatchActual", detectedEncoding, eulaEncoding));
            }
        }

        String actualEncoding = AUTO_DETECT_EULA_ENCODING.equals(eulaEncoding) ?
                detectedEncoding :
                eulaEncoding;

        if (StandardCharsets.UTF_16LE.name().equals(actualEncoding)) {
            return UNICODE_EULA_FLAG;
        } else if (StandardCharsets.US_ASCII.name().equals(actualEncoding)) {
            return EULA_FLAG;
        } else {
            throw new MojoFailureException(s("JetMojo.Package.Eula.UnsupportedEncoding", eulaEncoding));
        }
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

        if (javaRuntimeSlimDown != null) {
            getLog().info(s("JetMojo.SlimDown.Info", new File(jetOutputDir, javaRuntimeSlimDown.detachedPackage),
                    javaRuntimeSlimDown.detachedBaseURL));
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        JetHome jetHome = checkPrerequisites();

        // creating output dirs
        File buildDir = createBuildDir();

        File appDir = new File(jetOutputDir, APP_DIR);
        //cleanup packageDir
        try {
            Utils.cleanDirectory(appDir);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        List<Dependency> dependencies = copyDependencies(buildDir, mainJar);

        try {
            compile(jetHome, buildDir, dependencies);

            createAppDir(jetHome, buildDir, appDir);

            packageBuild(jetHome, buildDir, appDir);

        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoExecutionException(s("JetMojo.Unexpected.Error"), e);
        }
    }
}
