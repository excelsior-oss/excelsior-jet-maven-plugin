public class HelloWorld {

    public static void main(String args[]) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.out.println(i + ": " + arg);

        }
        if (args.length == 0) {
            System.out.println("No arguments specified");
            System.exit(3);
        }
    }

}