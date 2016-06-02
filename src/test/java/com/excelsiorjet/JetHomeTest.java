package com.excelsiorjet;

import com.excelsiorjet.api.JetHome;
import com.excelsiorjet.api.JetHomeException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
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
        String originalJetHome = System.getProperty("jet.home");
        try {
            String fakeJet = TestUtils.getOrCreateFakeJetHome().getAbsolutePath();
            System.setProperty("jet.home", fakeJet);
            assertEquals(fakeJet, new JetHome().getJetHome());
        } finally {
            System.setProperty("jet.home", originalJetHome != null ? originalJetHome : "");
        }
    }

    @Test(expected = JetHomeException.class)
    public void unsupportedJetHome() throws JetHomeException {
        new JetHome(TestUtils.getOrCreateFakeJetHome("1050").getAbsolutePath());
    }

    @Test
    public void checkEdition() throws JetHomeException {
        assertNotNull(new JetHome().getEdition());
    }

    @After
    public void cleanup() throws IOException {
        TestUtils.cleanFakeJetDir();
    }
}
