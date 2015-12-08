package testClasses;

import com.excelsiorjet.JetHome;
import com.excelsiorjet.JetHomeException;

import java.io.File;

/**
 * @author Nikita Lipsky
 */
public class FakeJC {

    public static void main(String arg[]) throws JetHomeException {
        String path = System.getenv("PATH");
        String firstEntry = path.split(File.pathSeparator)[0];
        if (!firstEntry.equals(new JetHome().getJetBinDirectory())) {
            throw new RuntimeException("fail: expected " + firstEntry + ", got " + new JetHome().getJetBinDirectory());
        }
        System.out.println("Ok");
    }


}
