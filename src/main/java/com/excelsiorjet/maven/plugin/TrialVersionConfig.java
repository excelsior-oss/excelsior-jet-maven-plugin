/*
 * Copyright (c) 2016, Excelsior LLC.
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

/**
 * Configuration parameters of Trial Version generation feature.
 *
 * @author Nikita Lipsky
 */
public class TrialVersionConfig {

    /**
     * Number of calendar days for the trial executable to work after its build date.
     * May not be set simultaneously with {@link #expireDate}.
     */
    public int expireInDays = -1;

    /**
     * Executable expiration date, in format <i>ddMMMyyyy</i>.
     * <p>
     *     For example: {@code 15Sep2020}
     * </p>
     * May not be set simultaneously with {@link #expireInDays}.
     */
    public String expireDate;

    /**
     * Message to display to the user on their attempt to launch the executable
     * after its expiration date.
     */
    public String expireMessage;

    boolean isEnabled() {
        return (expireInDays >=0) || (expireDate != null);
    }

    String getExpire() {
        if (expireInDays >= 0) {
            return String.valueOf(expireInDays);
        } else {
            return expireDate;
        }
    }

}
