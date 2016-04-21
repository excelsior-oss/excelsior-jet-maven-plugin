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
package com.excelsiorjet;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Utils {

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").contains("Linux");
    }

    public static boolean isOSX() {
        return System.getProperty("os.name").contains("OS X");
    }

    public static boolean isUnix() {
        return isLinux() || isOSX();
    }

    public static String getExeFileExtension() {
        return isWindows() ? ".exe" : "";
    }

    public static String mangleExeName(String exe) {
        return exe + getExeFileExtension();
    }

    public static void cleanDirectory(File f) throws IOException {
        Files.walkFileTree(f.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            private void deleteFile(File f) throws IOException {
                if (!f.delete()) {
                    if (f.exists()) {
                        throw new IOException(Txt.s("Utils.CleanDirectory.Failed", f.getAbsolutePath()));
                    }
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                deleteFile(file.toFile());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if (file.toFile().exists()) {
                    throw new IOException(Txt.s("Utils.CleanDirectory.Failed", f.getAbsolutePath()));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                deleteFile(dir.toFile());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copyFile(Path source, Path target) throws IOException {
        if (!target.toFile().exists()) {
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        } else if (source.toFile().lastModified() != target.toFile().lastModified()) {
            //copy only files that were changed
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }

    }

    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path subfolder, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(subfolder)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(sourceFile));
                copyFile(sourceFile, targetFile);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path sourceFile, IOException e) throws IOException {
                throw new IOException(Txt.s("Utils.CannotCopyFile.Error", sourceFile.toString(), e.getMessage()), e);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path source, IOException ioe) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static boolean isEmpty(String s) {
        return (s == null) || s.isEmpty();
    }
}
