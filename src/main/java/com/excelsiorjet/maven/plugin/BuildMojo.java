/*
 * Copyright (c) 2015-2017 Excelsior LLC.
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
import com.excelsiorjet.api.util.Txt;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.IOException;

import static com.excelsiorjet.api.log.Log.logger;
import static com.excelsiorjet.api.util.Txt.s;

/**
 * Main Mojo for building Java (JVM) applications with Excelsior JET to use inside {@code <goal>} Maven declarations
 * (does not fork Maven lifecycle).
 *
 * @author Nikita Lipsky
 */
@Mojo(name = "jet-build", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class BuildMojo extends AbstractBuildMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        init();
        if (!isSupportedPackaging()) {
            logger.warn(s("JetMavenPlugin.UnsupportedPackaging.Mojo.Warning", project.getPackaging(), project.getName()));
            return;
        }
        try {
            JetProject jetProject = getJetProject();
            ExcelsiorJet excelsiorJet = new ExcelsiorJet(jetHome);
            new JetBuildTask(excelsiorJet, jetProject, false).execute();
        } catch (JetTaskFailureException | JetHomeException  e) {
            throw new MojoFailureException(e.getMessage());
        } catch (CmdLineToolException | IOException e) {
            logger.debug("JetTask execution error", e);
            logger.error(e.getMessage());
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
