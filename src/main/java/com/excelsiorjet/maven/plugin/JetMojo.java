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

import com.excelsiorjet.api.ExcelsiorJet;
import com.excelsiorjet.api.cmd.CmdLineToolException;
import com.excelsiorjet.api.JetHomeException;
import com.excelsiorjet.api.tasks.JetBuildTask;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.tasks.JetTaskFailureException;
import com.excelsiorjet.api.tasks.config.*;
import com.excelsiorjet.api.util.Txt;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.io.IOException;

import static com.excelsiorjet.api.log.Log.logger;
import static com.excelsiorjet.api.util.Txt.*;

/**
 * Main Mojo for building Java (JVM) applications with Excelsior JET.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JetMojo extends AbstractJetMojo {

    /**
     * Target executable name. If not set, the main class name is used.
     */
    @Parameter(property = "outputName")
    protected String outputName;

    /**
     * (Windows) .ico file to associate with the resulting executable file.
     *
     * Default value is "icon.ico" of {@link #jetResourcesDir} directory.
     */
    @Parameter(property = "icon")
    protected File icon;

    /**
     * Splash image to display upon application start up.
     *
     * By default, the file "splash.png" from the {@link #jetResourcesDir} folder is used.
     * If it does not exist, but a splash image is specified in the manifest
     * of the application JAR file, that image will be used automatically.
     */
    @Parameter(property = "splash")
    private File splash;

    /**
     * The JET Runtime supports three modes of stack trace printing: {@code minimal}, {@code full}, and {@code none}.
     * <p>
     * In the {@code minimal} mode (default), line numbers and names of some methods are omitted in call stack entries,
     * but the class names are exact.
     * </p>
     * <p>
     * In the {@code full} mode, the stack trace info includes all line numbers and method names.
     * However, enabling the full stack trace has a side effect - substantial growth of the resulting
     * executable size, approximately by 30%.
     * </p>
     * <p>
     * In the {@code none} mode, Throwable.printStackTrace() methods print a few fake elements.
     * It may result in performance improvement if the application throws and catches exceptions repeatedly.
     * Note, however, that some third-party APIs may rely on stack trace printing. One example
     * is the Log4J API that provides logging services.
     * </p>
     */
    @Parameter(property = "stackTraceSupport")
    private String stackTraceSupport;

    /**
     * Controls the aggressiveness of method inlining.
     * Available values are:
     *   {@code aggressive} (default), {@code very-aggressive}, {@code medium}, {@code low}, {@code tiny-methods-only}.
     * <p>
     * If you need to reduce the size of the executable,
     * set the {@code low} or {@code tiny-methods-only} option. Note that it does not necessarily worsen application performance.
     * </p>
     */
    @Parameter(property = "inlineExpansion")
    private String inlineExpansion;

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
     * (32-bit only)
     * Reduce the disk footprint of the application by including the supposedly unused Java SE API
     * classes in the resulting package in a compressed form.
     * Valid values are: {@code none},  {@code medium} (default),  {@code high-memory},  {@code high-disk}.
     * <p>
     * The feature is only available if {@link #globalOptimizer} is enabled.
     * In this mode, the Java SE classes that were not compiled into the resulting executable are placed
     * into the resulting package in bytecode form, possibly compressed depending on the mode:
     * </p>
     * <dl>
     * <dt>none</dt>
     * <dd>Disable compression</dd>
     * <dt>medium</dt>
     * <dd>Use a simple compression algorithm that has minimal run time overheads and permits
     * selective decompression.</dd>
     * <dt>high-memory</dt>
     * <dd>Compress all unused Java SE API classes as a whole. This results in more significant disk
     * footprint reduction compared to than medium compression. However, if one of the compressed classes
     * is needed at run time, the entire bundle must be decompressed to retrieve it.
     * In the {@code high-memory} reduction mode the bundle is decompressed 
     * onto the heap and can be garbage collected later.</dd>
     * <dt>high-disk</dt>
     * <dd>Same as {@code high-memory}, but decompress to the temp directory.</dd>
     * </dl>
     */
    @Parameter(property = "diskFootprintReduction")
    private String diskFootprintReduction;

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
     * Java SE 8 defines three subsets of the standard Platform API called compact profiles.
     * Excelsior JET enables you to deploy your application with one of those subsets.
     * You may set this parameter to specify a particular profile.
     * Valid values are: {@code auto} (default),  {@code compact1},  {@code compact2},  {@code compact3}, {@code full}
     *  <p>
     * {@code auto} value (default) forces Excelsior JET to detect which parts of the Java SE Platform API
     * are referenced by the application and select the smallest compact profile that includes them all,
     * or the entire Platform API if there is no such profile.
     * </p>
     */
    @Parameter(property = "profile", defaultValue = "auto")
    private String profile;

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
     * Add optional JET Runtime components to the package.
     * By default, only the {@code jce} component (Java Crypto Extension) is added.
     * You may pass a special value {@code all} to include all available optional components at once
     * or {@code none} to not include any of them.
     * Available optional components:
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
    @Parameter(property = "packaging")
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
     * use the {@link #windowsVersionInfoConfiguration} Mojo parameter.
     */
    @Parameter(property = "version", defaultValue = "${project.version}")
    protected String version;

    /**
     * (Windows) If set to {@code true}, a version-information resource will be added to the final executable.
     *
     * @see #windowsVersionInfoConfiguration
     * @see WindowsVersionInfoConfig#company
     * @see WindowsVersionInfoConfig#product
     * @see WindowsVersionInfoConfig#version
     * @see WindowsVersionInfoConfig#copyright
     * @see WindowsVersionInfoConfig#description
     */
    @Parameter(property = "addWindowsVersionInfo", defaultValue = "true")
    protected boolean addWindowsVersionInfo;

    /**
     * Windows version-information resource description.
     */
    @Parameter(property = "windowsVersionInfoConfiguration")
    protected WindowsVersionInfoConfig windowsVersionInfoConfiguration;

    /**
     * Deprecated. Use {@link #windowsVersionInfoConfiguration} parameter instead.
     */
    @Deprecated
    @Parameter(property = "winVIVersion")
    protected String winVIVersion;

    /**
     * Deprecated. Use {@link #windowsVersionInfoConfiguration} parameter instead.
     */
    @Deprecated
    @Parameter(property = "winVICopyright")
    protected String winVICopyright;

    /**
     * Deprecated. Use {@link #windowsVersionInfoConfiguration} parameter instead.
     */
    @Deprecated
    @Parameter(property = "winVIDescription")
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
     * Windows Service configuration parameters.
     *
     * @see WindowsServiceConfig#name
     * @see WindowsServiceConfig#displayName
     * @see WindowsServiceConfig#description
     * @see WindowsServiceConfig#arguments
     * @see WindowsServiceConfig#logOnType
     * @see WindowsServiceConfig#allowDesktopInteraction
     * @see WindowsServiceConfig#startupType
     * @see WindowsServiceConfig#startServiceAfterInstall
     * @see WindowsServiceConfig#dependencies
     */
    @Parameter(property = "windowsServiceConfiguration")
    protected WindowsServiceConfig windowsServiceConfiguration;

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

    /**
     * Add locales and charsets.
     * By default only {@code European} locales are added.
     * You may pass a special value {@code all} to include all available locales at once
     * or {@code none} to not include any additional locales.
     * Available locales and charsets:
     *    {@code European}, {@code Indonesian}, {@code Malay}, {@code Hebrew}, {@code Arabic},
     *    {@code Chinese}, {@code Japanese}, {@code Korean}, {@code Thai}, {@code Vietnamese}, {@code Hindi},
     *    {@code Extended_Chinese}, {@code Extended_Japanese}, {@code Extended_Korean}, {@code Extended_Thai},
     *    {@code Extended_IBM}, {@code Extended_Macintosh}, {@code Latin_3}
     */
    @Parameter(property = "locales")
    private String[] locales;

    /**
     * Additional compiler options and equations.
     * The commonly used compiler options and equations are mapped to the respective project parameters,
     * so usually there is no need to specify them with this parameter.
     * However, the compiler also has some advanced options and equations 
     * that you may find in the Excelsior JET User's Guide, plus some troubleshooting settings
     * that the Excelsior JET Support team may suggest to you.
     * You may enumerate such options and equations with this parameter and they will be appended to the
     * Excelsior JET project generated by {@link JetBuildTask}.
     * <p>
     * Care must be taken when using this parameter to avoid conflicts with other project parameters.
     * </p>
     */
    @Parameter(property = "compilerOptions")
    private String[] compilerOptions;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            JetProject jetProject = getJetProject()
                    .addWindowsVersionInfo(addWindowsVersionInfo)
                    .excelsiorJetPackaging(packaging)
                    .vendor(vendor)
                    .product(product)
                    .windowsVersionInfoConfiguration(windowsVersionInfoConfiguration)
                    .inceptionYear(project.getInceptionYear())
                    .globalOptimizer(globalOptimizer)
                    .javaRuntimeSlimDown(javaRuntimeSlimDown)
                    .trialVersion(trialVersion)
                    .excelsiorInstallerConfiguration(excelsiorInstallerConfiguration)
                    .windowsServiceConfiguration(windowsServiceConfiguration)
                    .version(version)
                    .osxBundleConfiguration(osxBundleConfiguration)
                    .outputName(outputName)
                    .multiApp(multiApp)
                    .profileStartup(profileStartup)
                    .protectData(protectData)
                    .cryptSeed(cryptSeed)
                    .icon(icon)
                    .splash(splash)
                    .stackTraceSupport(stackTraceSupport)
                    .inlineExpansion(inlineExpansion)
                    .hideConsole(hideConsole)
                    .profileStartupTimeout(profileStartupTimeout)
                    .compilerOptions(compilerOptions)
                    .locales(locales)
                    .optRtFiles(optRtFiles)
                    .compactProfile(profile)
                    .diskFootprintReduction(diskFootprintReduction);

            checkDeprecated();
            ExcelsiorJet excelsiorJet = new ExcelsiorJet(jetHome);
            new JetBuildTask(excelsiorJet, jetProject).execute();
        } catch (JetTaskFailureException | JetHomeException  e) {
            throw new MojoFailureException(e.getMessage());
        } catch (CmdLineToolException | IOException e) {
            logger.debug("JetTask execution error", e);
            logger.error(e.getMessage());
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void checkDeprecated() {
        if (winVIVersion != null) {
            logger.warn(s("JetBuildTask.WinVIDeprecated.Warning", "winVIVersion", "version"));
            if (windowsVersionInfoConfiguration.version == null) {
                windowsVersionInfoConfiguration.version = winVIVersion;
            }
        }
        if (winVICopyright != null) {
            logger.warn(s("JetBuildTask.WinVIDeprecated.Warning", "winVICopyright", "copyright"));
            if (windowsVersionInfoConfiguration.copyright == null) {
                windowsVersionInfoConfiguration.copyright = winVICopyright;
            }
        }
        if (winVIDescription != null) {
            logger.warn(s("JetBuildTask.WinVIDeprecated.Warning", "winVIDescription", "description"));
            if (windowsVersionInfoConfiguration.description == null) {
                windowsVersionInfoConfiguration.description = winVIDescription;
            }
        }
    }

}
