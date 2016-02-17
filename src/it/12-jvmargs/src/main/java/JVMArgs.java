import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class JVMArgs {

    public static void main(String args[]) throws IOException {
        String jvmArg = System.getProperty("jetrt.location");
        Files.write(new File("run.out").toPath(), Collections.singletonList(jvmArg));
    }

}