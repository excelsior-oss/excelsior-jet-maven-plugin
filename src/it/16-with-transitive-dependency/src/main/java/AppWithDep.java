import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class AppWithDep {

    public static void main(String args[]) throws IOException {
        System.out.println(new ObjectMapper().writeValueAsString(new Obj("field1", "field2")));
    }

    private static class Obj {

        private final String field1;

        private final String field2;

        public Obj(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }
    }

}