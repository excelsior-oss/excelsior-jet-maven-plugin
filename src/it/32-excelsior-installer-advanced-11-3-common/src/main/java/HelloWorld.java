import java.io.*;

public class HelloWorld {

    public static void main(String args[]) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream("out.txt")))) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                out.println(i + ": " + arg);
            }
        }
    }

}