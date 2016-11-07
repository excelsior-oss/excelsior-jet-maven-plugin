package com.excelsiorjet;

import com.excelsiorjet.api.ExcelsiorJet;
import com.excelsiorjet.api.JetHomeException;

public class TestUtils {

    private static ExcelsiorJet excelsiorJet;

    static {
        try {
            excelsiorJet = new ExcelsiorJet(null);
        } catch (JetHomeException ignore) {
        }
    }

    public static boolean isWindows() {
        return excelsiorJet.getTargetOS().isWindows();
    }

    public static boolean isOSX() {
        return excelsiorJet.getTargetOS().isOSX();
    }

    public static boolean isCrossCompilation() {
        return excelsiorJet.isCrossCompilation();
    }

    public static String exeExt() {
        return excelsiorJet.getTargetOS().getExeFileExtension();
    }

    public static String mangleDllName(String dll) {
        return excelsiorJet.getTargetOS().mangleDllName(dll);
    }

    public static boolean isExcelsiorInstallerSupported() {
        return excelsiorJet.isExcelsiorInstallerSupported();
    }

    public static boolean isWindowsServicesSupported() {
        return excelsiorJet.isWindowsServicesSupported();
    }

    public static boolean isWindowsServicesInExcelsiorInstallerSupported() {
        return excelsiorJet.isWindowsServicesInExcelsiorInstallerSupported();
    }

    public static String toUnixLineSeparators(String text) {
        return text.replaceAll("\r\n", "\n");
    }

}
