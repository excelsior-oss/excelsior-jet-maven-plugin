package com.excelsiorjet.maven.plugin;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Main Mojo for building Java (JVM) applications with Excelsior JET.
 * Forks Maven lifecycle. Use "jet-build" goal ({@link BuildMojo}) for Maven {@code <goal>} declarations.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CliBuildMojo extends BuildMojo {
}
