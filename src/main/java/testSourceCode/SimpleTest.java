package testSourceCode;

import org.graalvm.polyglot.Context;

import org.graalvm.polyglot.Value;

public class SimpleTest {

    public static void main(String[] args) {
    try (Context context = Context.create()) {

        Value result = context.eval("python", "1 + 1");
        System.out.println("Python result: " + result.asInt());


        result = context.eval("js", "'Hello, ' + 'World!'");
        System.out.println("JS result: " + result.asString());
    }

    }
}
