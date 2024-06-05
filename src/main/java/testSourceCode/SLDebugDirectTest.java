package testSourceCode;/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import static testSourceCode.SLJavaInteropTest.toUnixString;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/** Guest language is Python **/

public class SLDebugDirectTest {

    public static void main(String[] args) {

        SLDebugDirectTest slDebugDirectTest = new SLDebugDirectTest();

        slDebugDirectTest.context = Context.create("python");

        try {
            slDebugDirectTest.testBreakpoint();
            slDebugDirectTest.stepInStepOver();
            slDebugDirectTest.testNull();

        }catch (Throwable exception){ exception.printStackTrace(); }
    }
    private static final Object UNASSIGNED = new Object();


    //private Debugger debugger;
    private final LinkedList<Runnable> run = new LinkedList<>();
   // private SuspendedEvent suspendedEvent;
    private Throwable ex;
    private Engine engine;

    public Context context;

    protected final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private static Source createFactorial() {
        return Source.newBuilder("python",  "def test():\n" +
                "    res = fac(2)\n" +
                "    print(res)\n" +
                "    return res\n\n" +
                "def fac(n):\n" +
                "    if n <= 1:\n" +
                "        return 1\n" +
                "    n_minus_one = n - 1\n" +
                "    n_mo_fact = fac(n_minus_one)\n" +
                "    res = n * n_mo_fact\n" +
                "    return res\n", "factorial.py").buildLiteral();
    }

    private static Source createFactorialWithDebugger() {
        return Source.newBuilder("python", "def test():\n" +
                "    res = fac(2)\n" +
                "    print(res)\n" +
                "    return res\n\n" +
                "def fac(n):\n" +
                "    if n <= 1:\n" +
                "        return 1\n" +
                "    n_minus_one = n - 1\n" +
                "    n_mo_fact = fac(n_minus_one)\n" +
                "    # debugger placeholder\n" +
                "    res = n * n_mo_fact\n" +
                "    return res\n", "factorial.py").buildLiteral();
    }

    private static Source createInteropComputation() {
        return Source.newBuilder("python",  "def test():\n" +
                "    pass\n\n" +
                "def interop_function(notify_handler):\n" +
                "    executing = True\n" +
                "    while executing == True or executing:\n" +
                "        executing = notify_handler.is_executing\n" +
                "    return executing\n", "interopComputation.py").buildLiteral();
    }

    protected final String getOut() {
        return toUnixString(out);
    }

    protected final String getErr() {
        try {
            err.flush();
        } catch (IOException e) {
        }
        return toUnixString(err);
    }


    public void testBreakpoint() throws Throwable {
        final Source factorial = createFactorial();


        context.eval(factorial);
        assertExecutedOK();

        Value value = context.getBindings("python").getMember("test").execute();
        assertExecutedOK();
        int n = value.asInt();
        System.out.println(n);

    }

    public void stepInStepOver() throws Throwable {
        final Source factorial = createFactorial();
        context.eval(factorial);

        Value value = context.getBindings("python").getMember("test");
        Value resultValue = value.execute();
        String resultStr = resultValue.toString();
        Number result = resultValue.asInt();
        assertExecutedOK();

        System.out.println("result: " + result);
        System.out.println("Factorial computed OK (should be 2): " + result.intValue());
        System.out.println("Factorial computed OK (should be 2): " + resultStr);
    }


    public void testPause() throws Throwable {
        final Source interopComp = createInteropComputation();

        context.eval(interopComp);
        assertExecutedOK();

        final ExecNotifyHandler nh = new ExecNotifyHandler();

        // Do pause after execution has really started

        Value value = context.getBindings("python").getMember("interopFunction").execute(nh);

        assertExecutedOK();
        System.out.println(value);
        boolean n = value.asBoolean();
        System.out.println("Interop computation OK: " + !n);

    }

    private static Source createNull() {
        return Source.newBuilder("python", "def null_test():\n" +
                "    res = do_null()\n" +
                "    return res\n\n" +
                "def do_null():\n" +
                "    pass\n", "nullTest.py").buildLiteral();
    }


    public void testNull() throws Throwable {
        final Source nullTest = createNull();
        context.eval(nullTest);

        Value value = context.getBindings("python").getMember("null_test").execute();
        assertExecutedOK();

        String val = value.toString();
        System.out.println("Should be null: " + val);

    }

    private void assertExecutedOK() throws Throwable {

        if (ex != null) {
            if (ex instanceof AssertionError) {
                throw ex;
            } else {
                throw new AssertionError("Error during execution", ex);
            }
        }
        System.out.println("Assuming all requests processed: " +run + ", " + run.isEmpty());
    }


    @SuppressWarnings({"static-method", "unused"})
    static class ExecNotifyHandler {

        private final Object pauseLock = new Object();
        private boolean canPause;
        private volatile boolean pauseDone;


        final Object readMember(String member) {
            setCanPause();
            return !isPauseDone();
        }


        final boolean isMemberReadable(String member) {
            return true;
        }


        final boolean hasMembers() {
            return true;
        }


        final Object getMembers(boolean includeInternal) {
            throw new AssertionError();
        }

        private void waitTillCanPause() {
            synchronized (pauseLock) {
                while (!canPause) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException iex) {
                    }
                }
            }
        }

        void setCanPause() {
            synchronized (pauseLock) {
                canPause = true;
                pauseLock.notifyAll();
            }
        }

        private void pauseDone() {
            pauseDone = true;
        }

        boolean isPauseDone() {
            return pauseDone;
        }

    }

}
