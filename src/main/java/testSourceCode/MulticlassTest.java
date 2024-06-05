package testSourceCode;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;


public class MulticlassTest {

    public static void main(String[] args) {
        try (Engine engine = Engine.create()) {
            Context context = Context.newBuilder().engine(engine).allowAllAccess(true).build();


            int[] javaData = {1, 2, 3, 4, 5};
            System.out.println("Java original data: " + arrayToString(javaData));
            context.getPolyglotBindings().putMember("arr", javaData);

            context.eval("python", "import polyglot\n" +
                    "class PythonClass1:\n" +
                    "    def process_data(self):\n" +
                    "       arr = polyglot.import_value('arr')\n" +
                    "       return [i * 2 for i in arr]\n" +
                    "instance1 = PythonClass1()");


            Value pythonInstance1 = context.getBindings("python").getMember("instance1");
            Value processDataMethod1 = pythonInstance1.getMember("process_data");



            Value pythonResult1 = processDataMethod1.execute();
            int[] pythonData = new int[(int) pythonResult1.getArraySize()];
            for (int i = 0; i < pythonResult1.getArraySize(); i++) {
                pythonData[i] = pythonResult1.getArrayElement(i).asInt();
            }
            System.out.println("Java data after PythonClass1 processing: " + arrayToString(pythonData));


            context.eval("python", "class PythonClass2:\n" +
                    "    def process_data(self, data):\n" +
                    "        return [i + 1 for i in data]\n" +
                    "instance2 = PythonClass2()");


            Value pythonInstance2 = context.getBindings("python").getMember("instance2");
            Value processDataMethod2 = pythonInstance2.getMember("process_data");


            Value pythonList2 = context.eval("python",
                    "def to_list(java_data):\n" +
                            "    return [i for i in java_data]\n" +
                            "to_list").execute((Object) pythonData);


            Value pythonResult2 = processDataMethod2.execute(pythonList2);
            int[] finalResult = new int[(int) pythonResult2.getArraySize()];
            for (int i = 0; i < pythonResult2.getArraySize(); i++) {
                finalResult[i] = pythonResult2.getArrayElement(i).asInt();
            }
            System.out.println("Java data after PythonClass2 processing: " + arrayToString(finalResult));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i : array) {
            sb.append(i).append(" ");
        }
        return sb.toString().trim();
    }
}
