/*
 * Copyright (c) 2017, 2022, Oracle and/or its affiliates. All rights reserved.
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
package testSourceCode;

import static testSourceCode.SLExceptionTest.assertGuestFrame;
import static testSourceCode.SLExceptionTest.assertHostFrame;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.PolyglotException.StackFrame;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;



/** Guest language is JS **/

public class SLJavaInteropExceptionTest {

    public static void main(String[] args) {

        SLJavaInteropExceptionTest slJavaInteropExceptionTest = new SLJavaInteropExceptionTest();
        try {

            System.out.println("testGR7284\n");
            slJavaInteropExceptionTest.testGR7284();
            System.out.println("\ntestGR7284GuestHostGuestHost\n");
            slJavaInteropExceptionTest.testGR7284GuestHostGuestHost();
            System.out.println("\ntestGuestHostCallbackGuestError\n");
            slJavaInteropExceptionTest.testGuestHostCallbackGuestError();
            System.out.println("\ntestGuestHostCallbackHostError\n");
            slJavaInteropExceptionTest.testGuestHostCallbackHostError();
            System.out.println("\ntestFunctionProxy\n");
            slJavaInteropExceptionTest.testFunctionProxy();
            System.out.println("\ntestTruffleMap\n");
            slJavaInteropExceptionTest.testTruffleMap();

        }catch (Exception exception) {exception.printStackTrace();}

    }

    public static class Validator {

        @HostAccess.Export
        public int validateException() {
            throw new NoSuchElementException();
        }

        @HostAccess.Export
        public void validateNested() throws Exception {
            String sourceText = "function test(validator) {\n" +
                            "  return validator.validateException();\n" +
                            "}";
            try (Context context = Context.newBuilder("js").build()) {
                context.eval(Source.newBuilder("js", sourceText, "Test").build());
                Value test = context.getBindings("js").getMember("test");
                test.execute(Validator.this);
            }
        }

        @HostAccess.Export
        @SuppressWarnings("unchecked")
        public Object validateCallback(int index, Map<?, ?> map) throws Exception {
            Object call = map.get(Integer.toString(index));
            if (call == null) {
                throw new NullPointerException("Nothing to call");
            }
            return ((Function<Object, Object>) call).apply(new Object[]{this, index});
        }

        @HostAccess.Export
        public long validateFunction(Supplier<Long> function) {
            return function.get();
        }

        @HostAccess.Export
        public void validateMap(Map<String, Object> map) {
            //Assert.assertNull(map.get(null));
        }
    }


  //public static void runWithWeakEncapsulationOnly() {
      //      TruffleTestAssumptions.assumeWeakEncapsulation();



    public void testGR7284() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  return validator.validateException();\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
           try {
                test.execute(new Validator());
              //  fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
               System.out.println("expected HostException: " + ex.isHostException());
               System.out.println(ex.asHostException().toString() + ": " + NoSuchElementException.class);

            }
        }
    }


    public void testGR7284GuestHostGuestHost() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  return validator.validateNested();\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            try {
                test.execute(new Validator());
                //fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                System.out.println("expected HostException: " + ex.isHostException());
                System.out.println(ex.asHostException().toString() + ": " + NoSuchElementException.class);
               // assertTrue("expected HostException", ex.isHostException());
              //  assertThat(ex.asHostException(), instanceOf(NoSuchElementException.class));
            }
        }
    }


    public void testGuestHostCallbackGuestError() throws Exception {
        String sourceText = "function doMultiCallback(validator, n) {\n" +
                        "    map = new Object();\n" +
                        "    if (n <= 0) {\n" +
                        "        return error();\n" +
                        "    }\n" +
                        "    map[n] = doCall;\n" +
                        "    validator.validateCallback(n, map);\n" +
                        "}\n" +
                        "function doCall(validator, x) {\n" +
                        "    doMultiCallback(validator, x - 1);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value doMultiCallback = context.getBindings("js").getMember("doMultiCallback");
            int numCalbacks = 3;
            try {
                doMultiCallback.execute(new Validator(), numCalbacks);
              // fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                Iterator<StackFrame> frames = ex.getPolyglotStackTrace().iterator();
                assertGuestFrame(frames, "js", "error");
                assertGuestFrame(frames, "js", "doMultiCallback", "Test", 91, 98);
                for (int i = 0; i < numCalbacks; i++) {
                    assertGuestFrame(frames, "js", "doCall", "Test", 205, 238);
                    assertHostFrame(frames, "com.oracle.truffle.polyglot.PolyglotFunction", "apply");
                    assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                    assertGuestFrame(frames, "js", "doMultiCallback", "Test", 131, 165);
                }
                assertHostFrame(frames, Value.class.getName(), "execute");

            }
        }
    }


    public void testGuestHostCallbackHostError() throws Exception {
        String sourceText = "function doMultiCallback(validator, n) {\n" +
                        "    map = new Object();\n" +
                        "    if (n <= 0) {\n" +
                        "        return validator.validateCallback(n, map); // will throw error\n" +
                        "    }\n" +
                        "    map[n] = doCall;\n" +
                        "    validator.validateCallback(n, map);\n" +
                        "}\n" +
                        "function doCall(validator, x) {\n" +
                        "    doMultiCallback(validator, x - 1);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value doMultiCallback = context.getBindings("js").getMember("doMultiCallback");
            int numCalbacks = 3;
            try {
                doMultiCallback.execute(new Validator(), numCalbacks);
               // fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
               //Assert.assertEquals("Nothing to call", ex.getMessage());
                Iterator<StackFrame> frames = ex.getPolyglotStackTrace().iterator();
                assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                assertGuestFrame(frames, "js", "doMultiCallback", "Test", 91, 125);
                for (int i = 0; i < numCalbacks; i++) {
                    assertGuestFrame(frames, "js", "doCall", "Test", 252, 285);
                    assertHostFrame(frames, "com.oracle.truffle.polyglot.PolyglotFunction", "apply");
                    assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                    assertGuestFrame(frames, "js", "doMultiCallback", "Test", 178, 212);
                }
                assertHostFrame(frames, Value.class.getName(), "execute");

            }
        }
    }

    public void testFunctionProxy() throws Exception {
        String javaMethod = "validateFunction";
        String sourceText = "" +
                        "function supplier() {\n" +
                        "  return error();\n" +
                        "}\n" +
                        "function test(validator) {\n" +
                        "  return validator." + javaMethod + "(supplier);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            try {
                test.execute(new Validator());
                //fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                StackTraceElement last = null;
                boolean found = false;
                for (StackTraceElement curr : ex.getStackTrace()) {
                    if (curr.getMethodName().contains(javaMethod)) {
                        if(last != null) {
                            System.out.println("expected Proxy stack frame: " + last.getClassName().contains("Proxy"));
                        }
                       // assertNotNull(last);
                       // assertThat("expected Proxy stack frame", last.getClassName(), containsString("Proxy"));
                        found = true;
                        break;
                    }
                    last = curr;
                }
                System.out.println(javaMethod + " not found in stack trace: " + found);
              //  assertTrue(javaMethod + " not found in stack trace", found);
            }
        }
    }


    public void testTruffleMap() throws Exception {
        boolean res = false;
        String javaMethod = "validateMap";
        String sourceText = "" +
                        "function test(validator) {\n" +
                        "  return validator." + javaMethod + "(new Object());\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            test.execute(new Validator());
            res = true;
            System.out.println(res);
        } catch (Exception exception) {

            exception.printStackTrace();
            res = false;
            System.out.println(res);
        }

    }
}