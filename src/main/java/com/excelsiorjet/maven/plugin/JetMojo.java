/*
 * Copyright (c) 2015,2016 Excelsior LLC.
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.excelsiorjet.Txt.s;

/**
 *  Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo( name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JetMojo extends AbstractJetMojo {

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
     * (32-bit only) If set to {@code true}, the Global Optimizer is enabled,
     * providing higher performance and lower memory usage for the compiled application.
     * Performing a Test Run is mandatory when the Global Optimizer is enabled.
     * The Global Optimizer is enabled automatically when you enable Java Runtime Slim-Down.
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
     * (it mimicks the command line syntax of the conventional {@code java} launcher).
     */
    @Parameter(property = "multiApp", defaultValue = "false")
    protected boolean multiApp;

    /**
     * If set to {@code true}, enables protection of application data - reflection information,
     * string literals, and resource files packed into the executable, if any.
     *
     * @see #cryptSeed
     */
    @Parameter(property = "protectData")
    protected boolean protectData;

    /**
     * Sets a seed string that will be used by the Excelsior JET compiler to generate a key for 
     * scrambling the data that the executable contains.
     * If data protection is enabled, but {@code cryptSeed} is not set explicitly, a random value is used.
     * <p>
     * You may want to set a {@code cryptSeed} value if you need the data to be protected in a stable way.
     * </p>
     * 
     * @see #protectData
     */
    @Parameter(property = "cryptSeed")
    protected String cryptSeed;
    
    /**
     * Enable/disable startup accelerator.
     * If enabled, the compiled application will run after build
     * for {@link #profileStartupTimeout} seconds for collecting a startup profile.
     */
    @Parameter(property = "profileStartup", defaultValue = "true")
    protected boolean profileStartup;

    /**
     * The duration of the after-build profiling session in seconds. Upon exhaustion,
     * the application will be automatically terminated.
     */
    @Parameter(property = "profileStartupTimeout", defaultValue = "20")
    protected int profileStartupTimeout;

    /**
     * Trial version configuration parameters.
     *
     * @see TrialVersionConfig#expireInDays
     * @see TrialVersionConfig#expireDate
     * @see TrialVersionConfig#expireMessage
     */
    @Parameter(property = "trialVersion")
    TrialVersionConfig trialVersion;

    /**
     * Add optional JET Runtime components to the package. Available optional components:
     * {@code runtime_utilities}, {@code fonts}, {@code awt_natives}, {@code api_classes}, {@code jce},
     * {@code accessibility}, {@code javafx}, {@code javafx-webkit}, {@code nashorn}, {@code cldr}
     */
    @Parameter(property = "optRtFiles")
    protected String[] optRtFiles;

    //packaging types
    private static final String ZIP = "zip";
    private static final String NONE = "none";
    private static final String EXCELSIOR_INSTALLER = "excelsior-installer";
    private static final String OSX_APP_BUNDLE = "osx-app-bundle";
    private static final String NATIVE_BUNDLE = "native-bundle";

    /**
     * Application packaging mode. Permitted values are:
     * <dl>
     *   <dt>zip</dt>
     *   <dd>zip archive with a self-contained application package (default)</dd>
     *   <dt>excelsior-installer</dt>
     *   <dd>self-extracting installer with standard GUI for Windows
     *     and command-line interface for Linux</dd>
     *   <dt>osx-app-bundle</dt>
     *   <dd>OS X application bundle</dd>
     *   <dt>native-bundle</dt>
     *   <dd>Excelsior Installer setups for Windows and Linux, application bundle for OS X</dd>
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
     * Excelsior Installer configuration parameters.
     *
     * @see ExcelsiorInstallerConfig#eula
     * @see ExcelsiorInstallerConfig#eulaEncoding
     * @see ExcelsiorInstallerConfig#installerSplash
     */
    @Parameter(property = "excelsiorInstallerConfiguration")
    protected ExcelsiorInstallerConfig excelsiorInstallerConfiguration;

    /**
     * OS X Application Bundle configuration parameters.
     *
     * @see OSXAppBundleConfig#fileName
     * @see OSXAppBundleConfig#bundleName
     * @see OSXAppBundleConfig#identifier
     * @see OSXAppBundleConfig#shortVersion
     * @see OSXAppBundleConfig#icon
     * @see OSXAppBundleConfig#developerId
     * @see OSXAppBundleConfig#publisherId
     */
    @Parameter(property = "osxBundleConfiguration")
    protected OSXAppBundleConfig osxBundleConfiguration;

    private static final String APP_DIR = "app";

    private void checkVersionInfo(JetHome jetHome) throws JetHomeException {
        if (!Utils.isWindows()) {
            addWindowsVersionInfo = false;
        }
        if (addWindowsVersionInfo && (jetHome.getEdition() == JetEdition.STANDARD)) {
            getLog().warn(s("JetMojo.NoVersionInfoInStandard.Warning"));
            addWindowsVersionInfo = false;
        }
        if (addWindowsVersionInfo || EXCELSIOR_INSTALLER.equals(packaging) || OSX_APP_BUNDLE.equals(packaging)) {
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
            String finalVersion = deriveFourDigitVersion(winVIVersion);
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

    private String deriveFourDigitVersion(String version) {
        String[] versions = version.split("\\.");
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
        return String.join(".", finalVersions);
    }

    private void checkGlobalAndSlimDownParameters(JetHome jetHome) throws JetHomeException, MojoFailureException {
        if (globalOptimizer) {
            if (jetHome.is64bit()) {
                getLog().warn(s("JetMojo.NoGlobalIn64Bit.Warning"));
                globalOptimizer = false;
            } else if (jetHome.getEdition() == JetEdition.STANDARD) {
                getLog().warn(s("JetMojo.NoGlobalInStandard.Warning"));
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
            } else if (jetHome.getEdition() == JetEdition.STANDARD) {
                getLog().warn(s("JetMojo.NoSlimDownInStandard.Warning"));
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

    private void checkTrialVersionConfig(JetHome jetHome) throws MojoFailureException, JetHomeException {
        if ((trialVersion != null) && trialVersion.isEnabled()) {
            if ((trialVersion.expireInDays >= 0) && (trialVersion.expireDate != null)) {
                throw new MojoFailureException(s("JetMojo.AmbiguousExpireSetting.Failure"));
            }
            if (trialVersion.expireMessage == null || trialVersion.expireMessage.isEmpty()) {
                throw new MojoFailureException(s("JetMojo.NoExpireMessage.Failure"));
            }

            if (jetHome.getEdition() == JetEdition.STANDARD) {
                getLog().warn(s("JetMojo.NoTrialsInStandard.Warning"));
                trialVersion = null;
            }
        } else {
            trialVersion = null;
        }
    }

    private void checkExcelsiorInstallerConfig() throws MojoFailureException {
        if (packaging.equals(EXCELSIOR_INSTALLER)) {
            excelsiorInstallerConfiguration.fillDefaults(project);
        }
    }

    private void checkOSXBundleConfig() {
        if (packaging.equals(OSX_APP_BUNDLE)) {
            String fourDigitVersion = deriveFourDigitVersion(version);
            osxBundleConfiguration.fillDefaults(project, outputName, product,
                    deriveFourDigitVersion(project.getVersion()),
                    deriveFourDigitVersion(fourDigitVersion.substring(0, fourDigitVersion.lastIndexOf('.'))));
            if (!osxBundleConfiguration.icon.exists()) {
                getLog().warn(s("JetMojo.NoIconForOSXAppBundle.Warning"));
            }
        }

    }

    @Override
    protected JetHome checkPrerequisites() throws MojoFailureException {
        JetHome jetHomeObj = super.checkPrerequisites();

        switch (appType) {
            case PLAIN:
                //normalize main and set outputName
                mainClass = mainClass.replace('.', '/');
                if (outputName == null) {
                    int lastSlash = mainClass.lastIndexOf('/');
                    outputName = lastSlash < 0 ? mainClass : mainClass.substring(lastSlash + 1);
                }
                break;
            case TOMCAT:
                if (outputName == null) {
                    outputName = project.getArtifactId();
                }
                break;
            default:
                throw new AssertionError("Unknown application type");
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
            case OSX_APP_BUNDLE:
                if (!Utils.isOSX()) {
                    getLog().warn(s("JetMojo.OSXBundleOnNotOSX.Warning"));
                    packaging = ZIP;
                }
                break;

            case NATIVE_BUNDLE:
                if (Utils.isOSX()) {
                    packaging = OSX_APP_BUNDLE;
                } else {
                    packaging = EXCELSIOR_INSTALLER;
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

            if (protectData) {
                if (jetHomeObj.getEdition() == JetEdition.STANDARD) {
                    throw new MojoFailureException(s("JetMojo.NoDataProtectionInStandard.Failure"));
                } else {
                    if (cryptSeed == null) {
                        cryptSeed = RandomStringUtils.randomAlphanumeric(64);
                    }
                }
            }

            checkTrialVersionConfig(jetHomeObj);

            checkGlobalAndSlimDownParameters(jetHomeObj);

            checkExcelsiorInstallerConfig();

            checkOSXBundleConfig();

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

        switch (appType) {
            case PLAIN:
                compilerArgs.add("-main=" + mainClass);
                break;
            case TOMCAT:
                compilerArgs.add("-apptype=tomcat");
                compilerArgs.add("-appdir=" + getTomcatInBuildDir());
                if (tomcatConfiguration.hideConfig) {
                    compilerArgs.add("-hideconfiguration+");
                }
                if (!tomcatConfiguration.genScripts) {
                    compilerArgs.add("-gentomcatscripts-");
                }
                break;
            default: throw new AssertionError("Unknown app type");
        }


        if (Utils.isWindows()) {
            if (icon.isFile()) {
                modules.add(icon.getAbsolutePath());
            }
            if (hideConsole) {
                compilerArgs.add("-gui+");
            }
        }

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

        if (trialVersion != null) {
            compilerArgs.add("-expire=" + trialVersion.getExpire());
            compilerArgs.add("-expiremsg=" + trialVersion.expireMessage);
        }

        if (protectData) {
            compilerArgs.add("-cryptseed=" + cryptSeed);
        }

        TestRunExecProfiles execProfiles = new TestRunExecProfiles(execProfilesDir, execProfilesName);
        if (execProfiles.getStartup().exists()) {
            compilerArgs.add("-startupprofile=" + execProfiles.getStartup().getAbsolutePath());
        }
        if (execProfiles.getUsg().exists()) {
            modules.add(execProfiles.getUsg().getAbsolutePath());
        }

        String jetVMPropOpt = "-jetvmprop=";
        if (jvmArgs != null && jvmArgs.length > 0) {
            jetVMPropOpt = jetVMPropOpt + String.join(" ", jvmArgs);

            // JVM args may contain $(Root) prefix for system property value
            // (that should expand to installation directory location).
            // However JET compiler replaces such occurrences with s value of "Root" equation if the "$(Root)" is
            // used in the project file.
            // So we need to pass jetvmprop as separate compiler argument as workaround.
            // We also write the equation in commented form to the project in order to see it in the technical support.
            compilerArgs.add("%"+jetVMPropOpt);
        }

        String prj = createJetCompilerProject(buildDir, compilerArgs, dependencies, modules);

        if (new JetCompiler(jetHome, "=p", prj, jetVMPropOpt)
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Build.Failure"));
        }
    }

    private ArrayList<String> getCommonXPackArgs() {
        ArrayList<String> xpackArgs = new ArrayList<>();

        switch (appType) {
            case PLAIN:
                if (packageFilesDir.exists()) {
                    xpackArgs.add("-source");
                    xpackArgs.add(packageFilesDir.getAbsolutePath());
                }

                xpackArgs.addAll(Arrays.asList(
                        "-add-file", Utils.mangleExeName(outputName), "/"
                ));
                break;
            case TOMCAT:
                xpackArgs.add("-source");
                xpackArgs.add(getTomcatInBuildDir().getAbsolutePath());
                if (packageFilesDir.exists()) {
                    getLog().warn(s("JetMojo.PackageFilesIgnoredForTomcat.Warning"));
                }
                break;
            default: throw new AssertionError("Unknown app type");
        }

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
    private void packWithEI(JetHome jetHome, File buildDir) throws CmdLineToolException, MojoFailureException {
        File target = new File(jetOutputDir, Utils.mangleExeName(project.getBuild().getFinalName()));
        ArrayList<String> xpackArgs = getCommonXPackArgs();
        if (excelsiorInstallerConfiguration.eula.exists()) {
            xpackArgs.add(excelsiorInstallerConfiguration.eulaFlag());
            xpackArgs.add(excelsiorInstallerConfiguration.eula.getAbsolutePath());
        }
        if (Utils.isWindows() && excelsiorInstallerConfiguration.installerSplash.exists()) {
            xpackArgs.add("-splash"); xpackArgs.add(excelsiorInstallerConfiguration.installerSplash.getAbsolutePath());
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
        getLog().info(s("JetMojo.Build.Success"));
        getLog().info(s("JetMojo.GetEI.Info", target.getAbsolutePath()));
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

    private void createOSXAppBundle(JetHome jetHome, File buildDir) throws MojoExecutionException, MojoFailureException, CmdLineToolException {
        File appBundle = new File(jetOutputDir, osxBundleConfiguration.fileName + ".app");
        mkdir(appBundle);
        try {
            Utils.cleanDirectory(appBundle);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
        File contents = new File (appBundle, "Contents");
        mkdir(contents);
        File contentsMacOs = new File(contents, "MacOS");
        mkdir(contentsMacOs);
        File contentsResources = new File (contents, "Resources");
        mkdir(contentsResources);

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(new File (contents, "Info.plist")), "UTF-8")))
        {
            out.print (
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "  <key>CFBundlePackageType</key>\n" +
                    "  <string>APPL</string>\n" +
                    "  <key>CFBundleExecutable</key>\n" +
                    "  <string>" + outputName + "</string>\n" +
                    "  <key>CFBundleName</key>\n" +
                    "  <string>" + osxBundleConfiguration.bundleName + "</string>\n" +
                    "  <key>CFBundleIdentifier</key>\n" +
                    "  <string>" + osxBundleConfiguration.identifier +"</string>\n" +
                    "  <key>CFBundleVersionString</key>\n" +
                    "  <string>"+ osxBundleConfiguration.version + "</string>\n" +
                    "  <key>CFBundleShortVersionString</key>\n" +
                    "  <string>"+ osxBundleConfiguration.shortVersion + "</string>\n" +
                    (osxBundleConfiguration.icon.exists()?
                            "  <key>CFBundleIconFile</key>\n" +
                            "  <string>" + osxBundleConfiguration.icon.getName() + "</string>\n" : "") +
                    (osxBundleConfiguration.highResolutionCapable?
                            "  <key>NSHighResolutionCapable</key>\n" +
                            "  <true/>" : "") +
                    "</dict>\n" +
                    "</plist>\n");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        ArrayList<String> xpackArgs = getCommonXPackArgs();
        xpackArgs.addAll(Arrays.asList(
            "-target", contentsMacOs.getAbsolutePath()
        ));
        if (new JetPackager(jetHome, xpackArgs.toArray(new String[xpackArgs.size()]))
                .workingDirectory(buildDir).withLog(getLog()).execute() != 0) {
            throw new MojoFailureException(s("JetMojo.Package.Failure"));
        }

        if (osxBundleConfiguration.icon.exists()) {
            try {
                Files.copy(osxBundleConfiguration.icon.toPath(),
                        new File(contentsResources, osxBundleConfiguration.icon.getName()).toPath());
            } catch (IOException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }
        }

        File appPkg = null;
        if (osxBundleConfiguration.developerId != null) {
            getLog().info(s("JetMojo.SigningOSXBundle.Info"));
            if (new CmdLineTool("codesign", "--verbose", "--force", "--deep", "--sign",
                    osxBundleConfiguration.developerId, appBundle.getAbsolutePath()).withLog(getLog(), true).execute() != 0) {
                throw new MojoFailureException(s("JetMojo.OSX.CodeSign.Failure"));
            }
            getLog().info(s("JetMojo.CreatingOSXInstaller.Info"));
            if (osxBundleConfiguration.publisherId != null) {
                appPkg = new File(jetOutputDir, project.getBuild().getFinalName() + ".pkg");
                if (new CmdLineTool("productbuild", "--sign", osxBundleConfiguration.publisherId,
                             "--component", appBundle.getAbsolutePath(), osxBundleConfiguration.installPath,
                                            appPkg.getAbsolutePath())
                        .withLog(getLog()).execute() != 0) {
                    throw new MojoFailureException(s("JetMojo.OSX.Packaging.Failure"));
                }
            } else {
                getLog().warn(s("JetMojo.NoPublisherId.Warning"));
            }
        } else {
            getLog().warn(s("JetMojo.NoDeveloperId.Warning"));
        }
        getLog().info(s("JetMojo.Build.Success"));
        if (appPkg != null) {
            getLog().info(s("JetMojo.GetOSXPackage.Info", appPkg.getAbsolutePath()));
        } else {
            getLog().info(s("JetMojo.GetOSXBundle.Info", appBundle.getAbsolutePath()));
        }

    }

    private void packageBuild(JetHome jetHome, File buildDir, File packageDir) throws IOException, MojoFailureException, CmdLineToolException, MojoExecutionException {
        switch (packaging){
            case ZIP:
                getLog().info(s("JetMojo.ZipApp.Info"));
                File targetZip = new File(jetOutputDir, project.getBuild().getFinalName() + ".zip");
                compressZipfile(packageDir, targetZip);
                getLog().info(s("JetMojo.Build.Success"));
                getLog().info(s("JetMojo.GetZip.Info", targetZip.getAbsolutePath()));
                break;
            case EXCELSIOR_INSTALLER:
                packWithEI(jetHome, buildDir);
                break;
            case OSX_APP_BUNDLE:
                createOSXAppBundle(jetHome, buildDir);
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

        try {
            if (appType == ApplicationType.PLAIN) {
                compile(jetHome, buildDir, copyDependencies(buildDir, mainJar));
            } else {
                copyTomcatAndWar();
                compile(jetHome, buildDir, Collections.emptyList());
            }

            createAppDir(jetHome, buildDir, appDir);

            packageBuild(jetHome, buildDir, appDir);

        } catch (Exception e) {
            getLog().error(e.getMessage());
            throw new MojoExecutionException(s("JetMojo.Unexpected.Error"), e);
        }
    }

}
