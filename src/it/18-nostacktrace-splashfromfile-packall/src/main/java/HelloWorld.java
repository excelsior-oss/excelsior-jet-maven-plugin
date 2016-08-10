public class HelloWorld {

    public static void main(String args[]) {
        if (!new Exception().getStackTrace()[0].getClassName().equals("Unknown"))
            throw new Error("Stack trace is here");
        System.out.println("Hello World");
    }

}