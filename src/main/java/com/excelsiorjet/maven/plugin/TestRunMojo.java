/*
 * Copyright (c) 2015, Excelsior LLC.
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
import com.excelsiorjet.api.log.Log;
import com.excelsiorjet.api.tasks.JetBuildTask;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.tasks.JetTaskFailureException;
import com.excelsiorjet.api.tasks.TestRunTask;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;

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
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo( name = "testrun", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class TestRunMojo extends AbstractJetMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ExcelsiorJet excelsiorJet = new ExcelsiorJet(jetHome);
            JetProject jetProject = getJetProject();
            new TestRunTask(excelsiorJet, jetProject).execute();
        } catch (JetTaskFailureException | JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (IOException | CmdLineToolException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

}
