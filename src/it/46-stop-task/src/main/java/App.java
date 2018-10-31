import com.excelsiorjet.api.cmd.*;
import com.excelsiorjet.api.log.*;
import java.io.*;

public class App {

    private static void failed() throws Exception {
        new File("failed").createNewFile();
        System.exit(10);
    }

    public static void main(String args[]) throws Exception {
        File workingDirectory = new File("../../..");
        try {
            // stop application using Maven. It is the only working way to test jet:stop -- call mvn jet:stop within
            // app runned via JET Maven plugin.
            if (new CmdLineTool(args[0], "jet:stop").workingDirectory(workingDirectory).withLog(new StdOutLog()).execute() != 0) {
                failed();
            }
        } catch (Exception e) {
            failed();
        }
        Thread.sleep(5000);
        failed();
    }

}