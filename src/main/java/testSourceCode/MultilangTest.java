package testSourceCode;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class MultilangTest {

    public static void main(String[] args) {
        try (Context context = Context.create()) {

            Value jsFunction = context.eval("js", "(x, y) => x + y");
            int jsResult = jsFunction.execute(2, 3).asInt();
            System.out.println("JavaScript result: " + jsResult);


            Value pyFunction = context.eval("python", "lambda x, y: x * y");
            int pyResult = pyFunction.execute(4, 5).asInt();
            System.out.println("Python result: " + pyResult);


            context.getBindings("js").putMember("javaVar", 10);
            Value jsCode = context.eval("js", "javaVar * 2");
            int javaResultFromJS = jsCode.asInt();
            System.out.println("Java result from JavaScript: " + javaResultFromJS);


            context.getBindings("python").putMember("pythonVar", 7);
            Value pyCode = context.eval("python", "pythonVar + 3");
            int javaResultFromPython = pyCode.asInt();
            System.out.println("Java result from Python: " + javaResultFromPython);
        }
    }
}
