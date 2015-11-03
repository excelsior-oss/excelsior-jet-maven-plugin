package testClasses;

import com.excelsior.jet.JetHome;
import com.excelsior.jet.JetHomeException;
import com.excelsior.jet.Utils;

import java.io.File;

/**
 * @author Nikita Lipsky
 */
public class FakeJC {

    public static void main(String arg[]) throws JetHomeException {
        String path = System.getenv("PATH");
        String firstEntry = path.split(File.pathSeparator)[0];
        if (!firstEntry.equals(new JetHome().getJETBinDirectory()))
            throw new RuntimeException("fail: expected " + firstEntry + ", got " + new JetHome().getJETBinDirectory());
        System.out.println("Ok");
    }


}
