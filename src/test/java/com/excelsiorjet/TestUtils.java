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

    public static boolean since_11_3() {
        return excelsiorJet.since11_3();
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

    public static boolean isCompactProfilesSupported() {
        return excelsiorJet.isCompactProfilesSupported();
    }

    public static boolean isDiskFootprintReductionSupported() {
        return excelsiorJet.isDiskFootprintReductionSupported();
    }

    public static boolean isMultiAppSupported() {
        return excelsiorJet.isMultiAppSupported();
    }

    public static boolean isStartupProfileGenerationSupported() {
        return excelsiorJet.isStartupProfileGenerationSupported();
    }

    public static boolean isTrialSupported() {
        return excelsiorJet.isTrialSupported();
    }

    public static boolean isDataProtectionSupported() {
        return excelsiorJet.isDataProtectionSupported();
    }

    public static boolean isTomcatSupported() {
        return excelsiorJet.isTomcatSupported();
    }

    public static boolean isWindowsVersionInfoSupported() {
        return excelsiorJet.isWindowsVersionInfoSupported();
    }

    public static boolean isSlimDownSupported() {
        return excelsiorJet.isSlimDownSupported();
    }

    public static boolean isChangeRTLocationAvailable() {
        return excelsiorJet.isChangeRTLocationAvailable();
    }

    //replace line separators to Unix as Groovy """ multiline strings produce Unix line separators
    public static String toUnixLineSeparators(String text) {
        return text.replaceAll("\r\n", "\n");
    }

}
