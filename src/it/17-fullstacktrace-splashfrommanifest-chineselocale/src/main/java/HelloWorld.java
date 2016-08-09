public class HelloWorld {

    public static void main(String args[]) {
        if (new Exception().getStackTrace()[0].getLineNumber() < 0)
            throw new Error("No line numbers in stack trace");
        System.out.println("Hello World");
    }

}