package com.excelsiorjet;

import com.excelsiorjet.api.log.StdOutLog;
import com.excelsiorjet.api.tasks.JetProject;
import com.excelsiorjet.api.util.Txt;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TxtTest {

    @Before
    public void setUp() {
        JetProject.configureEnvironment(new StdOutLog(), ResourceBundle.getBundle("Strings"));
    }

    @Test
    public void testTxt1() {
        String notValidKey = "NotValidKey";
        assertSame(notValidKey, Txt.s(notValidKey));
    }

    @Test
    public void testTxt2() {
        String validKey = "JetHome.JetNotFound.Error";
        assertTrue(Txt.s(validKey).contains("maven"));
    }
}
