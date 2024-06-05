package testSourceCode;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;

public class PinpongTest {
    public static void main(String[] args) {
        try (Engine engine = Engine.create()) {

            String pyCode = "import polyglot\n" +
                    "arr = polyglot.import_value('arr') \n" +
                    "mul = lambda x: [i * 2 for i in x]\n" +
                    "mul(arr)";

            Context context = Context.newBuilder().engine(engine).allowAllAccess(true).build();

            int[] data = {1, 2, 3, 4, 5};
            System.out.println("Java original data: " + arrayToString(data));

            context.getPolyglotBindings().putMember("arr", data);

            Value pyFunction = context.eval("python", pyCode);
            int[] pyResult = pyFunction.as(int[].class);

            System.out.println("Java data after Python processing: " + arrayToString(pyResult));

            for (int i = 0; i < pyResult.length; i++)
                pyResult[i] *= 2;

            System.out.println("Java data modified after came back from Python: " + arrayToString(pyResult));

            context.getPolyglotBindings().removeMember("arr");
            context.getPolyglotBindings().putMember("arr", pyResult);

            pyFunction = context.eval("python", pyCode);
            int[] pyResult2 = pyFunction.as(int[].class);

            System.out.println("Java data after Python processing: " + arrayToString(pyResult2));

        }
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
