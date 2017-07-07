import java.util.*;

public class Test {

    public static class ColdClass {

        static String result;

        public static void foo() {
            result = String.format("0x%08x", new Object[] {Integer.valueOf(2)});
        }

    }

    public static void hotCode() {
        //warm-up
        for (int i = 0; i < 10000; i++) {
            String.format("0x%08x", new Object[] {Integer.valueOf(i)});
        }

        long start = System.currentTimeMillis();

        for (int i = 10000000; i < 10300000; i++) {
          new Formatter().format("0x%08x", new Object[] {Integer.valueOf(i)}).toString();
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }

    public static void main(String[] args) {
        ColdClass.foo();
        hotCode();
    }
}
