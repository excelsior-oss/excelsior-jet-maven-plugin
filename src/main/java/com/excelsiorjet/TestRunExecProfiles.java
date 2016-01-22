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
 * Encapsulates execution profile files gathered during Test Run.
 *
 * @author Nikita Lipsky
 */
public class TestRunExecProfiles {

    private File usg;
    private File startup;

    public TestRunExecProfiles(File outputDir, String outputname) {
        this.usg = new File(outputDir, outputname + ".usg");
        this.startup = new File(outputDir, outputname + ".startup");
    }

    public File getUsg() {
        return usg;
    }

    public File getStartup() {
        return startup;
    }
}
