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

import com.excelsiorjet.api.log.AbstractLog;
import com.excelsiorjet.api.tasks.*;
import com.excelsiorjet.api.tasks.config.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;

/**
 * Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JetMojo extends AbstractJetMojo implements JetTaskConfig {

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

    /**
     * Application packaging mode. Permitted values are:
     * <dl>
     * <dt>zip</dt>
     * <dd>zip archive with a self-contained application package (default)</dd>
     * <dt>excelsior-installer</dt>
     * <dd>self-extracting installer with standard GUI for Windows
     * and command-line interface for Linux</dd>
     * <dt>osx-app-bundle</dt>
     * <dd>OS X application bundle</dd>
     * <dt>native-bundle</dt>
     * <dd>Excelsior Installer setups for Windows and Linux, application bundle for OS X</dd>
     * <dt>none</dt>
     * <dd>skip packaging altogether</dd>
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


    @Override
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            AbstractLog.setInstance(new MavenLog(getLog()));
            new JetTask(this).execute();
        } catch (ExcelsiorJetApiException e) {
            throw new MojoFailureException("JetMojo failure exception", e);
        }
    }

    @Override
    public void setAddWindowsVersionInfo(boolean addWindowsVersionInfoFlag) {
        this.addWindowsVersionInfo = addWindowsVersionInfoFlag;
    }

    @Override
    public boolean isAddWindowsVersionInfo() {
        return addWindowsVersionInfo;
    }

    @Override
    public String excelsiorJetPackaging() {
        return packaging;
    }

    @Override
    public void setExcelsiorJetPackaging(String excelsiorJetPackaging) {
        packaging = excelsiorJetPackaging;
    }

    @Override
    public String vendor() {
        return vendor;
    }

    @Override
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public String product() {
        return product;
    }

    @Override
    public String artifactId() {
        return project.getArtifactId();
    }

    @Override
    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String winVIVersion() {
        return winVIVersion;
    }

    @Override
    public void setWinVIVersion(String winVIVersion) {
        this.winVIVersion = winVIVersion;
    }

    @Override
    public String winVICopyright() {
        return winVICopyright;
    }

    @Override
    public void setWinVICopyright(String winVICopyright) {
        this.winVICopyright = winVICopyright;
    }

    @Override
    public String inceptionYear() {
        return project.getInceptionYear();
    }

    @Override
    public String winVIDescription() {
        return winVIDescription;
    }

    @Override
    public void setWinVIDescription(String winVIDescription) {
        this.winVIDescription = winVIDescription;
    }

    @Override
    public boolean globalOptimizer() {
        return globalOptimizer;
    }

    @Override
    public void setGlobalOptimizer(boolean globalOptimizer) {
        this.globalOptimizer = globalOptimizer;
    }

    @Override
    public SlimDownConfig javaRuntimeSlimDown() {
        return javaRuntimeSlimDown;
    }

    @Override
    public void setJavaRuntimeSlimDown(SlimDownConfig slimDownConfig) {
        this.javaRuntimeSlimDown = slimDownConfig;
    }

    @Override
    public TrialVersionConfig trialVersion() {
        return trialVersion;
    }

    @Override
    public void setTrialVersion(TrialVersionConfig trialVersionConfig) {
        this.trialVersion = trialVersionConfig;
    }

    @Override
    public ExcelsiorInstallerConfig excelsiorInstallerConfiguration() {
        return excelsiorInstallerConfiguration;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public OSXAppBundleConfig osxBundleConfiguration() {
        return osxBundleConfiguration;
    }

    @Override
    public String outputName() {
        return outputName;
    }

    @Override
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    @Override
    public boolean multiApp() {
        return multiApp;
    }

    @Override
    public void setMultiApp(boolean multiApp) {
        this.multiApp = multiApp;
    }

    @Override
    public boolean profileStartup() {
        return profileStartup;
    }

    @Override
    public void setProfileStartup(boolean profileStartup) {
        this.profileStartup = profileStartup;
    }

    @Override
    public boolean protectData() {
        return protectData;
    }

    @Override
    public String cryptSeed() {
        return cryptSeed;
    }

    @Override
    public void setCryptSeed(String cryptSeed) {
        this.cryptSeed = cryptSeed;
    }

    @Override
    public File icon() {
        return icon;
    }

    @Override
    public boolean hideConsole() {
        return hideConsole;
    }

    @Override
    public int profileStartupTimeout() {
        return profileStartupTimeout;
    }

    @Override
    public String[] optRtFiles() {
        return optRtFiles;
    }

    @Override
    public File jetOutputDir() {
        return jetOutputDir;
    }

}
