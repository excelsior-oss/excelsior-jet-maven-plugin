package com.excelsiorjet;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Nikita Lipsky
 */
public class JetCompilerTest {

    private Log log = mock(Log.class);

    @Test
    public void printUsage() throws CmdLineToolException, JetHomeException {
        assertEquals(0, new JetCompiler().withLog(log).execute());
        verify(log).info("Usage:");
    }

    @Test
    public void compileAndRunHelloWorld() throws CmdLineToolException, JetHomeException {
        JetHome jetHome = new JetHome();
        assertEquals(0,
                new JetCompiler(jetHome, "testClasses/HelloWorld")
                .workingDirectory(TestUtils.workDir())
                        .execute());
        File exe = new File(TestUtils.workDir(), Utils.mangleExeName("HelloWorld"));
        assertTrue(exe.exists());
        new CmdLineTool(exe.getAbsolutePath()).withEnvironment("PATH", jetHome.getJetBinDirectory())
                .withLog(log).workingDirectory(TestUtils.workDir()).execute();
        verify(log).info("Hello world!");
        exe.delete();
    }

}
