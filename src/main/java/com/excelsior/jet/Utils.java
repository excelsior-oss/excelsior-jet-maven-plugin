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
package com.excelsior.jet;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static boolean isEmpty(String s) {
        return (s == null) || s.isEmpty();
    }
}
