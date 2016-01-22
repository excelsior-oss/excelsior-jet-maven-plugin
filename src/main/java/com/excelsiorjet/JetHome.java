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

import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;

/**
 * Encapsulates the Excelsior JET home directory.
 * 
 * @author Nikita Lipsky
 */
public class JetHome {

    private static final int MIN_SUPPORTED_JET_VERSION = 1100;

    private static final String MARKER_FILE_PREFIX = "jet";
    private static final String MARKER_FILE_SUFFIX = ".home";

    private static final String BIN_DIR = "bin";

    private String jetHome;

    private JetEdition edition;

    private boolean is64;

    /**
     * @param jetHome Excelsior JET home directory
     * @return Excelsior JET version "multiplied by 100" (i.e. 1150 means version 11.5),
     *         or -1 if {@code jetHome} does not point to an Excelsior JET home directory
     */
    private static int getJetVersion(String jetHome) {
        File[] files = new File(jetHome, BIN_DIR).listFiles();
        if (files == null) {
            return -1;
        }
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

    private void checkAndSetJetHome(String jetHome, String errorPrefix) throws JetHomeException {
        if (!isJetDir(jetHome)) {
            throw new JetHomeException(Txt.s("JetHome.BadJETHomeDir.Error", errorPrefix, jetHome));
        }
        if (!isSupportedJetVersion(jetHome)) {
            throw new JetHomeException(Txt.s("JetHome.UnsupportedJETHomeDir.Error", errorPrefix, jetHome));
        }
        this.jetHome = jetHome;
    }

    /**
     * Constructs a JetHome object given a filesystem location supposedly containing a copy of Excelsior JET
     * 
     * @param jetHome Excelsior JET home directory pathname
     * @throws JetHomeException if {@code jetHome} does not point to a supported version of Excelsior JET
     */
    public JetHome(String jetHome) throws JetHomeException {
        if (Utils.isUnix() && jetHome.startsWith("~/")) {
            // expand "~/" on Unixes
            jetHome = System.getProperty("user.home") + jetHome.substring(1);
        }
        checkAndSetJetHome(jetHome, Txt.s("JetHome.PluginParameter.Error.Prefix"));
    }

    private boolean trySetJetHome(String jetHome, String errorPrefix) throws JetHomeException {
        if (!Utils.isEmpty(jetHome)) {
            checkAndSetJetHome(jetHome, errorPrefix);
            return true;
        }
        return false;
    }

    /**
     * Attempts to locate an Excelsior JET home directory using the following algorithm:
     * <ul>
     *   <li> If the jet.home system property is set, use its value</li>
     *   <li> Otherwise, if the JET_HOME environment variable is set, use its value</li>
     *   <li> Otherwise scan the PATH environment variable for a suitable Excelsior JET installation</li>
     * </ul>
     * @throws JetHomeException if either jet.home or JET_HOME is set, but does not point to a suitable
     *                          Excelsior JET installation, or if no such installation could be found in PATH
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

    private String obtainVersionString() throws JetHomeException {
        try {
            String[] result = {null};
            CmdLineTool jetCompiler = new JetCompiler(this).withLog(new SystemStreamLog() {
                public void info(CharSequence info) {
                    if (result[0] == null) {
                        String line = info.toString();
                        if (line.contains("Excelsior JET ")) {
                            result[0] = line;
                        }
                    }
                }

                public void error(CharSequence charSequence) {
                }
            });
            if ((jetCompiler.execute() != 0) || result[0] == null)  {
                throw new JetHomeException(Txt.s("JetHome.UnableToDetectEdition.Error"));
            }
            return result[0];
        } catch (CmdLineToolException e) {
            throw new JetHomeException(e.getMessage());
        }
    }

    private void detectEditionAndCpuArch() throws JetHomeException {
        if (edition == null) {
            String version = obtainVersionString();
            edition = JetEdition.retrieveEdition(version);
            if (edition == null) {
                throw new JetHomeException(Txt.s("JetHome.UnableToDetectEdition.Error"));
            }
            is64 = version.contains("64-bit");
        }
    }

    public JetEdition getEdition() throws JetHomeException {
        detectEditionAndCpuArch();
        return edition;
    }

    public boolean is64bit() throws JetHomeException {
        detectEditionAndCpuArch();
        return is64;
    }
}
