package com.excelsiorjet;

import com.excelsiorjet.api.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author kit
 *         Date: 04.12.2015
 */
public class XJavaTest {

    private AbstractLog log = mock(AbstractLog.class);
    private JetHome jetHome;

    public XJavaTest() throws JetHomeException {
        jetHome = new JetHome();
    }

    @Test
    public void runHello() throws CmdLineToolException, JetHomeException {
        assertEquals(0,
                new XJava(jetHome, "testClasses.HelloWorld")
                .withLog(log)
                .workingDirectory(TestUtils.workDir())
                        .execute());
        verify(log).info("Hello world!");
    }

    @Test
    public void testRun() throws CmdLineToolException, JetHomeException {
        TestRunExecProfiles execProfiles = new TestRunExecProfiles(TestUtils.workDir(), "test");
        assertEquals(0,
                new XJava(jetHome)
                .addTestRunArgs(execProfiles)
                .arg("testClasses.HelloWorld")
                .workingDirectory(TestUtils.workDir())
                        .execute());
        assertTrue(execProfiles.getStartup().exists());
        assertTrue(jetHome.is64bit() || execProfiles.getUsg().exists());
    }
}
