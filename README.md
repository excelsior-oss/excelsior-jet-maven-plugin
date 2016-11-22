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

This plugin will transform your application into an optimized native executable for the platform
on which you run Maven, and place it into a separate directory together with all required
Excelsior JET runtime files. In addition, it can either pack that directory into a zip archive
(all platforms), create an Excelsior Installer setup (Windows and Linux only)
or an OS X application bundle/installer.

The current version of the plugin can handle four types of applications:

* **Plain Java SE applications**, i.e. applications that have a main class
and have all their dependencies explicitly listed in the JVM classpath at launch time, and

* **Tomcat Web applications** &mdash; `.war` files that can be deployed to the
  Apache Tomcat application server.

* **Invocation Dynamic Libraries** (e.g. Windows DLLs), callable
  from non-JVM languages via the Invocation API

* **Windows Services** special long-running processes that may be launched
   during operating system bootstrap and use the
   [Excelsior JET WinService API](https://github.com/excelsior-oss/excelsior-jet-winservice-api)
   (Windows only)

In other words, if your application can be launched using a command line
of the following form:

```
java -cp [dependencies-list] [main class]
```

and loads classes mostly from jars that are present
in the `dependencies-list`, *or* if it is packaged into a `.war` file that can be deployed
to a Tomcat application server instance, then you can use this plugin.
Invocation Dynamic Libraries and Windows Services are essentially special build modes
of plain Java SE applications that yield different executable types: dynamic libraries or Windows services.

Excelsior JET can also compile Eclipse RCP applications.
The plugin does not yet support Eclipse RCP projects nor some advanced Excelsior JET features.
We plan to cover all that functionality in the future, but if you need the plugin to support
a particular feature sooner rather than later, you can help us prioritize the roadmap
by creating a feature request [here](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/issues).

### Usage

If your project is a plain Java SE application, you need to copy and paste the following configuration into the `<plugins>`
section of your `pom.xml` file:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.8.1</version>
	<configuration>
		<mainClass></mainClass>
	</configuration>
</plugin>
```

set the `<mainClass>` parameter, and use the following command line to build the application:

```
mvn jet:build
```

For a Tomcat Web application, the `<mainClass>` parameter is not needed. Instead, you would need to add
the `<tomcatHome>` parameter pointing to a *clean* Tomcat installation, a copy of which will be used
for the deployment of your Web application at build time.
See [Building Tomcat Web Applications](#building-tomcat-web-applications) section below for more details.

An [Invocation Dynamic Library](#invocation-dynamic-libraries) does not need a main class either,
and the main class of a [Windows Service](#windows-services) application must extend a special class `com.excelsior.service.WinService`
of the [Excelsior JET WinService API](https://github.com/excelsior-oss/excelsior-jet-winservice-api).

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
configuration parameter to have the plugin create an Excelsior Installer setup instead,
and on OS X, setting `<packaging>osx-app-bundle</packaging>` will result in the creation
of an application bundle and, optionally, a native OS X installer package (`.pkg` file).

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

Your application may require command line arguments to run. If that is the case, set the `runArgs` plugin parameter as follows:
```xml
<runArgs>
   <runArg>arg1</runArg>
   <runArg>arg2</runArg>
</runArgs>
```
You may also pass the arguments via the `jet.runArgs` system property, where arguments are comma separated (use "`\`" to escape commas within arguments, i.e. `-Djet.runArgs="arg1,Hello\, World"` will be passed to your application as `arg1 "Hello, World"`)

### Configurations other than `<mainClass>`

For a complete list of parameters, look into the Javadoc of `@Parameter` field declarations
of the
[AbstractJetMojo](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/AbstractJetMojo.java)
and [JetMojo](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/JetMojo.java)
classes. Most of them have default values derived from your `pom.xml` project
such as `<outputName>` parameter specifying resulting executable name.

#### Application appearance
If the startup of your client application takes longer than you would have liked,
the thumb rule is to show a splash screen.
A splash screen provides visial feedback about the loading process the end user, and
gives you an opportunity to display information about your product and company.
The splash screen functionality appeared in Java API since Java SE 6. For more details, see
http://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html

If the splash image has been specified in the manifest of the application's JAR file,
the respective image will be obtained automatically,
otherwise, you may assign a splash screen image to the application manually:

`<splash>`*splash-file*`</splash>`

It is recommended to place the splash image in a VCS, and if you place it at
`${project.basedir}/src/main/jetresources/splash.png`, you won't need to specify it
in the configuration explicitly. The plugin uses the location `${project.basedir}/src/main/jetresources`
for other Excelsior JET-specific resource files (such as the EULA for Excelsior Installer setups).

There are also two useful Windows-specific configuration parameters:

`<hideConsole>true</hideConsole>` – hide console

`<icon>`*icon-file*`</icon>` – set executable icon (in Windows .ico format)

Just as it works for the splash image, if you place the icon file at
`${project.basedir}/src/main/jetresources/icon.ico`, you won't need to specify it
in the configuration explicitly.

#### Dependency-specific settings

As mentioned [above](#build-process), the plugin automatically picks up and compiles the run time dependencies
of your Maven project.
In addition, the plugin enables you to specify certain processing rules separately
for each dependency, or for groups of dependencies:

- enforce code protection for all classes
- enable selective optimization of classes
- control packing of resource files into the resulting executable

##### Dependencies configuration

To set these properties for a particular dependency, add the following configuration to the plugin configuration
section:

```xml
<dependencies>
    <dependency>
        <groupId>groupId</groupId>
        <artifactId>artifactId</artifactId>
        <version>version</version>
        <protect></protect>     <!-- all | not-required -->
        <optimize></optimize>   <!-- all | auto-detect -->
        <pack></pack>           <!-- all | auto-detect | none -->
    </dependency>
</dependencies>
```

where `<groupId>`, `<artifactId>`, and `<version>` identify the dependency in the same way as in
the respective global `<dependencies>` section of the Maven project,
and `<protect>`, `<optimize>`, and `<pack>` are Excelsior JET-specific properties for the dependency,
described below.

You may omit `<groupId>` and/or `<version>` from the configuration, if you are sure that there is
only one dependency with the given `<artifactId>` in the project. The plugin will issue an
ambiguous dependency resolution error if that is not the case.

You may also set only the `<groupId>` parameter to set the same properties for all dependencies
sharing the same `groupId` at once.

Finally, if you need some additional dependencies that are not listed in the project explicitly
to appear in the application classpath (for example, you need to access some resources in a directory
via `ResourceBundle.getResource()`), add, for each of them, a `<dependency>` configuration
with the `<path>` parameter pointing to the respective directory or jar/zip file,
*instead of* `<groupId>`, `<artifactId>`, and/or `<version>`:

```xml
<dependencies>
    <dependency>
        <path>path</path>
        <protect></protect>
        <optimize></optimize>
        <pack></pack>
    </dependency>
</dependencies>
```

You may also use the `<path>` parameter to identify project dependencies that are described
with the `<systemPath>` parameter.


##### Code protection

If you need to protect your classes from decompilers,
make sure that the respective dependencies have the `<protect>` property set to `all`.
If you do not need to protect classes for a certain dependency (e.g. a third-party library),
set it to the `not-required` value instead. The latter setting may reduce compilation time and the size of
the resulting executable in some cases.


##### Selective optimization

To optimize all classes and all methods of each class of a dependency for performance,
set its `<optimize>` property to `all`. The other valid value of that property is `auto-detect`.
It means that the Optimizer detects which classes from the dependency are used by the application
and compiles the dependency selectively, leaving the unused classes in bytecode or non-optimized form.
That helps reduce the compilation time and download size of the application.

You may want to enable selective optimization for the third-party dependencies of which your application
uses only a fraction of their implementing classes. However, it is not recommended to choose the
`auto-detect` value for the dependencies containing your own classes, because, in general,
the Excelsior JET Optimizer cannot determine the exact set of used classes due to possible access
via the Reflection API at run time. That said, you can help it significantly to detect such
dynamic class usage by performing a [Test Run](#performing-a-test-run) prior to the build.


##### Automatic dependency categorization

**IMPORTANT:**

As mentioned above, you may wish to set the `<optimize>` property to `auto-detect`
and the `<protect>` property to `not-required` for third-party dependencies, and
set both properties to `all` for the dependencies contaiting your own classes.
By default, the plugin distinguishes between application classes and third-party library classes
automatically using the following rule: it treats all dependencies sharing the `groupId` with the
main artifact as application classes, and all other dependencies as third-party dependencies.

Therefore, if some of your application classes reside in a dependency with a different `groupId`
than your main artifact, make sure to set the `<optimize>` and `<protect>` properties for them
explicitly, for instance:

```xml
<dependencies>
    <dependency>
        <groupId>my.company.project.group</groupId>
        <protect>all</protect>
        <optimize>all</optimize>
    </dependency>
</dependencies>
```

##### isLibrary hint

Instead of setting the `<protect>` and `<optimize>` properties, you may provide a semantic hint
to the future maintainers of the POM file that a particular dependency is a third party library
by setting its `<isLibrary>` property to `true`. The plugin will then set `<protect>`
to `not-required` and `<optimize>` to `auto-detect` automatically.
Conversely, if you set `<isLibrary>` to `false`, both those properties will be set to `all`.
The following configuration is therefore equivalent to the example in the
[previous section](#automatic-dependency-categorization):

```xml
<dependencies>
    <dependency>
        <groupId>my.company.project.group</groupId>
        <isLibrary>false</isLibrary>
    </dependency>
</dependencies>
```


##### Resource packing

**Note:** This section only applies to dependencies that are jar or zip files.

Dependencies often contain resource files, such as images, icons, media files, etc.
By default, the Excelsior JET Optimizer packs those files into the resulting executable.
If protection is disabled and selective optimization is enabled for a dependency,
the classes that were not compiled also get packed into the executable and will be
handled by the JIT compiler at run time on an attempt to load them. As a result, the
original jar files are no longer needed for the running application to work.

The above describes the behavior for dependencies that have the `<pack>` property
omitted or set to the default value of `auto-detect`. However, certain dependencies
may require presence of the original class files at application run time.
For instance, some third-party security providers, e.g. Bouncy Castle, check the sizes of
their class files during execution. In such a dependency, class files serve as both program code
*and* resources: even if all classes get pre-compiled,
you still have to make them available to the running application.
Setting the `<pack>` property of that dependency to `all` resolves the problem.

You may also opt to not pack a particular dependency into the executable at all by
setting its `<pack>` property to `none`. The dependency will then be copied
to the final package as-is.
To control its location in the package, use the `<packagePath>` parameter of
the `<dependency>` configuration. By default, non-packed jar files are copied to
the `lib` subfolder of the package, while directories
(referenced by the `<path>` parameter) are copied to the root of the package.

Finally, if you are sure that a certain dependency does not contain any resources
*and* all its classes get compiled, you can disable copying of such a (non-packed)
dependency to the package by setting its `<disableCopyToPackage>` parameter to `true`.

Example of an additional dependency configuration:

```xml
<dependencies>
    <dependency>
        <path>${basedir}/target/extra-resources</path>
        <packagePath>my-extra-files</packagePath>
    </dependency>
</dependencies>
```

Here we add the `extra-resources` directory to the application classpath, telling
the plugin to place it under the `my-extra-files` directory of the package
(thus `extra-resources` directory will appear in the `my-extra-files` directory
of the final package).

Note that the only valid value of the `<pack>` property for directories is `none`,
so there is no need to set it in the respective `<dependency>` configuration.


##### Ignoring project dependencies

If you build your main artifact as a so called fat jar (using `maven-assembly-plugin`
with `jar-with-dependencies`, for example), you most likely do not need Excelsior JET
to compile any of its dependencies, because the main artifact will contain all
classes and resources of the application.
In this case, you may set the `<ignoreProjectDependencies>` plugin parameter to `true`
to disable compilation of project dependencies.
Then you will only need to set the `protect/optimize/pack` properties for your main artifact
and for the entries of the `<dependencies>` section of the plugin that are identified
with the `<path>` parameter, if any.


##### Tomcat web application dependencies

You may configure Tomcat web application dependencies as described above, except that
`<path>`, `<packagePath>`, and `<disableCopyToPackage>` parameters are not available for them.


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

**Note:** if you use the same pom.xml for all three supported platforms (Windows, OS X, and Linux),
it is recommended to use another configuration:

`<packaging>native-bundle</packaging>`

to create Excelsior Installer setups on Windows and Linux and an application bundle and installer on OS X.

Excelsior Installer setup, in turn, has the following configurations:

* `<product>`*product-name*`</product>` - default is `${project.name}`

* `<vendor>`*vendor-name*`</vendor>` -  default is `${project.organization.name}`

* `<version>`*product-version*`</version>` - default is `${project.version}`

The above parameters are also used by Windows Version Information and OS X bundle configurations.

To further configure the Excelsior Installer setup, you need to add the following configuration section:

```xml
<excelsiorInstaller>
</excelsiorInstaller>
```

that has the following configuration parameters:

* `<eula>`*end-user-license-agreement-file*`</eula>` - default is `${project.basedir}/src/main/jetresources/eula.txt`

* `<eulaEncoding>`*eula-file-encoding*`</eulaEncoding>` - default is `autodetect`. Supported encodings are US-ASCII (plain text), UTF16-LE

* `<installerSplash>`*installer-splash-screen-image*`</installerSplash>` - default is `${project.basedir}/src/main/jetresources/installerSplash.bmp`

#### Creating OS X application bundles and installers

The plugin supports the creation of OS X application bundles and installers.

To create an OS X application bundle, add the following configuration into the plugin
`<configuration>` section:

`<packaging>osx-app-bundle</packaging>`

**Note:** if you use the same pom.xml for all three supported platforms (Windows, OS X, and Linux), it is recommended to use another configuration:

`<packaging>native-bundle</packaging>`

to create Excelsior Installer setups on Windows and Linux and an application bundle and installer on OS X.

To configure the OS X application bundle, you need to add the following configuration section:

```xml
<osxBundleConfiguration>
</osxBundleConfiguration>
```

The values of most bundle parameters are derived automatically from the other parameters of your `pom.xml`.
The complete list of the parameters can be obtained
[here](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/OSXAppBundleConfig.java).

You still need to tell the plugin where the OS X icon (`.icns` file) for your bundle is located.
Do that using the `<icon>` parameter of `<osxBundleConfiguration>`, or simply place the icon file at
`${project.basedir}/src/main/jetresources/icon.icns` to let the plugin pick it up automatically.

By default, the plugin will create an OS X application bundle only,
but to distribute your application to your customers you probably need to sign it and package as an
OS X installer (`.pkg` file).
The plugin enables you to do that using the following parameters under `<osxBundleConfiguration>` section:

* `<developerId>`*developer-identity-certificate*`</developerId>` - "Developer ID Application" or "Mac App Distribution" certificate name for signing resulting OSX app bundle with `codesign` tool.
* `<publisherId>`*publisher-identity-certificate*`</publisherId>` - "Developer ID Installer" or "Mac Installer Distribution"
certificate name for signing the resulting OS X Installer Package (`.pkg` file) with the `productbuild` tool.

If you do not want to expose above parameters via `pom.xml`, you may pass them as system properties
to the `mvn` command instead, using the arguments `-Dosx.developer.id` and `-Dosx.publisher.id` respectively.

**Troubleshooting:** If you would like to test the created installer file on the same OS X system on which
it was built, you need to first remove the OS X application bundle created by the plugin and located
next to the installer. Otherwise, the installer will overwrite that existing OS X application bundle
instead of installing the application into the `Applications` folder.

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

#### Stack trace support
The Excelsior JET Runtime supports three modes of stack trace printing: `minimal`, `full`, and `none`.

In the `minimal` mode (default), line numbers and names of some methods are omitted in call stack entries,
but class names are exact.

In the `full` mode, the stack trace info includes all line numbers and method names.
However, enabling the full stack trace has a side effect &mdash; substantial growth of the resulting executable in size, approximately by 30%.

In the `none` mode, `Throwable.printStackTrace()` methods print a few fake elements.
It may result in a performance improvement, if the application throws and catches exceptions repeatedly.
Note, however, that certain third-party APIs rely on stack trace printing. One example is the Log4J API that provides logging services.

To set the stack trace support mode, use the `<stackTraceSupport>` configuration parameter:

`<stackTraceSupport>`*stack-trace-mode*`</stackTraceSupport>`

#### Method Inlining
When optimizing a Java program, the compiler often replaces method call statements with bodies of the methods
that would be called at run time. This optimization, known as method inlining, improves application performance,
especially when tiny methods, such as get/set accessors, are inlined.
However, inlining of larger methods increases code size and its impact on performance may be uncertain.
To control the aggressiveness of method inlining, use the `<inlineExpansion>` plugin parameter:

`<inlineExpansion>`*inline-expasnion-mode*`</inlineExpansion>`

The available modes are:
  `aggressive` (default), `very-aggressive`, `medium`, `low`, and `tiny-methods-only`

If you need to reduce the size of the executable, opt for the `low` or `tiny-methods-only` setting.
Note that it does not necessarily worsen application performance.

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

<a name="jvmargs"></a>
#### Defining System Properties and JVM Arguments

Unless you opted for multi-app executable generation, the resulting executable interprets
all its command line arguments as arguments of the main class you have specified.
In other words, there is no place on the application command line for an argument
setting a system property or altering JVM defaults, such as `-Dprop=value` or `-Xmx1G` .
To address this, the plugin enables you to hardwire system properties and JVM arguments
into the resulting executable using the following plugin configuration:

```xml
<jvmArgs>
    <jvmArg>-Dprop=value</jvmArg>
    <jvmArg>-jvmArg</jvmArg>
</jvmArgs>
```

This configuration also affects Test Runs and can be used with multi-app executables as well
(relieving the user from the need to specify those arguments explicitly on the command line).

System property values may contain references to the *actual* root directory of the installed package
in the form of `$(Root)`.
For example, suppose the package contains a subdirectory `AppFiles`. You may define the following property:

`-Dmy.app.files.dir=$(Root)/AppFiles`

Then you prepare a package and install it into a certain directory on the target system.
Upon application startup, the JET Runtime replaces `$(Root)` with the absolute pathname of the installation directory.
Thus, when the installed application retrieves the value of the `my.app.files.dir` property,
it gets the full path to the `AppFiles` directory on the target system.

**Note:** most of the `-XX` options recognized by the Oracle JRE are not supported,
as they are specific to that HotSpot VM. Moreover, certain `-X` options are not supported either,
for example setting `-Xbootclasspath` or `-Xms` (initial Java heap size) makes no effect.

All in all, the JET Runtime recognizes the following standard JVM arguments:

`-ea, -da, -enableassertions, -disableassertions` - assertions control

`-esa, -dsa, -enablesystemassertions, -disablesystemassertions` - system assertions control

`-Xmx` - set maximum heap size

> **Note:** Setting maximum heap size to zero (default) enables adaptive heap sizing.
> Refer to the "Memory Management" section of the "Application Considerations" chapter
> of the Excelsior JET User's Guide
> and [Knowledge Base Article #25](http://www.excelsiorjet.com/kb/25/)
> for more information.

`-Xss` - set maximum thread stack size

`-Xverify:all` - enable the strict verifier

`-XX:MaxDirectMemorySize` - set maximum memory size for direct buffers

`-javaagent:` - specify a Java Agent (for non-precompiled classes)

`-version` - print version information on startup

`-verbose:gc` - be verbose about garbage collection

The Excelsior JET Runtime also recognizes a handful of system properties controlling
its own behavior, such as `‑Djet.gc.ratio`.
For more information, consult the "Java System Properties / JET Runtime Specific Properties" section
of the "Application Considerations" chapter of the Excelsior JET User's Guide.

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

If your application requires command line arguments to run, set the `runArgs` plugin parameter in the same way as for the [Test Run](#performing-a-test-run).

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

#### Optional Runtime Components Configurations

##### Locales and charsets
Additional locales and character encoding sets that may potentially be in use in the regions
where you distribute your application can be added to the package with the following configuration:

```xml
<locales>
  <locale>Locale1</locale>
  <locale>Locale2</locale>
<locales>
```

You may specify `all` as the value of `<locale>` to add all locales and charsets at once or
`none` to not include any of them.
The available sets of locales and encodings are:

`European`, `Indonesian`, `Malay`, `Hebrew`, `Arabic`, `Chinese`, `Japanese`, `Korean`, `Thai`,
`Vietnamese`, `Hindi`, `Extended_Chinese`, `Extended_Japanese`, `Extended_Korean`, `Extended_Thai`,
`Extended_IBM`, `Extended_Macintosh`, `Latin_3`

By default, only the `European` locales are added.

##### Optional components
To include optional JET Runtime components in the package, use the following configuration:

```xml
<optRtFiles>
  <optRtFile>optRtFile1</optRtFile>
  <optRtFile>optRtFile2</optRtFile>
</optRtFiles>
```

You may specify `all` as the value of `<optRtFile>` to add all components at once or
`none` to not include any of them.

The available optional components are:

`runtime_utilities, fonts, awt_natives, api_classes, jce, accessibility, javafx, javafx-webkit, nashorn, cldr`

*Note:* by default, the plugin automatically includes the optional components which the compiler detected
   as used when building the executable(s).

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

**Fixed issue:** Java Runtime Slim-Down did not work with the `excelsior-installer` packaging type
                 due to a bug in Excelsior JET. This issue is fixed in Excelsior JET 11 Maintenance Pack 2.

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

#### Data Protection

If you do not wish constant data, such as reflection info, Java string literals, or packed resource files,
to be visible in the resulting executable, enable data protection by specifying the following configuration:

`<protectData>true</protectData>`

For more details on data protection, refer to the "Data Protection" section of
the "Intellectual Property Protection" chapter of the Excelsior JET User's Guide.

#### Additional Compiler Options and Equations
The commonly used compiler options and equations are mapped to the parameters of the plugin.
However the compiler has some advanced options and equations that you may find in the
Excelsior JET User's Guide, plus some troubleshooting settings that the Excelsior JET Support
team may suggest you to use.
You may enumerate such options using the `<compilerOptions>` configuration, for instance:

```xml
<compilerOptions>
  <compilerOption>-disablestacktrace+</compilerOption>
  <compilerOption>-inlinetolimit=200</compilerOption>
</compilerOptions>
```

These options will be appended to Excelsior JET project generated by the plugin.

**Notice:** Care must be taken with using this parameter to avoid conflicts
with other project parameters.

### Building Tomcat Web Applications
The plugin enables you to compile Apache Tomcat together with your Web applications down
to a native binary using Excelsior JET. Compared to running your
application on a conventional JVM, this has the following benefits:

* More predictable latency for your Web application, as no code de-optimizations
  may occur suddenly at run time

* Better startup time, which may be important if you need to launch a multitude of microservices
  upon updating your distributed application.

* Better initial performance that remains stable later on, which can be important
  for load balancing inside an application cluster

* Security and IP protection, as reverse engineering of sensitive application code
  becomes much more expensive and the exposure of yet unknown to you security vulnerabilities is reduced

#### Supported Tomcat versions
Excelsior JET 11 supports Apache Tomcat 5.0.x (starting from version 5.0.1), 5.5.x, 6.0.x,
and 7.0.x up to version 7.0.62. Excelsior JET 11.3 adds support for Tomcat 8.0 and Tomcat 7.0.63+ versions.

#### Usage
The plugin will treat your Maven project as a Tomcat Web application project if its `<packaging>` type is `war`.
To enable native compliation of your Tomcat Web application, you need to copy and paste the following configuration into the `<plugins>`
section of your `pom.xml` file:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.8.1</version>
	<configuration>
        <tomcatConfiguration>
             <tomcatHome></tomcatHome>
        </tomcatConfiguration>
	</configuration>
</plugin>
```

and set the `<tomcatHome>` parameter, which has to point to the *master* Tomcat installation &mdash; basically,
a clean Tomcat instance that was never launched.

You may also set the above parameter by passing the `tomcat.home` system property on the Maven command line as follows:

```
mvn jet:build -Dtomcat.home=[Tomcat-Home]
```

or set the `TOMCAT_HOME` or `CATALINA_HOME` environment variables.

**NOTICE:** The binary distributions of Tomcat that are available from http://tomcat.apache.org/ usually contain
a set of standard examples in the `webapps` directory, which are most likely not needed in your own application distribution.
So it is safe to remove them from the `webapps` directory of the master Tomcat installation, making it empty.


#### Build process
During the build of your application, the plugin first copies the master Tomcat installation to the `jet/build` subdirectory.
Then it copies your main project artifact (`.war` file) to the `webapps` subdirectory of that copy,
and compiles it all together into a native executable.

Upon success, the plugin creates a directory structure similar to that of the master Tomcat installation in the `jet/app` directory,
placing the executable into the `jet/app/bin` subdirectory. It also copies the required Excelsior JET Runtime files
into the `jet/app` directory and binds the resulting executable to that copy of the Runtime.

> Your natively compiled Tomcat application is ready for distribution at this point: you may copy
> the contents of the `jet/app` directory to another computer that has neither Excelsior JET nor
> the Oracle JRE installed, and the executable should work as expected.
> You may also run your application using standard Tomcat scripts that are placed into the resulting
> `jet/app/bin` folder by default.

Finally, the plugin packs the contents of the `jet/app` directory into
a zip archive named `${project.build.finalName}.zip` so as to aid single file re-distribution.
Other packaging types that are available for plain Java SE applications are supported for Tomcat as well (see above).

#### Tomcat configuration parameters
Most configuration parameters that are available for plain Java SE applications listed above
are also available for Tomcat web applications. There are also a few Tomcat-specific configuration parameters that
you may set within the `<tomcatConfiguration>` parameters block:

* `<warDeployName>` - the name of the war file to be deployed into Tomcat.
   By default, Tomcat uses the name of the war file as the context path of the respective web application.
   If you need your web application to be on the "/" context path, set `<warDeployName>` to `ROOT` value.

* `<hideConfig>` - if you do not want your end users to inspect or modify the Tomcat configuration files
  located in `<tomcatHome>/conf/`, set this plugin parameter to `true`
  to have those files placed inside the executable, so they will not appear in the `conf/` subdirectory
  of end user installations of your Web application.

    **Important:**  For Tomcat to start your Web applications with hidden configuration files,
    you need to either mark the `conf/tomcat-users.xml` file read-only, or move it away from
    the `conf/` directory. If you opt for the latter, that file would remain visible, of course.

    You can do the above respectively by adding the attribute `readonly="true"` to the tag
    `<Resource name="UserDatabase">` in the `conf/server.xml` file of the master Tomcat installation,
    or modifying the `pathname` attribute of that tag. For example:
```
<Resource name="UserDatabase" auth="Container"
 type="org.apache.catalina.UserDatabase"
 description="User database that can be updated and saved"
 factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
 pathname="conf/tomcat-users.xml"
 readonly="true"/>
```
  Also, you would likely want to pre-deploy the XML descriptors of your Web applications
  to `conf/<Engine>/<Host>`. Otherwise, Tomcat will extract those XML files
  from applications and place them in the `conf/` directory on startup,
  thus negating the effect of hiding.

* `<genScripts>` - you may continue to use the standard Tomcat scripts such as `bin/startup`
  and `bin/shutdown` with the natively compiled Tomcat, as by default
  the respective scripts are created in `jet/app/bin` along with the executable.
  However, if you are going to launch the created executable directly, you may set
  the `<genScripts>` parameter to `false`.

* `<installWindowsService>` - if you opt for `excelsior-installer` packaging for Tomcat on Windows,
  the installer will registers the Tomcat executable as a Windows service by default.
  You may set this parameter to `false` to disable that behavior.
  Otherwise, you may configure Windows Service-specific parameters for the Tomcat service by adding
  a `<windowsServiceConfiguration>` section as described [here](#windows-service-configuration).

    **Note:** This functionality is only available in Excelsior JET 11.3 and above.

#### Multiple Web applications and Tomcat installation configuration
Excelsior JET can also compile multiple Web applications deployed onto a single Tomcat instance.

To do this with the help of this plugin, you need to do the following:

* Determine what is the *last* Web application in your build process and add the above Excelsior JET
  plugin configuration to its Maven project.

* To the projects of all other Web applications, add a file copy operation that would copy the
  final `.war` artifact into the `webapps` subdirectory of the master Tomcat installation of
  your last Web application project.

This way, the Excelsior JET AOT compiler will pick up all the Web applications that were built earlier
and compile them into the same executable as the last one.

If you need to add or change some Tomcat configurations specific to your applications,
such as DB configurations, simply make the respective changes in the master Tomcat installation.
Similarly, if you need any additional files included in the resulting installation package, you can
place them in the master Tomcat installation as well: the plugin will copy them into the final package
automatically.

#### Test Run of a Tomcat Web application

You can launch your Tomcat Web application on Excelsior JET JVM using a JIT compiler
before pre-compiling it to native code using the `jet:testrun` Mojo the same way as with plain Java SE applications.

However, please note that a running Tomcat instance would not terminate until you run its standard `shutdown` script.
Technically, you can terminate it using <key>Ctrl-C</key>, but that would terminate the entire Maven build and would not constitute a correct Tomcat termination.
So it is recommended to use the standard Tomcat `shutdown` script for correct Tomcat termination at the end of a Test Run.
You may launch it from any standard Tomcat installation.

### Invocation Dynamic Libraries

To create a dynamic library callable from applications written in a non-JVM language
instead of a runnable executable, add the following Excelsior JET Maven plugin configuration:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.9.0</version>
	<configuration>
        <appType>invocation-dynamic-library</appType>
	</configuration>
</plugin>
```

Using Invocation DLLs is a bit tricky.
Like any other JVM, Excelsior JET executes Java code in a special isolated context
to correctly support exception handling, garbage collection, and so on.
That is why Java methods cannot be directly invoked from a foreign environment.
Instead, you have to use the standard Java SE platform APIs, specifically the Invocation API
and Java Native Interface (JNI).
See `samples/Invocation` in your Excelsior JET installation directory for detailed examples.

#### Test Run for Dynamic Libraries

To test an invocation dynamic library, you may set
a "test" `<main>` class in the plugin configuration. The `main` method of that class
should in turn call methods that are subject for usage from a non-JVM language.

### Windows Services

A Windows service, formerly known as NT service, is a special long-running process that may be launched during
operating system bootstrap.
An essential feature of a service is the ability to run even if no user is logged on to the system.
Examples of services are FTP/HTTP servers, print spoolers, file sharing, etc.
Typically, Windows services have not a user interface but are managed through
the Services applet of the Windows Control Panel, or a separate application or applet.
Using the standard Services applet, a user can start/stop, and, optionally, pause/continue a previously installed service.
The common way for a service to report a warning or error is recording an event into the system event log.
The log can be inspected using the Event Viewer from Administrative Tools.
A service program is a conventional Windows executable associated with a unique system name
using which it can be installed to/removed from the system. A service can be installed as automatic
(to be launched at system bootstrap) or manual (to be activated later by a user
through the start button in the Windows Control Panel/Services).

#### Adding dependency on the Excelsior JET WinService API

A Windows service program must register a callback routine (so called control handler)
that is invoked by the system on service initialization, interruption, resume, etc.
With Excelsior JET, you achieve this functionality by implementing a subclass of
`com.excelsior.service.WinService` of Excelsior JET WinService API and specifying it
as the main class of the plugin configuration.
The JET Runtime will instantiate that class on startup and translate calls to the callback routine into calls
of its respective methods, collectively called handler methods. For more details, refer to the
"Windows Services" Chapter of the Excelsior JET User's Guide.

To compile your implementation of `WinService` to Java bytecode you will need to reference
the Excelsior JET WinService API from your Maven project. For that, add the following dependency
to the `<dependencies>` section  of your `pom.xml` file:

```xml
<dependency>
    <groupId>com.excelsiorjet</groupId>
    <artifactId>excelsior-jet-winservice-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

#### Windows Service Configuration

To create a Windows Service, add the following Excelsior JET Maven plugin configuration:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.9.0</version>
	<configuration>
        <appType>windows-service</appType>
        <main>*service-main*</main>
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
</plugin>
```

Where:

* `<main>` - a class extending the `com.excelsior.service.WinService` class of the Excelsior WinService API.

* `<name>` -  the system name of the service. It is used to install, remove and otherwise manage the service.
  It can also be used to recognize messages from this service in the system event log.
  This name is set during the creation of the service executable.
  By default, the value of the `<outputName>` parameter is substituted.

* `<displayName>` - the descriptive name of the service.
  It is shown in the Event Viewer system tool and in the Services applet of the Windows Control Panel.
  By default, the value of the `<name>` paramter of the `<windowsServiceConfiguration>` section is used
  as the display name.

* `<description>` - the user description of the service. It must not exceed 1000 characters.

* `<arguments>` - command line arguments that shall be passed to the service upon startup.

* `<logOnType>` - specifies an account to be used by the service.
  Valid values are: `local-system-account` (default), `user-account`.
  - `local-system-account` - run the service under the built-in system account.
  - `user-account` - run the service under a user account.
     When installing the package, the user will be prompted for an account name
     and password necessary to run the service.

* `<allowDesktopInteraction>` - specifies if the service needs to interact with the system desktop,
  e.g. open/close other windows, etc. This option is only available if the service is installed
  under the local system account.

* `<startupType>` -  specifies how to start the service. Valid values are `automatic` (default), `manual`, `disabled`.
  - `automatic` - specifies that the service should start automatically when the system starts.
  - `manual` - specifies that a user or a dependent service can start the service.
     Services with Manual startup type do not start automatically when the system starts.
  - `disabled` - prevents the service from being started by the system, a user, or any dependent service.

* `<startServiceAfterInstall>` -  specifies if the service should be started immediately after installation.
   Available only for the `excelsior-installer` `<packaging>` parameter.

*  `<dependencies>` - list of other service names on which the service depends.

Based on the above parameters, the plugin will create the `install.bat`/`uninstall.bat` scripts
in the `target/jet/app` directory to enable you to install and uninstall the service manually to test it. 
If you opt for the `excelsior-installer` packaging type, the service will be registered automatically
during package installation.

**Note:** The plugin does not support creation of Excelsior Installer packages for Windows Services
using Excelsior JET 11.0, as the respective functionality is missing in the `xpack` utility.
It only works for Excelsior JET 11.3 and above.

**Note:** You may build a multi-app executable runnable as both plain application and Windows service.
For that set `<appType>` parameter to "windows-service" and `<multiApp>` parameter to `true`.
Please note that in this case, Windows Service arguments will have the syntax of multi-app executables,
so if you would need to just pass usual arguments to your service and not pass VM arguments, do not forget
to add "-args" as the first argument of your service `<arguments>` list.

#### Test Run of Windows Services

Unfortunately, a service cannot be resistered in the system before its compilation,
so a fully functional Test Run is not available for Windows Services. However, it is recommended
to add a `public static void main(String args[])` method to your Windows Service main class
to test your basic application functionality with Test Run.

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

## Release Notes

Version 0.9.0 (??-Nov-2016)
Invocation dynamic libraries and Windows services support.

Version 0.8.1 (28-Oct-2016)

The release supports [Excelsior JET Embedded 11.3 for Linux/ARM](https://www.excelsiorjet.com/embedded/).

Version 0.8.0 (20-Oct-2016)

The release adds the capability to set Excelsior JET-specific properties for project dependencies,
such as code protection, selective optimization, and resource packing.

Version 0.7.2 (19-Aug-2016)

This release adds the capability to pass command-line arguments to the application during startup profiling
and the test run.

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

## Roadmap

Even though we are going to base the plugin development on your feedback in the future, we have our own short-term plan as well.
So the next few releases will add the following features:

* Dynamic libraries and Windows services support.
* Multi-component support: building dependencies into separate native libraries
                           to reuse them across multiple Maven project builds
                           so as to reduce overall compilation time
* Code signing.

Note that the order of appearance of these features is not fixed and can be adjusted based on your feedback.
