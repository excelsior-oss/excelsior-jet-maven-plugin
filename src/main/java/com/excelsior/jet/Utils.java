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

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static String getExeFileExtension() {
        return isWindows() ? ".exe" : "";
    }

    public static String mangleExeName(String exe) {
        return exe + getExeFileExtension();
    }

    public static void cleanDirectory(File f){
        if (f.isDirectory()) {
            File farr[] = f.listFiles();
            if (farr == null) return;
            for (int i=farr.length-1; i>=0; i--) cleanDirectory(farr[i]);
        }
        f.delete();
    }

    public static void compressZipfile(File sourceDir, File outputFile) throws IOException, FileNotFoundException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
        compressDirectoryToZipfile(sourceDir.getAbsolutePath(), sourceDir.getAbsolutePath(), zipFile);
        IOUtils.closeQuietly(zipFile);
    }

    private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException, FileNotFoundException {
        for (File file : new File(sourceDir).listFiles()) {
            if (file.isDirectory()) {
                compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "")  + File.separator + file.getName());
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(sourceDir + File.separator +  file.getName());
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
            }
        }
    }

    public static boolean isEmpty(String s) {
   		return (s == null) || s.isEmpty();
   	}
}
