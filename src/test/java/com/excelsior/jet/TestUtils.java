package com.excelsior.jet;

import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;

/**
 * @author Nikita Lipsky
 */
public class TestUtils {

    static final String FAKE_JET_HOME = "FakeJetHome";

    public static File workDir() {
        try {
            return new File(TestUtils.class.getResource("/").toURI());
        } catch (URISyntaxException e) {
            fail();
            return null;
        }
    }

    public static File classesDir() {
        try {
            String utilsClass = "/" + Utils.class.getName().replace('.', '/') + ".class";
            String utilsFile  =  new File(Utils.class.getResource(utilsClass).toURI()).getAbsolutePath();
            return  new File (utilsFile.substring(0, utilsFile.length() - utilsClass.length()));
        } catch (URISyntaxException e) {
            fail();
            return null;
        }
    }

    static File getFakeJetHomeNoCreate(){
        return new File(workDir(), FAKE_JET_HOME);
    }

    static File getOrCreateFakeJetHome(String version){
        File fakeJetHome = getFakeJetHomeNoCreate();
        File fakeJetHomeBin = new File(fakeJetHome, "bin");
        if (new File(fakeJetHomeBin, "jet.config").exists()) return fakeJetHome;
        fakeJetHome.mkdir();
        fakeJetHomeBin.mkdir();
        try {
            new File(fakeJetHomeBin, "jet.config").createNewFile();
            new File(fakeJetHomeBin, "jet" + version + ".home").createNewFile();
            File jc = new File(fakeJetHomeBin, Utils.mangleExeName(JetCompiler.JET_COMPILER));
            if (!jc.exists()) {
                jc.createNewFile();
            }
            File xpack = new File(fakeJetHomeBin, Utils.mangleExeName(JetPackager.JET_PACKAGER));
            if (!xpack.exists()) {
                xpack.createNewFile();
            }
        } catch (IOException ignore) {
        }
        return fakeJetHome;
    }

    static File getOrCreateFakeJetHome(){
        return getOrCreateFakeJetHome("1100");
    }

    public static void cleanFakeJetDir() throws IOException {
        Utils.cleanDirectory(new File(workDir(), FAKE_JET_HOME));
    }
}
