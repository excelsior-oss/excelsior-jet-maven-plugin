/*
 * Copyright (c) 2015,2016 Excelsior LLC.
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

import java.io.File;

/**
 * Configuration parameters of OS X App Bundle.
 * Used to create OS X App Bundle and Mac Installer Package (.pkg file).
 *
 * <p>
 * See
 * <a href=
 * "https://developer.apple.com/library/mac/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html">
 * Bundle Programming Guide</a>
 * to learn more about OS X App Bundles.
 * </p>
 * @author Nikita Lipsky
 */
public class OSXAppBundleConfig {

    /**
     * OS X app bundle file name.
     * Default is {@link JetMojo#outputName}.
     */
    public String fileName;

    /**
     * Value for {@code CFBundleName} key in the resulting {@code Info.plist} file.
     * Default is {@link JetMojo#product}.
     */
    public String bundleName;

    /**
     * Value for {@code CFBundleIdentifier} key in the resulting {@code Info.plist} file.
     * Default is {@code ${project.groupId}.${project.build.finalName}}.
     */
    public String identifier;

    /**
     * Value for {@code CFBundleShortVersionString} key in the resulting {@code Info.plist} file.
     * By default is derived from {@link JetMojo#version}.
     */
    public String shortVersion;

    /**
     * Value for {@code CFBundleVersionString} key in the resulting {@code Info.plist} file.
     * By default is derived from {@code ${project.version}}.
     */
    public String version;

    /**
     * Value for {@code CFBundleIconFile} key in the resulting {@code Info.plist} file.
     * Default is {@code ${project.basedir}/src/main/jetresources/icon.icns}.
     */
    public File icon;

    /**
     * Value for {@code NSHighResolutionCapable} key in the resulting {@code Info.plist} file.
     */
    public boolean highResolutionCapable = true;

    /**
     * "Developer ID Application" or "Mac App Distribution" certificate name for signing resulting OSX app bundle.
     *  You may also set the parameter via {@code osx.developer.id"} system property.
     * <p>
     * See
     * <a href=
     * "https://developer.apple.com/library/ios/documentation/IDEs/Conceptual/AppDistributionGuide/MaintainingCertificates/MaintainingCertificates.html">
     * App Distribution Guide</A>
     * for more details.
     * </p>
     */
    public String developerId;

    /**
     * "Developer ID Installer" or "Mac Installer Distribution" certificate name for signing resulting
     *  Mac Installer Package (.pkg file).
     *  You may also set the parameter via {@code osx.publisher.id"} system property.
     *
     * <p>
     * See
     * <a href=
     * "https://developer.apple.com/library/ios/documentation/IDEs/Conceptual/AppDistributionGuide/MaintainingCertificates/MaintainingCertificates.html">
     * App Distribution Guide</A>
     * for more details.
     * </p>
     *
     */
    public String publisherId;

    /**
     * The default installation path on the target system used for creation .pkg installer file.
     * Default value is "/Applications".
     */
    public String installPath = "/Applications";
}
