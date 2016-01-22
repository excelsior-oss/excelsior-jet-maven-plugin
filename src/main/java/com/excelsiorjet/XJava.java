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

/**
 * Excelsior JET "xjava" tool executor utility class.
 *
 * @author Nikita Lipsky
 */
public class XJava extends JetTool {

    private static final String X_JAVA = "xjava";

    public XJava(JetHome jetHome, String... args) {
        super(jetHome, X_JAVA, args);
    }

    public XJava addTestRunArgs(TestRunExecProfiles execProfiles) throws JetHomeException {
        arg("-Djet.jit.profile.startup=" + execProfiles.getStartup().getAbsolutePath());
        if (!jetHome.is64bit()) {
            arg("-Djet.usage.list=" + execProfiles.getUsg().getAbsolutePath());
        }
        return this;
    }
}
