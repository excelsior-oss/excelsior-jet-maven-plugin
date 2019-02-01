package com.excelsiorjet.maven.plugin;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Mojo for performing a Test Run before building the application.
 * Running your Java application before optimization helps Excelsior JET:
 * <ul>
 *  <li>
 *      Verify that your application can run on the Excelsior JET JVM flawlessly
 *      (i.e. it has no implicit dependencies on the Oracle JVM implementation
 *      and your Maven project has no configuration issues specific to Excelsior JET).
 *  </li>
 *  <li>
 *      Collect profile information to optimize your app more effectively.
 *  </li>
 *  <li>
 *      Enable application startup time optimization.
 *      Performing a Test Run can reduce the startup time by a factor of up to two.
 *  </li>
 * </ul>
 * To perform a Test Run, issue the following Maven command:
 * <p>
 * <code>
 *     mvn jet:testrun
 * </code>
 * </p>
 * <p>
 * It is recommended to commit the collected profiles (.usg, .startup) to VCS so as to
 * enable the plugin to re-use them during subsequent builds without performing the Test Run.
 * The profiles are placed to {@code ${project.basedir}/src/main/jetresources} by default.
 * </p>
 *
 *  Note: During a Test Run, the application is executed in a special profiling mode,
 *        so disregard its modest start-up time and performance.
 *
 *  Note: Forks Maven lifecycle. Use "jet-testrun" goal for Maven {@code <goal>} declarations.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "testrun", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CliTestRunMojo extends TestRunMojo {
}
