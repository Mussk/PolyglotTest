package testSourceCode;

import org.graalvm.polyglot.Context;

import org.graalvm.polyglot.Value;

public class SimpleTest {

    public static void main(String[] args) {
    try (Context context = Context.create()) {
        // Execute JavaScript code from within Java
        Value result = context.eval("python", "1 + 1");
        System.out.println("Python result: " + result.asInt());

        // Execute Ruby code from within Java
        result = context.eval("js", "'Hello, ' + 'World!'");
        System.out.println("JS result: " + result.asString());
    }

    }
}
