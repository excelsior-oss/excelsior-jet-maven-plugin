[![Maven Central](https://img.shields.io/maven-central/v/com.excelsiorjet/excelsior-jet-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/com.excelsiorjet/excelsior-jet-maven-plugin)
Excelsior JET Maven Plugin
=====

*Excelsior JET Maven Plugin* provides Maven users with an easy way to compile their applications
down to optimized native Windows, OS X, or Linux executables with [Excelsior JET](http://excelsiorjet.com).
Such precompiled applications start and often work faster, do not depend on the JRE,
and are as difficult to reverse engineer as if they were written in C++.

  * [Basic Usage](#basic-usage)
  * [Full Documentation](#full-documentation)
  * [Sample Project](#sample-project)
  * [Communication](#communication)
  * [Release Notes](#release-notes)
  * [Roadmap](#roadmap)


## Basic Usage


The current version of the plugin supports four types of applications:

*   _Plain Java SE applications_, defined as applications that (a) can be run
    with all dependencies explicitly listed on the command-line
    of the conventional `java` launcher:
    `java [-cp` _dependencies-list_ `] `_main-class_
    and (b) load classes mostly from the listed jars,

*   [Tomcat Web applications](https://www.excelsiorjet.com/solutions/protect-java-web-applications)
    — `.war` files that can be deployed to the Apache Tomcat application server,

*   **Invocation dynamic libraries** (e.g. Windows DLLs) callable from non-JVM languages, and

*   Java applications disguised as **Windows services** using the
    [Excelsior JET WinService API](https://www.excelsiorjet.com/docs/WinService/javadoc/)

Assuming that a copy of Excelsior JET is accessible via the operating system `PATH`,
here is what you need to do to use it in your Maven project:

### Configuring

First, copy and paste the following configuration into the `<plugins>`
section of your `pom.xml` file:

    <plugin>
        <groupId>com.excelsiorjet</groupId>
        <artifactId>excelsior-jet-maven-plugin</artifactId>
        <version>1.2.0</version>
        <configuration>
        </configuration>
    </plugin>

then proceed depending on the type of your application:

  * [Plain Java SE Application](#plain-java-se-application)
  * [Tomcat Web Application](#tomcat-web-application)
  * [Invocation Library](#invocation-library)
  * [Windows Service](#windows-service)

#### Plain Java SE Application


1.  Add the following to the `<configuration>` section:

        <configuration>
            <mainClass></mainClass>
        </configuration>

2.  Set the value of the `<mainClass>` parameter to the
    name of the main class of your application.

3.  Optionally, conduct a Test Run:

        mvn jet:testrun

4.  Optionally, collect an execution profile (not available for 32-bit Intel x86 targets yet):

        mvn jet:profile

5.  [Build the project](#building)

#### Tomcat Web Application


1.  Add the following to the `<configuration>` section:

        <configuration>
            <tomcatConfiguration>
                 <tomcatHome></tomcatHome>
            </tomcatConfiguration>
        </configuration>

2.  Set the `<tomcatHome>` parameter to point to the
    _master_ Tomcat installation — basically, a clean Tomcat instance that was never launched.

3.  Optionally, conduct a Test Run:

        mvn jet:testrun

4.  Optionally, collect an execution profile (not available for 32-bit Intel x86 targets yet):

        mvn jet:profile

5.  [Build the project](#building)


#### Invocation Library


1.  Add the following to the `<configuration>` section:

        <configuration>
            <appType>dynamic-library</appType>
        </configuration>

    **Warning:** Testing and using dynamic libraries that expose Java APIs is tricky.
    Make sure to read the respective
    [section](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Invocation-Dynamic-Libraries)
    of the plugin documentation.

2.  Optionally, create a profiling image (not available for 32-bit Intel x86 targets yet):

        mvn jet:profile

    and collect an execution profile by running a test application that loads your library from the created image.

3.  [Build the project](#building)


#### Windows Service

1.  Implement a class extending `com.excelsior.service.WinService`,
    as described in the [Excelsior JET WinService API documentation](https://www.excelsiorjet.com/docs/WinService/javadoc/).

2.  Add a dependency on the Excelsior JET WinService API to your Maven project.
    Copy and paste the following snippet to the `<dependencies>`
    section of your `pom.xml` file:


        <dependency>
            <groupId>com.excelsiorjet</groupId>
            <artifactId>excelsior-jet-winservice-api</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>


3.  Add the following to the `<configuration>` section:

        <configuration>
            <appType>windows-service</appType>
            <mainClass></mainClass>
            <windowsServiceConfiguration>
                <name></name>
                <displayName></displayName>
                <description></description>
                <arguments>
                    <argument></argument>
                </arguments>
                <logOnType></logOnType>
                <allowDesktopInteraction></allowDesktopInteraction>
                <startupType></startupType>
                <startServiceAfterInstall></startServiceAfterInstall>
                <dependencies>
                     <dependency></dependency>
                </dependencies>
            </windowsServiceConfiguration>
        </configuration>

4.  Set `<mainClass>` to the name of the class implemented on Step 1.
    For descriptions of all other parameters, refer to
    [plugin documentation](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Windows-Services).

    You may find complete information on Windows services support in Excelsior JET
    in the "Windows Services" Chapter of the
    [Excelsior JET for Windows User's Guide.](https://www.excelsiorjet.com/docs/jet/jetw)

5.  Optionally, create a profiling image (not available for 32-bit Intel x86 targets yet):

        mvn jet:profile

    and collect an execution profile by installing and running the service from the created image.

6.  [Build the project](#building)

### Building

Run Maven with the `jet:build` goal:

    mvn jet:build

At the end of a successful build, the plugin will place your natively compiled
Java application/library and the required pieces of Excelsior JET Runtime:

  * in the `target/jet/app` subdirectory of your project
  * in a zip archive named `${project.build.finalName}.zip`.

If your project is a plain Java SE application or Tomcat Web application, you can then
run it:

    mvn jet:run

Refer to [plugin documentation](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki) for further instructions.


## Full Documentation

See the [Wiki](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki) for full documentation on the plugin.

  * [Home](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki)
  * [Prerequisites](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Prerequisites)
  * [Getting Started](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Getting-Started)
  * [Build Process](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process)

      - [Test Run](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process#test-run)
      - [Profiling](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process#profiling)
      - [Compilation](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process#compilation)
      - [Packaging](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process#packaging)
      - [Running](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Build-Process#running)

**Compilation Settings:**

  * [Incremental Compilation](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Incremental-Compilation)
  * [Dependency-Specific Settings](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Dependency-Specific-Settings)
  * [Optimizations](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Optimization-Settings)
  * [Target Executable](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Target-Executable-Settings)
  * [Application Apperarance](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Application-Appearance)
  * [Raw Compiler Options](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Raw-Compiler-Options)

**Packaging Settings:**

  * [Package Contents](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Customizing-Package-Contents)
  * [System Properties And JVM Arguments](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Defining-System-Properties-And-JVM-Arguments)
  * [Excelsior JET Runtime](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Excelsior-JET-Runtime-Configurations)
  * [Excelsior Installer (Windows/Linux)](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Excelsior-Installer-Configurations)
  * [OS X App Bundles And Installers](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Creating-OS-X-Application-Bundles-And-Installers)

**Application Type Specifics:**

  * [Tomcat Web Applications](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Tomcat-Web-Applications)
  * [Dynamic Libraries](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Invocation-Dynamic-Libraries)
  * [Windows Services](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/wiki/Windows-Services)


Refer to the [Excelsior JET User's Guide](https://www.excelsiorjet.com/docs)
and [Knowledge Base](https://www.excelsiorjet.com/kb)
for complete usage information.


## Sample Project

To demonstrate the process and result of plugin usage, we have forked the [JavaFX VNC Client](https://github.com/comtel2000/jfxvnc) project on GitHub, added the Excelsior JET plugin to its `pom.xml` file, and run it through Maven to build native binaries for three platforms.

You can download the binaries from here:

* [Windows (32-bit, 14MB installer)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-windows-x86.exe)
* [OS X (64-bit, 45MB installer)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-osx-amd64.pkg)
* [Linux (64-bit, 30MB installer)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-linux-amd64.bin)

or clone [the project](https://github.com/pjBooms/jfxvnc) and build it yourself:

```
    git clone https://github.com/pjBooms/jfxvnc
    cd jfxvnc/ui
    mvn jet:build
```


## Communication

To report a bug in the plugin, or suggest an improvement, use
[GitHub Issues](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues).

To receive alerts on plugin and Excelsior JET updates, subscribe to
the [Excelsior JET RSS feed](https://www.excelsior-usa.com/blog/category/excelsior-jet/feed/),
or follow [@ExcelsiorJET](https://twitter.com/ExcelsiorJET) on Twitter.


## Release Notes

Version 1.2.0 (08-May-2018)

`<pdb>` configuration section introduced to control the location of the Project Database (PDB).
PDB is used for incremental compilation: once a full build succeeds, only the changed project dependencies
are recompiled during the subsequent builds.
The configuration, as well as the incremental compilation feature, are available only for Excelsior JET 15 and above, and only for targets other than 32-bit x86.
This release of the plugin places the PDB outside of the build directory by default to enable incremental compilation even for clean builds.
In addition, this version of the plugin also introduces the `jet:clean`  task for cleaning the PDB.

Version 1.1.3 (20-Apr-2018)

Filter `pom` dependencies (issue #69).


Version 1.1.2 (26-Oct-2017)

Fix for `NullPointerException` when a shortcut with no icon is used for Excelsior Installer backend (issue (#62)[https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues/62])


Version 1.1.0 (07-Jul-2017)

Support for new features of Excelsior JET 12 and other enhancements:

  * Global Optimizer is now enabled for all target platforms
  * **Profile** task introduced to enable the use of Profile-Guided Optimization
    (not available for 32-bit Intel x86 targets yet):

            mvn jet:profile

  * **Run** task introduced for running the natively compiled application right after the build:

            mvn jet:run

  * Fix for a file copying [issue](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues/57).

Version 1.0.0 (04-May-2017)

First non-beta release. Here is what we have done:

  * Reworked plugin documentation and moved it to the Wiki
  * Tested the plugin against all platforms/editions that Excelsior JET 11.0 and 11.3 support
  * Fixed a handful of minor bugs reported by users and found during testing
  * Added the somehow overlooked `<stackAllocation>` parameter
    that controls allocation of Java objects on the stack

**Backward incompatibile change alert:** Windows version-information resource generation
is now _off_ by default. To revert to the previous behavior, add
`<addWindowsVersionInfo>`*`true`*`</addWindowsVersionInfo>` to the plugin configuration.


Version 0.9.5 aka 1.0 Release Candidate (15-Feb-2017)

This release covers all Excelsior JET features accessible through the JET Control Panel GUI,
and all options of the `xpack` utility as of Excelsior JET 11.3 release, except for three things
that we do not plan to implement in the near future, for different reasons:
creation of update packages, Eclipse RCP applications support, and internationalization
of Excelsior Installer messages.
If you are using any other Excelsior JET functionality that the plugin does not support,
please create a feature request [here](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues).
Otherwise, think of this version as of 1.0 Release Candidate 1.

Compared with the previous releases, the following functionality was added to the plugin:

* `<packageFiles>` parameter introduced to add separate files/folders to the package
* `<excelsiorInstaller>` configuration section extended with the following parameters:
    - `<language>` - to set installation wizard language
    - `<cleanupAfterUninstall>` - to remove all files on uninstall
    - `<afterInstallRunnable>` - to run an executable after installation
    - `<compressionLevel>` - to control installation package compression
    - `<installationDirectory>` - to change installation directory defaults
    - `<registryKey>` - to customize the registry key used for installation on Windows
    - `<shortcuts>` - to add shortcuts to the Windows Start menu, desktop, etc.
    - `<noDefaultPostInstallActions>` - to not add the default post-install actions
    - `<postInstallCheckboxes>` - to configure post-install actions
    - `<fileAssociations>` - to create file associations
    - `<installCallback>` - to set install callback dynamic library
    - `<uninstallCallback>` - to set uninstall callback dynamic library
    - `<welcomeImage>`, `<installerImage>`, `<uninstallerImage>` - to customize (un)installer appearance
* `<allowUserToChangeTomcatPort>` parameter added to the `<tomcat>` configuration section
  to allow the user to change the Tomcat port at install time

Version 0.9.4 (24-Jan-2017)

* `typical` and `smart` optimization presets introduced.

Version 0.9.3 (19-Jan-2017)

* `<runtime>` configuration section introduced and related parameters moved to it:
   `<locales>`, `<profile>`, `<optRtFiles>` (renamed to `<components>`), `<javaRuntimeSlimDown>` (renamed to `<slimDown>`).
   Old configuration parameters are now deprecated and will be removed in a future release.
   New parameters added to the `<runtime>` section:
    - `<flavor>` to select a runtime flavor
    - `<location>` to change runtime location in the resulting package
    - `<diskFootprintReduction>` to reduce application disk footprint

* Windows version-info resource configuration changed to meet other enclosed configurations style.
  Old way to configure Windows version info is deprecated and will be removed in a future release.

Version 0.9.2 (12-Jan-2017)

Issue with buildnumber-maven-plugin #49 fixed

Version 0.9.1 (02-Dec-2016)

* Support for Compact Profiles
* Not working Test Run for 7+ Tomcat versions fixed(issue #42)

Version 0.9.0 (23-Nov-2016)

Invocation dynamic libraries and Windows services support.

Version 0.8.1 (28-Oct-2016)

The release supports [Excelsior JET Embedded 11.3 for Linux/ARM](https://www.excelsiorjet.com/embedded/).

Version 0.8.0 (20-Oct-2016)

The release adds the capability to set Excelsior JET-specific properties for project dependencies,
such as code protection, selective optimization, and resource packing.

Version 0.7.2 (19-Aug-2016)

This release adds the capability to pass command-line arguments to the application
during startup profiling and the test run.

Version 0.7.1 (10-Aug-2016)

This release covers most of the compiler options that are available in the JET Control Panel UI,
and all options of the `xpack` utility as of Excelsior JET 11.0 release:

  * `<splash>` parameter introduced to control the appearance of your application on startup
  * `<inlineExpansion>` parameter introduced to control aggressiveness of methods inlining
  * `<stackTraceSupport>` parameter introduced to set stack trace support level
  * `<compilerOptions>` parameter introduced to set advanced compiler options and equations
  * `<locales>` parameter introduced to add additional locales and charsets to the resulting package

Version 0.7.0 (22-June-2016)

* Massive refactoring that introduces `excelsior-jet-api` module: a common part between Maven and Gradle
  Excelsior JET plugins

* `<jetResourcesDir>` parameter introduced to set a directory containing Excelsior JET specific resource files
   such as application icons, installer splash, etc.

Version 0.6.0 (30-May-2016)

* Compilation of Tomcat Web applications is supported

Version 0.5.1 (13-Apr-2016)

* Fix for incorrect default EULA value for Excelsior Installer

Version 0.5.0 (04-Apr-2016)

* Mac OS X application bundles and installers support

Version 0.4.4 (11-Mar-2016)

* `<protectData>` parameter added to enable data protection

Version 0.4.3 (17-Feb-2016)

* `<jvmArgs>` parameter introduced to define system properties and JVM arguments

Version 0.4.2 (11-Feb-2016)

* Trial version generation is supported

Version 0.4.1 (05-Feb-2016)

* `<packageFilesDir>` parameter introduced to add extra files to the final package

Version 0.4.0 (03-Feb-2016)

Reduced the download size and disk footprint of resulting packages by means of supporting:

* Global Optimizer
* Java Runtime Slim-Down

Version 0.3.2 (01-Feb-2016)

* "[Changes are not reflected in compiled app if building without clean #11](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues/11)" issue fixed
* Error message corrected for "[Cannot find jar if classifier is used #10](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues/10)",
  explicitly referring the `<mainJar>` plugin parameter that should be set in such cases.

Version 0.3.1 (26-Jan-2016)

* `<optRtFiles>` parameter introduced to add optional JET runtime components

Version 0.3.0 (22-Jan-2016)

* Startup Accelerator supported and enabled by default
* Test Run Mojo implemented that enables:
   - running an application on the Excelsior JET JVM before pre-compiling it to native code
   - gathering application execution profiles to enable the Startup Optimizer

Version 0.2.1 (21-Jan-2016)

* Support of multi-app executables

Version 0.2.0 (14-Dec-2015)

* Support of Excelsior Installer setup generation
* Windows Version Information generation

Version 0.1.0 (08-Dec-2015)
* Initial release supporting compilation of the Maven Project with all dependencies into native executable
and placing it into a separate directory with required Excelsior JET runtime files.
