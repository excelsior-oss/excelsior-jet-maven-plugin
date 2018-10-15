import java.io.*;

public class App {

    private static void failed() throws Exception {
        new File("failed").createNewFile();
        System.exit(10);
    }

    public static void main(String args[]) throws Exception {
        Thread.sleep(10000);
        failed();
    }

}