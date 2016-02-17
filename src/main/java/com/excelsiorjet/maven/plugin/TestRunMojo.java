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

import com.excelsiorjet.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private void copyExtraPackageFiles(File buildDir) {
        // We could just use Maven FileUtils.copyDirectory method but it copies a directory as a whole
        // while here we copy only those files that were changed from previous build.
        Path target = buildDir.toPath();
        Path source = packageFilesDir.toPath();
        try {
            Files.walkFileTree(source, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path subfolder, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(subfolder)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = target.resolve(source.relativize(sourceFile));
                    if (!targetFile.toFile().exists()) {
                        Files.copy(sourceFile, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
                    } else if (sourceFile.toFile().lastModified() != targetFile.toFile().lastModified()) {
                        //copy only files that were changed
                        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path sourceFile, IOException e) throws IOException {
                    getLog().warn(Txt.s("TestRunMojo.CannotCopyPackageFile.Warning", sourceFile.toString(), e.getMessage()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path source, IOException ioe) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            getLog().warn(Txt.s("TestRunMojo.ErrorWhileCopying.Warning", source.toString(), target.toString(), e.getMessage()));
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        JetHome jetHome = checkPrerequisites();

        // creating output dirs
        File buildDir = createBuildDir();

        List<Dependency> dependencies = copyDependencies(buildDir, mainJar);

        if (packageFilesDir.exists()) {
            //application may access custom package files at runtime. So copy them as well.
            copyExtraPackageFiles(buildDir);
        }

        mkdir(execProfilesDir);

        XJava xjava = new XJava(jetHome);
        try {
            xjava.addTestRunArgs(new TestRunExecProfiles(execProfilesDir, execProfilesName))
                    .withLog(getLog())
                    .workingDirectory(buildDir);
        } catch (JetHomeException e) {
            throw new MojoFailureException(e.getMessage());
        }

        //add jvm args substituting $(Root) occurences with buildDir
        xjava.addArgs(Stream.of(jvmArgs)
                .map(s -> s.replace("$(Root)", buildDir.getAbsolutePath()))
                .collect(Collectors.toList())
        );

        xjava.arg("-cp");
        xjava.arg(String.join(File.pathSeparator,
                dependencies.stream().map(d -> d.dependency).collect(Collectors.toList())));
        xjava.arg(mainClass);
        try {
            String cmdLine = xjava.getArgs().stream()
                    .map(arg -> arg.contains(" ") ? '"' + arg + '"' : arg)
                    .collect(Collectors.joining(" "));

            getLog().info(Txt.s("TestRunMojo.Start.Info", cmdLine));

            int errCode = xjava.execute();
            String finishText = Txt.s("TestRunMojo.Finish.Info", errCode);
            if (errCode != 0) {
                getLog().warn(finishText);
            } else {
                getLog().info(finishText);
            }
        } catch (CmdLineToolException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }
}
