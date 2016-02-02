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
 * Configuration parameters of Java Runtime Slim-Down feature.
 *
 * @author Nikita Lipsky
 */
public class SlimDownConfig {

    /**
     * <p>
     *   Detach the specified components from the main installation package and place them
     *   in a remote package. If unspecified, the components detected to be unused by your
     *   application are automatically detached if Java Runtime Slim-Down is enabled.
     * </p>
     * <p>
     *   Available detachable components:
     *     {@code corba}, {@code management}, {@code xml}, {@code jndi}, {@code jdbc},
     *     {@code awt/java2d}, {@code swing}, {@code jsound}, {@code rmi}, {@code jax-ws}
     * </p>
     *
     * @see #detachedBaseURL
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String[] detachComponents;

    /**
     * Set the base url for the detached package. This parameter is mandatory.
     * 
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String detachedBaseURL;

    /**
     * Set the detached package name.
     * 
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String detachedPackage;

    boolean isEnabled() {
        return ((detachComponents != null) && (detachComponents.length > 0) || (detachedBaseURL != null) ||
                (detachedPackage != null));
    }

}
