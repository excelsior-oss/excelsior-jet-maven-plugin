package testClasses;

/**
 * @author Nikita Lipsky
 */
public class PrintEnv {

    public static void main(String arg[]) {
        System.out.println(System.getenv(arg[0]));
    }

}
