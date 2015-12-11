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

import java.util.Arrays;
import java.util.Collection;

/**
 * Excelsior JET Edition enum.
 *
 * @author Nikita Lipsky
 */
public enum JetEdition {
    EVALUATION("Evaluation"),
    STANDARD("Standard Edition"),
    PROFESSIONAL("Professional Edition"),
    ENTERPRISE("Enterprise Edition"),
    EMBEDDED("Embedded Edition"),
    EMBEDDED_EVALUATION("Embedded Evaluation");

    private final String fullName;

    JetEdition(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Examples: "Enterprise Edition", "Embedded Evaluation".
     */
    public String fullEditionName() {
        return fullName;
    }

    static JetEdition detectEdition(JetHome jetHome) throws JetHomeException {
        try {
            JetEdition[] edition = {null};
            CmdLineTool jetCompiler = new JetCompiler(jetHome).withLog(new SystemStreamLog() {
                public void info(CharSequence info) {
                    if (edition[0] == null) {
                        Arrays.stream(JetEdition.values())
                                .filter(e -> info.toString().contains(e.fullEditionName()))
                                .findFirst()
                                .ifPresent(e -> edition[0] = e);
                    }
                }

                public void error(CharSequence charSequence) {
                }

            });
            if ((jetCompiler.execute() != 0) || edition[0] == null)  {
                throw new JetHomeException(Txt.s("JetHome.UnableToDetectEdition.Error"));
            }
            return edition[0];
        } catch (CmdLineToolException e) {
            throw new JetHomeException(e.getMessage());
        }
    }


}
