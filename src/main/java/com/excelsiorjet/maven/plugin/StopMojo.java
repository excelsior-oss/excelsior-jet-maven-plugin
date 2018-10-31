/*
 * Copyright (c) 2017 Excelsior LLC.
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
import com.excelsiorjet.api.JetHomeException;
import com.excelsiorjet.api.cmd.CmdLineToolException;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.tasks.JetTaskFailureException;
import com.excelsiorjet.api.tasks.StopTask;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import static com.excelsiorjet.api.log.Log.logger;
import static com.excelsiorjet.api.util.Txt.s;

/**
 * Mojo for stopping applications that were run by {@link TestRunMojo}, {@link RunMojo}, {@link ProfileMojo}.
 *
 * @author Nikita Lipsky
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class StopMojo extends AbstractJetMojo {

    @Override
    public void execute() throws MojoFailureException {
        init();
        if (!isSupportedPackaging()) {
            logger.warn(s("JetMavenPlugin.UnsupportedPackaging.Mojo.Warning", project.getPackaging(), project.getName()));
            return;
        }
        try {
            JetProject jetProject = getJetProject();
            ExcelsiorJet excelsiorJet = new ExcelsiorJet(jetHome);
            new StopTask(excelsiorJet, jetProject).execute();
        } catch (JetTaskFailureException | JetHomeException  e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

}
