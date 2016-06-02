package com.excelsiorjet.maven.plugin;

import com.excelsiorjet.api.Artifact;

import java.io.File;

public class MavenArtifact implements Artifact {

    private final org.apache.maven.artifact.Artifact mavenArtifact;

    public MavenArtifact(org.apache.maven.artifact.Artifact mavenArtifact) {
        this.mavenArtifact = mavenArtifact;
    }

    @Override
    public File getFile() {
        return mavenArtifact.getFile();
    }

    @Override
    public String getGroupId() {
        return mavenArtifact.getGroupId();
    }
}
