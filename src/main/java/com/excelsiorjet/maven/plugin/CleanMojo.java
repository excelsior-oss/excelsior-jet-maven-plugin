package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.api.ExcelsiorJet;
import com.excelsiorjet.api.JetHomeException;
import com.excelsiorjet.api.tasks.JetCleanTask;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.tasks.JetTaskFailureException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;

/**
 * Cleans up Excelsior JET Project database (PDB) directory for the current project.
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "clean", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CleanMojo extends AbstractBuildMojo {

    @Override
    public void execute() throws MojoFailureException {
        try {
            JetProject jetProject = getJetProject();
            ExcelsiorJet excelsiorJet = new ExcelsiorJet(jetHome);
            new JetCleanTask(jetProject, excelsiorJet).execute();
        } catch (JetTaskFailureException | JetHomeException | IOException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

}
