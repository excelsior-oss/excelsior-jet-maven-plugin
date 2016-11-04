public class HelloDll {

    public static void dllEntryPoint() {
        System.out.println("Hello Dll");
    }

    public static void main(String args[]) {
        dllEntryPoint();
    }

}