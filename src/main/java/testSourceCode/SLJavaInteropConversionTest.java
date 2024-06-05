/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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


import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;



import org.graalvm.polyglot.HostAccess;

/** Guest language is JS **/

public class SLJavaInteropConversionTest {

    public static void main(String[] args) {

        SLJavaInteropConversionTest slJavaInteropConversionTest = new SLJavaInteropConversionTest();

        try
        {
            slJavaInteropConversionTest.testGR7318Object();
            slJavaInteropConversionTest.testGR7318Map();
            slJavaInteropConversionTest.testGR7318List();

        }catch (Exception exception){exception.printStackTrace();}
    }

    public static class Validator {
        @HostAccess.Export
        @SuppressWarnings("unchecked")
        public int validateObject(Object value1, Value value2) {
                if (!((Map<?, ?>) value1).isEmpty())
                    if(((Map<String, ?>) value1).containsKey("a") && ((Map<String, ?>) value1).containsKey("b"))
                        if(value2.getClass().equals(Value.class))
                            if(value2.hasMembers())
                                if(value2.getMemberKeys().contains("a") && value2.getMemberKeys().contains("b"))
                                    return 42;
           // assertThat(value1, instanceOf(Map.class));
           // assertTrue(!((Map<?, ?>) value1).isEmpty());
          //  assertThat(((Map<String, ?>) value1).keySet(), hasItems("a", "b"));
           // assertThat(value2, instanceOf(Value.class));
          //  assertTrue(value2.hasMembers());
           // assertThat(value2.getMemberKeys(), hasItems("a", "b"));
            return 0;
        }

        @HostAccess.Export
        public int validateMap(Map<String, Object> map1, Map<String, Value> map2) {
            int res = 0;
          // assertEquals(2, map1.size());
          // assertThat(map1.keySet(), hasItems("a", "b"));
            if(map1.size() == 2)
                if(map1.containsKey("a") && map1.containsKey("b")) {
                    for (Object value : map1.values()) {
                        //  assertThat(value, instanceOf(Map.class));
                        System.out.println(value.getClass() + ": " + instanceOf(Map.class));
                    }
                    res = 21;
                }
           // assertEquals(2, map2.size());
           // assertThat(map2.keySet(), hasItems("a", "b"));
            if(map2.size() == 2)
                if(map2.containsKey("a") && map2.containsKey("b")) {
                    for (Object value : map2.values()) {
                        //  assertThat(value, instanceOf(Map.class));
                        System.out.println(value.getClass() + ": " + instanceOf(Value.class));
                    }
                    res *= 2;
                }
            return res;
        }

        @HostAccess.Export
        public int validateList(List<Object> list1, List<Value> list2) {
            int res = 0;
          //  assertEquals(2, list1.size());
            if(list1.size() == 2) {
                for (Object value : list1) {
                    // assertThat(value, instanceOf(Map.class));
                    System.out.println(value.getClass() + ": " + instanceOf(Map.class));
                }
                res = 21;
            }
            //assertEquals(2, list2.size());
            if(list1.size() == 2) {
                for (Object value : list2) {
                    // assertThat(value, instanceOf(Map.class));
                  System.out.println(value.getClass() + ": " + instanceOf(Value.class));
                }
                res *= 2;
            }
            return res;
        }
    }


    public void testGR7318Object() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  obj = new Object();\n" +
                        "  obj.a = new Object();\n" +
                        "  obj.b = new Object();\n" +
                        "  return validator.validateObject(obj, obj);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            Value res = test.execute(new Validator());
            System.out.println("42: " + res.asInt());
           // assertTrue(res.isNumber() && res.asInt() == 42);
        }
    }


    public void testGR7318Map() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  obj = new Object();\n" +
                        "  obj.a = new Object();\n" +
                        "  obj.b = new Object();\n" +
                        "  return validator.validateMap(obj, obj);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            Value res = test.execute(new Validator());
           // assertTrue(res.isNumber() && res.asInt() == 42);
            System.out.println("42: " + res.asInt());
        }
    }


    public void testGR7318List() throws Exception {
        String sourceText = "function test(validator, array) {\n" +
                        "  array[0] = new Object();\n" +
                        "  array[1] = new Object();\n" +
                        "  return validator.validateList(array, array);\n" +
                        "}";
        try (Context context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build()) {
            context.eval(Source.newBuilder("js", sourceText, "Test").build());
            Value test = context.getBindings("js").getMember("test");
            Value res = test.execute(new Validator(), new Object[2]);
           // assertTrue(res.isNumber() && res.asInt() == 42);
            System.out.println("42: " + res.asInt());
        }
    }
}
