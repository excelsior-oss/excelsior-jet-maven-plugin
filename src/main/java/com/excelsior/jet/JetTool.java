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

import java.io.File;

/**
 *  A parent of Excelsior JET tools executors that are reside in [JET]/bin directory.
 *
 * @author Nikita Lipsky
 */
public class JetTool extends CmdLineTool {

    private static String[] getNewArgs(JetHome jetHome, String exeName, String[] args) {
        String newArgs[] = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = Utils.mangleExeName(jetHome.getJETBinDirectory() + File.separator + exeName);
        return newArgs;
    }

    public JetTool(JetHome jetHome, String exeName, String... args) {
        super(getNewArgs(jetHome, exeName, args));
        String path = System.getenv("PATH");
        //place itself to the start of path
        path = jetHome.getJETBinDirectory() + File.pathSeparator + path;
        withEnvironment("PATH=" + path);
    }

    public JetTool(String exeName, String... args) throws JetHomeException {
        this(new JetHome(), exeName, args);
    }
}
