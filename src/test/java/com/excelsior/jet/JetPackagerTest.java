package com.excelsior.jet;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author kit
 */
public class JetPackagerTest {

    private Log log = mock(Log.class);

    @Test
    public void testPackHelloWorld() throws CmdLineToolException, JetHomeException {
        assertEquals(0,
                new JetCompiler("testClasses/HelloWorld")
                .workingDirectory(TestUtils.workDir())
                        .execute());
        File exe = new File(TestUtils.workDir(), Utils.mangleExeName("HelloWorld"));
        final String target = "hw-native";
        File targetDir = new File(TestUtils.workDir(), target);
        assertEquals(0,
                new JetPackager("-add-file", exe.getAbsolutePath(), "/", "-target",
                        targetDir.getAbsolutePath()).execute());
        File exePacked = new File(TestUtils.workDir(), target + File.separator + Utils.mangleExeName("HelloWorld"));
        assertTrue(exePacked.exists());
        new CmdLineTool(exePacked.getAbsolutePath()).withLog(log).execute();
        verify(log).info("Hello world!");
        exe.delete();
        Utils.cleanDirectory(targetDir);
    }

    @Test
    public void createAndTestFakeJC() throws CmdLineToolException, JetHomeException {
        try {
            File classesDir = TestUtils.classesDir();
            assertEquals(0,
                    new JetCompiler("testClasses/FakeJC", "-outputname=jc",
                            "-lookup=*.class=" + classesDir.getAbsolutePath())
                            .workingDirectory(TestUtils.workDir())
                            .execute());

            File exe = new File(TestUtils.workDir(), Utils.mangleExeName("jc"));
            File fakeJetBin = new File(TestUtils.getFakeJetHomeNoCreate(), "bin");
            fakeJetBin.mkdirs();

            assertEquals(0,
                    new JetPackager("-add-file", exe.getAbsolutePath(), "/",
                            "-add-file", classesDir.getAbsolutePath(), "/",
                            "-assign-resource", exe.getName(), classesDir.getName(), classesDir.getName(),
                            "-target", fakeJetBin.getAbsolutePath()).execute());


            new JetCompiler(new JetHome(TestUtils.getOrCreateFakeJetHome().getAbsolutePath()))
                    .withEnvironment("JET_HOME=")
                    .withLog(log).execute();
            verify(log).info("Ok");
        } finally {
            TestUtils.cleanFakeJetDir();
        }
    }


}
