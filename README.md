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
Excelsior JET runtime files.
    
Excelsior JET supports many more features than this plugin.
We plan to cover all those features in the future.


### Usage

You need to copy and paste the following configuration into the `<plugins>` section of
your `pom.xml` file:

```xml
<plugin>
	<groupId>com.excelsiorjet</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.1.0</version>
	<configuration>
		<mainClass></mainClass>
	</configuration>
</plugin>
```

and use the following command line to build the application:

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

### Configurations other than `<mainСlass>`
For a complete list of parameters look into JavaDoc of `@Parameter` field declarations
of [JetMojo](https://github.com/excelsior-oss/excelsior-jet-maven-plugin/blob/master/src/main/java/com/excelsiorjet/maven/plugin/JetMojo.java)
class. Most of them have default value derived from your pom.xml project
such as `<outputName>` parameter specifying resulting executable name.

There are also two useful Windows-specific configuration parameters:

`<hideConsole>true</hideConsole>` – hide console

`<icon>icon-file</icon>` – set executable icon (in Windows .ico format)

It is recommended to place the executable icon into VCS, and if you place it to
`${project.basedir}/src/main/jetresources/icon.ico` you do not need to explicitly specify it
in the configuration. In the future, we will use the location
`${project.basedir}/src/main/jetresources` for other JET specific resource files
(such as EULA for Excelsior Installer setup).

### Build process

The native build is performed in the `jet` subdirectory of the Maven target build directory.
First, the plugin copies the main application jar to the `jet/build` directory,
and copies all its run time dependencies to `jet/build/lib`.
Then it invokes the Excelsior JET AOT compiler to compile all those jars into a native executable.
Upon success, it copies that executable and the required Excelsior JET Runtime files
into the `jet/app` directory, and binds the executable to that copy of the Runtime.

> Your natively compiled application is ready for distribution at this point: you may copy
> contents of the `jet/app` directory to another computer that has neither Excelsior JET nor
> the Oracle JRE installed, and the executable should work as expected.

Finally, the plugin packs the contents of the `jet/app` directory into
a zip archive named `${project.build.finalName}.zip` so as to aid single file re-distribution.

In the future, the plugin will also support the creation of Windows installers
and OS X app bundles.

## Sample Project

To demonstrate the process and result of plugin usage, we have forked the [JavaFX VNC Client](https://github.com/comtel2000/jfxvnc) project on GitHub, added the Excelsior JET plugin to its `pom.xml` file, and run it through Maven to build native binaries for three platforms.

You can download the binaries from here:

* [Windows (32-bit, 46MB)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-windows-x86.zip)
* [OS X (64-bit, 45MB)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-osx-amd64.zip)
* [Linux (64-bit, 45MB)](http://www.excelsior-usa.com/download/jet/maven/jfxvnc-ui-1.0.0-linux-amd64.zip)

or clone [the project](https://github.com/pjBooms/jfxvnc) and build it yourself:

```
    git clone https://github.com/pjBooms/jfxvnc
    cd jfxvnc/ui
    mvn jet:build
```

## Roadmap

Even though we are going to base the plugin development on your feedback in the future, we have our own short-term plan as well.
So the next few releases will add the following features:

* Packaging the natively compiled application with Excelsior Installer for Windows and Linux. Adding version information to Windows executables will also be also supported.
* Startup time optimizations. These optimizations are only possible in the presence of a startup execution profile, so the plugin will have the ability to gather that profile.
* [Java Runtime Slim-Down](http://www.excelsiorjet.com/solutions/java-download-size).
* Mapping of compiler and packager options to plugin configuration parameters.
* Creation of Mac OS X application bundles.
* Code signing.
* Tomcat Web Applications support.

Note that the order of appearance of these features is not fixed and can be adjusted based on your feedback.

