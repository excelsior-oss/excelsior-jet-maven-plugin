package testClasses;

import com.excelsiorjet.api.JetHomeException;

import java.io.File;

/**
 * @author Nikita Lipsky
 */
public class FakeJC {

    public static void main(String arg[]) throws JetHomeException {
        String path = System.getenv("PATH");
        String firstEntry = path.split(File.pathSeparator)[0];
        if (!firstEntry.contains("FakeJetHome")) {
            throw new RuntimeException("fail: expected find FakeJetHome as first PATH entry, got " + firstEntry);
        }
        System.out.println("Ok");
    }


}
