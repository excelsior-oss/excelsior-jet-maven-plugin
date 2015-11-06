package com.excelsior.jet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Nikita Lipsky
 */
public class JetHomeTest {

    @Test(expected = JetHomeException.class)
    public void createBadJetHome() throws JetHomeException {
        new JetHome("Bad jet home");
    }

    @Test
    public void jetHomeIdempotent() throws JetHomeException {
        String jetHome = new JetHome().getJetHome();
        JetHome jetHomeObj = new JetHome(jetHome);
        assertSame(jetHome, jetHomeObj.getJetHome());
    }

    @Test
    public void fakeJetHome() throws JetHomeException {
        File fakeJet = TestUtils.getOrCreateFakeJetHome();
        new JetHome(fakeJet.getAbsolutePath());
    }

    @Test
    public void jetHomeViaVMProp() throws JetHomeException {
        try {
            String fakeJet = TestUtils.getOrCreateFakeJetHome().getAbsolutePath();
            System.setProperty("jet.home", fakeJet);
            assertEquals(fakeJet, new JetHome().getJetHome());
        } finally {
            System.setProperty("jet.home", "");
        }
    }

    @Test(expected = JetHomeException.class)
    public void unsupportedJetHome() throws JetHomeException {
        new JetHome(TestUtils.getOrCreateFakeJetHome("1050").getAbsolutePath());
    }

    @After
    public void cleanup() throws IOException {
        TestUtils.cleanFakeJetDir();
    }
}
