[![Maven Central](https://img.shields.io/maven-central/v/com.excelsiorjet/excelsior-jet-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/com.excelsiorjet/excelsior-jet-maven-plugin)
Excelsior JET Maven Plugin
=====

*Excelsior JET Maven Plugin* provides Maven users with an easy way to compile their applications
down to optimized native Windows, OS X, or Linux executables with [Excelsior JET](http://excelsiorjet.com).
Such precompiled applications start and often work faster, do not depend on the JRE,
and are as difficult to reverse engineer as if they were written in C++.

### Prerequisites
Before using this plugin, you need to install Excelsior JET.
You may find a fully functional evaluation version of Excelsior JET [here](http://www.excelsiorjet.com/evaluate).
It is free for evaluation purposes and the only limitation it has is that it expires 90 days
after installation, along with all compiled applications.

**Note:** Excelsior JET does not yet support cross-compilation, so you need to build your application on each target platform
separately. The supported platforms are Windows (32- and 64-bit), Linux (32- and 64-bit), and OS X (64-bit).
  

### Overview

The current version of the plugin supports very basic functionality.
It can only handle *plain Java SE applications*, i.e. applications that have a main class
and all their dependencies are explicitly listed in the JVM classpath at launch time.
In other words, if your application can be launched using a command line
of the following form:
    
```
java -cp [dependencies-list] [main class]
```
and loads classes mostly from jars that are present
in the `dependencies-list`, then you can use this plugin.

This plugin will transform your application into an optimized native executable for the platform
on which you run Maven, and place it into a separate directory together with all required
Excelsior JET runtime files. In addition, it can either pack that directory into a zip archive
(all platforms) or create an Excelsior Installer setup (Windows and Linux only).
    
Excelsior JET supports many more features than this plugin.
We plan to cover all those features in the future.


### Usage

You need to copy and paste the following configuration into the `<plugins>` section of
your `pom.xml` file:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.4.2</version>
	<configuration>
		<mainClass></mainClass>
	</configuration>
</plugin>
```

set the `<mainClass>` parameter, and use the following command line to build the application:

```
mvn jet:build
```

### Excelsior JET Installation Directory Lookup

In order to do its job, the plugin needs to locate an Excelsior JET installation.
You have three ways to specify the Excelsior JET installation directory explicitly:

- add the `<jetHome>` parameter to the `<configuration>` section of the plugin
- pass the `jet.home` system property on the Maven command line as follows:
```
mvn jet:build -Djet.home=[JET-Home]
```
- or set the `JET_HOME` O/S environment variable

If none of above is set, the plugin searches for an Excelsior JET installation along the `PATH`.
So if you only have one copy of Excelsior JET installed, the plugin should be able to find it on Windows right away,
and on Linux and OS X - if you have run the Excelsior JET `setenv` script prior to launching Maven.

### Build process

The native build is performed in the `jet` subdirectory of the Maven target build directory.
First, the plugin copies the main application jar to the `jet/build` directory,
and copies all its run time dependencies to `jet/build/lib`.
Then it invokes the Excelsior JET AOT compiler to compile all those jars into a native executable.
Upon success, it copies that executable and the required Excelsior JET Runtime files
into the `jet/app` directory, binds the executable to that copy of the Runtime,
and copies the contents of the `<packageFilesDir>` directory recursively
to `jet/app`, if applicable (see "Customizing Package Content" below.)

> Your natively compiled application is ready for distribution at this point: you may copy
> the contents of the `jet/app` directory to another computer that has neither Excelsior JET nor
> the Oracle JRE installed, and the executable should work as expected.

Finally, the plugin packs the contents of the `jet/app` directory into
a zip archive named `${project.build.finalName}.zip` so as to aid single file re-distribution.
On Windows and Linux, you can also set the `<packaging>excelsior-installer</packaging>`
configuration parameter to have the plugin create an Excelsior Installer setup instead.

In the future, the plugin will also support the creation of OS X app bundles.

### Performing a Test Run

The plugin can run your Java application on the Excelsior JET JVM
using a JIT compiler before pre-compiling it to native code. This so-called Test Run
helps Excelsior JET:

* verify that your application can be executed successfully on the Excelsior JET JVM.
  Usually, if the Test Run completes normally, the natively compiled application also works well.
* detect the optional parts of Excelsior JET Runtime that are used by your application.
  For instance, JavaFX Webkit is not included in the resulting package by default
  due to its size, but if the application used it during a Test Run, it gets included automatically.
* collect profile information to optimize your app more effectively

To perform a Test Run, execute the following Maven command:

```
mvn jet:testrun
```

The plugin will place the gathered profiles in the `${project.basedir}/src/main/jetresources` directory.
Incremental changes of application code do not typically invalidate the profiles, so
it is recommended to commit the profiles (`.usg`, `.startup`) to VCS to allow the plugin
to re-use them during automatic application builds without performing a Test Run.

It is recommended to perform a Test Run at least once before building your application.

Note: 64-bit versions of Excelsior JET do not collect `.usg` profiles yet.
      So it is recommended to perform a Test Run on the 32-bit version of Excelsior JET at least once.

The profiles will be used by the Startup Optimizer and the Global Optimizer (see below).

Note: During a Test Run, the application executes in a special profiling mode,
      so disregard its modest start-up time and performance.

### Configurations other than `<mainClass>`

For a complete list of parameters, look into the Javadoc of `@Parameter` field declarations
of the
[AbstractJetMojo](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/AbstractJetMojo.java)
and [JetMojo](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/JetMojo.java)
classes. Most of them have default values derived from your `pom.xml` project
such as `<outputName>` parameter specifying resulting executable name.

There are also two useful Windows-specific configuration parameters:

`<hideConsole>true</hideConsole>` – hide console

`<icon>`*icon-file*`</icon>` – set executable icon (in Windows .ico format)

It is recommended to place the executable icon into a VCS, and if you place it to
`${project.basedir}/src/main/jetresources/icon.ico`, you do not need to explicitly specify it
in the configuration. The plugin uses the location `${project.basedir}/src/main/jetresources`
for other Excelsior JET-specific resource files (such as the EULA for Excelsior Installer setups).

#### Customizing Package Content

By default, the final package contains just the resulting executable and the necessary Excelsior JET Runtime files.
However, you may want the plugin to add other files to it: README, license, media, help files,
third-party native libraries, and so on. For that, add the following configuration parameter:

`<packageFilesDir>`*extra-package-files-directory*`</packageFilesDir>`

referencing a directory with all such extra files that you need added to the package.
The contents of the directory will be copied recursively to the final package.

By default, the plugin assumes that the extra package files reside
in the `src/main/jetresources/packagefiles` subdirectory of your project,
but you may dynamically generate the contents of that directory by means of other Maven plugins
such as `maven-resources-plugin`.

#### Excelsior Installer Configurations

The plugin supports the creation of Excelsior Installer setups -
conventional installer GUIs for Windows or self-extracting archives with command-line interface
for Linux.

To create an Excelsior Installer setup, add the following configuration into the plugin
`<configuration>` section:

`<packaging>excelsior-installer</packaging>`

Excelsior Installer setup, in turn, has the following configurations:

* `<product>`*product-name*`</product>` - default is `${project.name}`

* `<vendor>`*vendor-name*`</vendor>` -  default is `${project.organization.name}`

* `<version>`*product-version*`</version>` - default is `${project.version}`

* `<eula>`*end-user-license-agreement-file*`</eula>` - default is `${project.basedir}/src/main/jetresources/eula.txt`

* `<eulaEncoding>`*eula-file-encoding*`</eulaEncoding>` - default is `autodetect`. Supported encodings are US-ASCII (plain text), UTF16-LE

* `<installerSplash>`*installer-splash-screen-image*`</installerSplash>` - default is `${project.basedir}/src/main/jetresources/installerSplash.bmp`

#### Windows Version-Information Resource Configurations

On Windows, the plugin automatically adds a
[version-information resource](https://msdn.microsoft.com/en-us/library/windows/desktop/ms646981%28v=vs.85%29.aspx)
to the resulting executable. This can be disabled by specifying the following
configuration:

    <addWindowsVersionInfo>false</addWindowsVersionInfo>

By default, the values of version-information resource strings are derived from project settings.
The values of `<product>` and `<vendor>` configurations are used verbatim as
`ProductName` and `CompanyName` respectively;
other defaults can be changed using the following configuration parameters:

* `<winVIVersion>`*version-string*`</winVIVersion>` - version number (both `FileVersion` and `ProductVersion` strings are set to this same value)

    **Notice:** unlike Maven `${project.version}`, this string must have format `v1.v2.v3.v4`, where vi is a number.
    The plugin would use heuristics to derive a correct version string from the specified value if the latter
    does not meet this requirement, or from `${project.version}` if this configuration is not present.

* `<winVICopyright>`*legal-copyright*`</winVICopyright>` - `LegalCopyright` string, with default value derived from other parameters

* `<winVIDescription>`*executable-description*`</winVIDescription>` - `FileDescription` string, default is `${project.name}`

#### Multi-app Executables

The plugin may compile more than one application into a single executable and
let you select a particular application at launch time via command line arguments.

The command line syntax of [multi-app executables](http://www.excelsior-usa.com/doc/jet/jetw011.html#0330)
is an extension of the `java` launcher command
line syntax that allows specifying the main class, VM options, Java system properties,
and the arguments of the application:

```
    Exe-name [Properties-and-options] Main-classname [App-arguments]
```

To enable the multi-app mode add the following configuration parameter:

`<multiApp>true</multiApp>`

#### Defining System Properties and JVM Arguments

**New in 0.4.3:**
If you do not opt multi-app executable generation, the plugin will generate a conventional executable which command line
arguments are arguments of the main class you specify so there is no place to set
a system property or a JVM argument such as `-Dprop=value` or `-Xmx1G` on the application`s command line.
To address this, the plugin allows to hardwire system properties and JVM arguments
into the resulting executable using the following plugin configuration:

```xml
<jvmArgs>
    <jvmArg>-Dprop=value</jvmArg>
    <jvmArg>-jvmArg</jvmArg>
</jvmArgs>
```

The configuration also affects on a Test Run and can be used with multi-app executables also
(thus you will not need to specify them explicitly on the command line).

System property values may contain references to root directories from the package files in the form of `$(Root)`.
For example, suppose the package contains a subdirectory `AppFiles`. You may define the following property:

`my.app.files.dir=$(Root)/AppFiles`

Then you prepare a package and install it into a directory on the target system.
Upon application startup, the JET Runtime replaces `$(Root)` with the absolute path of the installation directory.
Thus, when the installed application inquires the value of the `my.app.files.dir` property,
it gets the full path to the `AppFiles` directory on the target system.

**Note:** most `-XX` options that you might use for Oracle HotSpot are not supported as they are specific
to HotSpot JVM implementation. Moreover, certain `-X` options are not supported too,
for example setting `-Xbootclasspath` or `-Xms` (initial Java heap size) take no effect.

##### Standard JVM arguments
The JET Runtime recognizes the following standard JVM arguments:

`-ea, -da, -enableassertions, -disableassertions, -esa, -dsa, -enablesystemassertions, -disablesystemassertions`

Assertion directives.

`-Xmx`

Maximum heap size.
Setting size to zero (default) enables adaptive heap size.
See [Memory management](http://www.excelsior-usa.com/doc/jet/jetw011.html#0324) for more information.

`-Xss`

Maximum stack size.

`-Xverify:all`

 Enabling strict verifier.

`-XX:MaxDirectMemorySize`

Setting maximum memory size for direct buffers.

`-javaagent:`

Specifying a Java Agent (for non-compiled classes).

`-version`, `-verbose:gc`

##### JET Runtime-specific properties
In addition, the JET Runtime recognizes the following properties:

`-Djet.gc.ratio=ratio`

Specifies the maximum proportion of CPU time to be allocated to the garbage collector at runtime, in 1/1000ths.
See [GC ratio](http://www.excelsior-usa.com/doc/jet/jetw011.html#0325) for details.

`-Djet.rt=flavor-name`

Select a specific JET Runtime. Valid values of flavor-name are `classic`, `desktop`, and `server`.

See [Runtime Selection](http://www.excelsior-usa.com/doc/jet/jetw011.html#0306) for details.

`-Djet.gc.threads=N`

Sets the maximum number of concurrent threads that may be used for the CoreBalance garbage collection
available in the Server Runtime. By default, N equals the number of processors/cores on the system.
See [Parallel hardware](http://www.excelsior-usa.com/doc/jet/jetw011.html#0329) for more information.

`-Djet.jit.memory.reserve=value`

This option is specific to 64-bit version of the JET Runtime. I
t defines the amount of address space reserved for code and data produced by the JIT compiler.
The default value is 1 gigabyte and the maximum value is 2 gigabytes.

#### Startup Accelerator Configurations

The Startup Accelerator improves the startup time of applications compiled with Excelsior JET.
The plugin automatically runs the compiled application immediately after build,
collects the necessary profile information and hard-wires it into the executable just created.
The JET Runtime will then use the information to reduce the application startup time.
The Startup Accelerator is enabled by default, but you may disable it by specifying the following
configuration:

`<profileStartup>false</profileStartup>`

You may also specify the duration of the profiling session in seconds by specifying the following
configuration:

`<profileStartupTimeout>`*duration-in-seconds*`</profileStartupTimeout>`

As soon as the specified period elapses, profiling stops and the application is automatically terminated,
so ensure that the timeout value is large enough to capture all actions the application nomrally carries out
during startup. (It is safe to close the application manually if the profiling period proves to be excessively long.)

#### Global Optimizer

The 32-bit versions of Excelsior JET feature the Global Optimizer - a powerful facility that has several
important advantages over the default compilation mode:

* single component linking yields an executable that does not require the dynamic libraries
  containing the standard Java library classes,
  thus reducing the size of the installation package and the disk footprint of the compiled application
* global optimizations improve application performance and reduce the startup time and memory usage

By default, Excelsior JET uses the *dynamic link model*. It only compiles application classes, 
linking them into an executable that depends on dynamic libraries containing precompiled
Java SE platform classes. These dynamic libraries, found in the JET Runtime, have to be
distributed together with the executable.

The Global Optimizer detects the platform classes that are actually used by the application
and compiles them along with application classes into a single executable.
Even though the resulting binary occupies more disk space compared with the one built
in the default mode, it no longer requires the dynamic libraries with platform classes.
This results, among other benefits, in a considerable reduction of the application
installation package size.

To enable the Global Optimizer, add the following configuration parameter:

`<globalOptimizer>true</globalOptimizer>`

**Note:** performing a Test Run is mandatory if the Global Optimizer is enabled.

#### Java Runtime Slim-Down Configurations

The 32-bit versions of Excelsior JET feature Java Runtime Slim-Down, a unique
Java application deployment model delivering a significant reduction
of application download size and disk footprint.

The key idea is to select the components of the Java SE API that are not used by the application,
and exclude them from the installation altogether. Such components are called *detached*.
For example, if your application does not use any of Swing, AWT, CORBA or, say, JNDI API,
Excelsior JET enables you to easily exclude from the main setup package the standard library
classes implementing those APIs and the associated files, placing them in a separate *detached package*.

The detached package should be placed on a Web server so that the JET Runtime could download it
if the deployed application attempts to use any of the detached components via JNI or the Reflection API.

To enable Java Runtime Slim-Down, copy and paste the following plugin configuration:

```xml
<javaRuntimeSlimDown>
    <detachedBaseURL></detachedBaseURL>
</javaRuntimeSlimDown>
```

and specify the base URL of the location where you plan to place the detached package, e.g.
`http://www.example.com/download/myapp/detached/`.

By default, the plugin automatically detects which Java SE APIs your application does not use
and detaches the respective JET Runtime components from the installation package.
Alternatively, you may enforce detaching of particular components using the following parameter
under the `<javaRuntimeSlimDown>` configuration section:

`<detachComponents>`*comma-separated list of APIs*`</detachComponents>`

Available detachable components: `corba, management, xml, jndi, jdbc, awt/java2d, swing, jsound, rmi, jax-ws`

At the end of the build process, the plugin places the detached package in the `jet` subdirectory
of the Maven target build directory. You may configure its name with the `<detachedPackage>` parameter
of the `<javaRuntimeSlimDown>` section (by default the name is `${project.build.finalName}.pkl`).

Do not forget to upload the detached package to the location specified in `</detachedBaseURL>`
above before deploying your application to end-users.

**Note:** Enabling Java Runtime Slim-Down automatically enables the Global Optimizer, 
          so performing a Test Run is mandatory for Java Runtime Slim-Down as well.

**Known issue:** Java Runtime Slim-Down does not work with the `excelsior-installer` packaging type yet
                 due to a bug in Excelsior JET. We are going to fix it in the next update of Excelsior JET.

#### Creating Trial Versions

You can create a trial version of your Java application that will expire in a specified number of days
after the build date of the executable, or on a fixed date.
Once the trial period is over, the application will refuse to start up,
displaying a custom message.

To enable trial version generation, copy and paste into your `pom.xml` file the following plugin configuration:

```xml
<trialVersion>
    <expireInDays></expireInDays>
    <expireMessage></expireMessage>
</trialVersion>
```

and specify the number of calendar days after the build date when you want the application
to expire, and the error message that the expired binary should display to the user on a launch attempt.

You can also set a particular, fixed expiration date by using the `<expireDate>` parameter
instead of `<expireInDays>`. The format of the `<expireDate>` parameter value
is *ddMMMyyyy*, for example `15Sep2020`.

**Note:** If you choose the `excelsior-installer` `<packaging>` type, the generated setup
package will also expire, displaying the same message to the user.

One common usage scenario of this functionality is setting the hard expiration date further into the future,
while using some other mechanism to enforce a (shorter) trial period.
Typically, you would set the hard expiration date somewhat beyond the planned release 
date of the next version of your application. This way, you would ensure that nobody uses
an outdated trial copy for evaluation.

## Sample Project

To demonstrate the process and result of plugin usage, we have forked the [JavaFX VNC Client](https://github.com/comtel2000/jfxvnc) project on GitHub, added the Excelsior JET plugin to its `pom.xml` file, and run it through Maven to build native binaries for three platforms.

You can download the binaries from here:

* [Windows (32-bit, 19MB installer)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-windows-x86.exe)
* [OS X (64-bit, 44MB)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-osx-amd64.zip)
* [Linux (64-bit, 30MB installer)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-linux-amd64.bin)

or clone [the project](https://github.com/pjBooms/jfxvnc) and build it yourself:

```
    git clone https://github.com/pjBooms/jfxvnc
    cd jfxvnc/ui
    mvn jet:build
```

## Release Notes

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
* Error message corrected for "[Cannot find jar if classifier is used #10]
  (https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues/10)",
  explicitly referring `<mainJar>` plugin parameter that should be set for the case.

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

## Roadmap

Even though we are going to base the plugin development on your feedback in the future, we have our own short-term plan as well.
So the next few releases will add the following features:

* Mapping of compiler and packager options to plugin configuration parameters.
* Creation of Mac OS X application bundles.
* Code signing.
* Tomcat Web Applications support.
* Multi-component support: building dependencies into separate native libraries
                           to reuse them across multiple Maven project builds
                           so as to reduce overall compilation time

Note that the order of appearance of these features is not fixed and can be adjusted based on your feedback.
