package com.excelsiorjet;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Nikita Lipsky
 */
public class CmdLineToolTest {

    private Log log = mock(Log.class);

    @Test(expected = CmdLineToolException.class)
    public void executeNotExist() throws CmdLineToolException {
        new CmdLineTool("NotExist").execute();
    }

    private String getJavaExe() {
        String javaHome = System.getProperty("java.home");
        assertNotNull(javaHome);
        return Utils.mangleExeName(javaHome + File.separator + "bin" + File.separator + "java");
    }

    @Test
    public void executeJava() throws CmdLineToolException {
        assertEquals(1, new CmdLineTool(getJavaExe()).withLog(log).execute());
        verify(log).error("Usage: java [-options] class [args...]");
        verify(log, never()).info("");
        verify(log, never()).error("Abrakadabra");
    }


    @Test
    public void executeHelloWorldOnJava() throws CmdLineToolException {
        assertEquals(0,
                new CmdLineTool(getJavaExe(), "testClasses/HelloWorld")
                .withLog(log)
                .workingDirectory(TestUtils.workDir())
                .execute());
        verify(log).info("Hello world!");
    }

    @Test
    public void executePatchedEnv() throws CmdLineToolException {
        assertEquals(0,
                new CmdLineTool(getJavaExe(), "testClasses/PrintEnv", "PATH")
                .withLog(log)
                .withEnvironment("PATH", "EmptyPath")
                .workingDirectory(TestUtils.workDir())
                .execute());
        verify(log).info("EmptyPath");
    }

}
