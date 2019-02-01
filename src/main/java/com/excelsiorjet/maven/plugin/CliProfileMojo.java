package com.excelsiorjet.maven.plugin;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Mojo for profiling Java (JVM) applications with Excelsior JET.
 * Forks Maven lifecycle. Use "jet-profile" goal ({@link BuildMojo}) for Maven {@code <goal>} declarations.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "profile", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CliProfileMojo extends ProfileMojo {
}
