import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.System;
import java.util.concurrent.ConcurrentHashMap;

public class AppWithDep {

    public static void main(String args[]) throws IOException {
        System.out.println(IOUtils.toString(new StringReader("HelloWorld")));
        org.junit.Assert.assertTrue(true);
    }

}