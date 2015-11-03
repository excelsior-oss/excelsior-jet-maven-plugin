Excelsior JET Maven Plugin
=====
*Excelsior JET Maven Plugin* provides a way of building Java (JVM-based) applications 
with [Excelsior JET](http://excelsiorjet.com) from within Maven.

### Prerequisites
Before using this plugin, you need to install Excelsior JET. 
You may find fully functional evaluation version of Excelsior JET [here](http://www.excelsiorjet.com/evaluate). 
It's free for evaluation purposes and has the only limitation that it expires with all built applications within 90 days.
Excelsior JET does not yet support cross-compilation so you need to build your application on every target platform 
separately. Supported platforms are Windows (32-bit and 64-bit), Linux (32-bit and 64-bit) and OS X (64-bit).
  
### Overview
Currently the plugin supports very basic functionality.
It builds only *plain Java SE applications*, i.e. applications that have main class and all dependencies are resided in
application classpath at run-time. So you can use this plugin, if your application can run 
with the following Java command line:
    
```
java -cp [dependencies-list] [main class]
```
 
As a result of using this plugin, you get optimized native executable for a target platform 
with all required Excelsior JET runtime files. The executable starts and works faster, does not depend on the JRE, 
and is as difficult to reverse engineer as if it was written in C++.
    
The product itself supports much more features than this plugin, however we plan to cover all the features in the future.

### Usage
You need to copy&paste the following configuration into your pom.xml plugins section:

```xml
<plugin>
	<groupId>com.excelsior</groupId>
	<artifactId>excelsior-jet-maven-plugin</artifactId>
	<version>0.1.0</version>
	<configuration>
		<mainClass></mainClass>
	</configuration>
</plugin>
```

and use the following command line to build the application:

```
mvn package jet:build
```

You need to supply "package" argument to build your main jar.

### Excelsior JET installation directory lookup strategy 
The plugin will not run if it does not find Excelsior JET installation directory.
The plugin uses the following strategy to find it:

- First, you may directly specify it in *configuration* section of the plugin using *jetHome* property.  
- You may also supply *-Djet.home* VM property with Maven command line as:
```
mvn package jet:build -Djet.home=[JETInstallDir]
```
- Or you may set *JET_HOME* OS environment variable
- If none of above is set, the plugin looks for proper Excelsior JET installation in PATH environment variable. 
So if you have the only Excelsior JET installed, the plugin should be able to find it on Windows right away 
or after applying "setenv" script on Linux and OS X.      
   

### Build process
The build is performed in "jet" subdirectory of Maven target build directory.
First main application jar is copied to "jet/build" directory and all project runtime dependencies are copied to
"jet/build/lib". Then the Excelsior JET AOT compiler is invoked to build all the jars into native executable.
Then the resulting executable and required Excelsior JET runtime files are linked together and copied 
into "jet/app" directory. This directory is eligible for further re-distribution: 
you may copy it into another PCs that may not have Excelsior JET or Oracle JRE installed 
and the executable should run as expected there. Finally, the plugin archives the directory into 
`${project.build.finalName}.zip` zip archive to allow single file re-destribution. 
In the future, the plugin will also support Windows installer and Mac OS app bundles creation.             


