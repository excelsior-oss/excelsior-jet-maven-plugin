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

import java.io.File;

/**
 * Encapsulates JET home directory.
 * 
 * @author Nikita Lipsky
 */
public class JetHome {

    private static final int MIN_SUPPORTED_JET_VERSION = 1100;

    private static String MARKER_FILE_PREFIX = "jet";
    private static String MARKER_FILE_SUFFIX = ".home";

    public static final String BIN_DIR = "bin";
    private String jetHome;

    /**
     * @param jetHome jet home directory
     * @return version of JET in 4 figures like "1100", "1050".
	 *         If it is not JET returns -1
     */
    private static int getJetVersion(String jetHome) {
        File[] files = new File(jetHome, BIN_DIR).listFiles();
        if (files == null)
            return -1;
        for (File f : files) {
            String fname = f.getName();
            if (fname.startsWith(MARKER_FILE_PREFIX) && fname.endsWith(MARKER_FILE_SUFFIX)) {
                try {
                     // expected file name: jet<version>.home
                    return Integer.parseInt(fname.substring(MARKER_FILE_PREFIX.length(), fname.length() - MARKER_FILE_SUFFIX.length()));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private static boolean isSupportedJetVersion(String jetHome) {
        return getJetVersion(jetHome) >= MIN_SUPPORTED_JET_VERSION;
    }

    private static void checkJetHome(String jetHome, String errorPrefix) throws JetHomeException {
        if (!isJetDir(jetHome)) {
            throw new JetHomeException(Txt.s("JetHome.BadJETHomeDir.Error", errorPrefix, jetHome));
        }
        if (!isSupportedJetVersion(jetHome)) {
            throw new JetHomeException(Txt.s("JetHome.UnsupportedJETHomeDir.Error", errorPrefix, jetHome));
        }
    }

    /**
     * Constructs jet home object by given JET home directory.
     * 
     * @param jetHome jet home directory
     * @throws JetHomeException if supplied directory is not JET Directory or is not supported
     */
    public JetHome(String jetHome) throws JetHomeException {
        if (Utils.isUnix() && jetHome.startsWith("~/")) {
            // expand "~/" on Unixes
            jetHome = System.getProperty("user.home") + jetHome.substring(1);
        }
        checkJetHome(jetHome, Txt.s("JetHome.PluginParameter.Error.Prefix"));
        this.jetHome = jetHome;
    }

    private boolean trySetJetHome(String jetHome, String errorPrefix) throws JetHomeException {
        if (!Utils.isEmpty(jetHome)) {
            checkJetHome(jetHome, errorPrefix);
            this.jetHome = jetHome;
            return true;
        }
        return false;
    }

    /**
     * Detects jet home directory by the following algorithm:
     * <ul>
     *   <li> first it checks -Djet.home system property, if it is set, it takes jet home from it </li>
     *   <li> then it checks JET_HOME environment variable, if it is set, it takes jet home from it </li>
     *   <li> finally it scans PATH environment variable for appropriate jet home</li>
     * </ul>
     * @throws JetHomeException if -Djet.home or JET_HOME does not contain jet home, 
     *                          or there no jet home in PATH
     */
    public JetHome() throws JetHomeException {
        // try to detect jet home
        if (!trySetJetHome(System.getProperty("jet.home"), Txt.s("JetHome.ViaVMProp.Error.Prefix"))
                && !trySetJetHome(System.getenv("JET_HOME"), Txt.s("JetHome.ViaEnvVar.Error.Prefix"))) {
            // try to detect jetHome via path
            String path = System.getenv("PATH");
            for (String p : path.split(File.pathSeparator)) {
                if (isJetBinDir(p)) {
                    String jetPath = new File(p).getParentFile().getAbsolutePath();
                    if (isSupportedJetVersion(jetPath)) {
                        jetHome = jetPath;
                        return;
                    }
                }
            }
            throw new JetHomeException(Txt.s("JetHome.JetNotFound.Error"));
        }
    }

    public String getJetHome() {
        return jetHome;
    }

    public String getJetBinDirectory() {
        return getJetBinDirectory(getJetHome());
    }

    private static boolean isJetBinDir(String jetBin) {
        return new File(jetBin, "jet.config").exists() &&
               new File(jetBin, Utils.mangleExeName(JetCompiler.JET_COMPILER)).exists() &&
               new File(jetBin, Utils.mangleExeName(JetPackager.JET_PACKAGER)).exists() ;
    }

    private static String getJetBinDirectory(String jetHome) {
        return jetHome + File.separator + BIN_DIR;
    }

    private static boolean isJetDir(String jetHome) {
        return isJetBinDir(getJetBinDirectory(jetHome));
    }
}
