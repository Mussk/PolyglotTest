package testSourceCode;
/*
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


/** Guest lang is JS **/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;


public class SLJavaInteropTest {

    public static void main(String[] args) {

        SLJavaInteropTest slJavaInteropTest = new SLJavaInteropTest();

        slJavaInteropTest.create();
        try {
            slJavaInteropTest.testHostFunctionDisplayName();
            slJavaInteropTest.asFunction();
            slJavaInteropTest.clearArray();
            slJavaInteropTest.asFunctionWithArg();
            slJavaInteropTest.clearArray();
            slJavaInteropTest.asFunctionWithArr();
            slJavaInteropTest.clearArray();
            slJavaInteropTest.asFunctionWithVarArgs();
            slJavaInteropTest.clearArray();
            slJavaInteropTest.asFunctionWithArgVarArgs();
            slJavaInteropTest.sumPairs();
            slJavaInteropTest.sumPairsFunctionalInterface();
            slJavaInteropTest.sumPairsFunctionalRawInterface();
            slJavaInteropTest.sumPairsIndirect();
            slJavaInteropTest.sumPairsInArray();
            slJavaInteropTest.sumPairsInArrayOfArray();
            slJavaInteropTest.sumMapInArrayOfArray();
            slJavaInteropTest.sumPairInMapOfArray();
            slJavaInteropTest.accessJavaMap();
            slJavaInteropTest.testMemberAssignment();

            slJavaInteropTest.dispose();
        }catch (Exception exception){exception.printStackTrace();}
    }

    private Context context;
    private ByteArrayOutputStream os;


    public void create() {
        os = new ByteArrayOutputStream();
        context = Context.newBuilder().allowHostAccess(HostAccess.ALL).allowHostClassLookup((s) -> true).out(os).build();
    }


    public void dispose() {
        context.close();
    }


    public void clearArray() {

        os.reset();

    }

    public void testHostFunctionDisplayName() throws IOException {
        boolean res1 = false, res2 = false;

        context.eval(Source.newBuilder("js","function main() {\n" + "    return Java.type(\"java.math.BigInteger\").valueOf;\n" + "}\n","Test").build());
        if((BigInteger.class.getName() + ".valueOf").equals(context.getBindings("js").getMember("main").execute().toString())) {
            res1 = true;
        }
        context.eval(Source.newBuilder("js","function main() {\n" + "    return Java.type(\"java.math.BigInteger\").ZERO.add;\n" + "}\n","Test").build());
        if((BigInteger.class.getName() + ".add").equals(context.getBindings("js").getMember("main").execute().toString())){
            res2 = true;
        }

        System.out.println("testHostFunctionDisplayName1: " + res1 + "\ntestHostFunctionDisplayName2: " + res2);

    }


    public void asFunction() throws Exception {
        boolean res = false;
        String scriptText = "function test() {\n" + "    print(\"Called!\");\n" + "}\n";
        context.eval("js", scriptText);
        Value main = lookup("test");
        Runnable runnable = main.as(Runnable.class);
        runnable.run();

        if("Called!\n".equals(toUnixString(os))) {

            res = true;

        }
        System.out.println("asFunction: " + res);

       // assertEquals("Called!\n", toUnixString(os));
    }

    private Value lookup(String symbol) {
        return context.getBindings("js").getMember(symbol);
    }


    public void asFunctionWithArg() throws Exception {
        boolean res = false;
        String scriptText = "function values(a, b) {\n" + //
                        "  print(\"Called with \" + a + \" and \" + b);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");
        PassInValues valuesIn = fn.as(PassInValues.class);
        valuesIn.call("OK", "Fine");


        if("Called with OK and Fine\n".equals(toUnixString(os))) {

            res = true;

        }

        System.out.println("asFunctionWithArg: " + res);

       // assertEquals("Called with OK and Fine\n", toUnixString(os));
    }

    private static void assertNumber(double exp, Object real) {
        double delta = 0.1;
        if (real instanceof Number) {
            if(Math.abs(exp - ((Number) real).doubleValue()) <= delta) {
          // assertEquals(exp, ((Number) real).doubleValue(), 0.1);
                System.out.println("From " + Thread.currentThread().getStackTrace()[2].getMethodName() + //
                        ": " + exp + " and " + real + " are equal within " + delta);
            }
        } else {
          //  fail("Expecting a number, but was " + real);
            System.out.println("Expecting a number, but was " + real);
        }
    }

    @FunctionalInterface
    public interface PassInValues {
        void call(Object a, Object b);
    }


    public void asFunctionWithArr() throws Exception {
        boolean res = false;
        String scriptText = "function values(a, b) {\n" + //
                        "  print(\"Called with \" + a[0] + a[1] + \" and \" + b);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");
        PassInArray valuesIn = fn.as(PassInArray.class);
        valuesIn.call(new Object[]{"OK", "Fine"});

        if("Called with OKFine and undefined\n".equals(toUnixString(os))) {

            res = true;

        }

        System.out.println("asFunctionWithArr: " + res);
        //assertEquals("Called with OKFine and NULL\n", toUnixString(os));
    }


    public void asFunctionWithVarArgs() throws Exception {
        boolean res = false;
        String scriptText = "function values(a, b) {\n" + //
                        "  print(\"Called with \" + a + \" and \" + b);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");
        PassInVarArg valuesIn = fn.as(PassInVarArg.class);

        valuesIn.call("OK", "Fine");
       // assertEquals("Called with OK and Fine\n", toUnixString(os));
        if("Called with OK and Fine\n".equals(toUnixString(os))) {

            res = true;

        }

        System.out.println("asFunctionWithVarArgs: " + res);
    }


    public void asFunctionWithArgVarArgs() throws Exception {
        boolean res = false;
        String scriptText = "function values(a, b, c) {\n" + //
                        "  print(\"Called with \" + a + \" and \" + b + c);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");
        PassInArgAndVarArg valuesIn = fn.as(PassInArgAndVarArg.class);

        valuesIn.call("OK", "Fine", "Well");
       // assertEquals("Called with OK and FineWell\n", toUnixString(os));

        if("Called with OK and FineWell\n".equals(toUnixString(os))) {

            res = true;

        }

            System.out.println("asFunctionWithArgVarArgs: " + res);
    }


    public void sumPairs() {
        boolean res = false;
        String scriptText = "function values(sum, k, v) {\n" + //
                        "  obj = new Object();\n" + //
                        "  obj.key = k;\n" + //
                        "  obj.value = v;\n" + //
                        "  return sum.sum(obj);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");

        Sum javaSum = new Sum();
        Object sum = javaSum;
        Object ret1 = fn.execute(sum, "one", 1).asHostObject();
        Object ret2 = fn.execute(sum, "two", 2).as(Object.class);
        Sum ret3 = fn.execute(sum, "three", 3).as(Sum.class);

        if(javaSum.sum == 6 && ret1 == ret2 && ret3 == ret2 && sum == ret2) {

            res = true;

        }

        System.out.println("sumPairs: " + res);
      //  assertEquals(6, javaSum.sum);
       // assertSame(ret1, ret2);
      //  assertSame(ret3, ret2);
      //  assertSame(sum, ret2);
    }


    public void sumPairsFunctionalInterface() {
        boolean res = false;
        String scriptText = "function values(sum, k, v) {\n" + //
                        "  obj = new Object();\n" + //
                        "  obj.key = k;\n" + //
                        "  obj.value = v;\n" + //
                        "  return sum.sum(obj);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Values fn = lookup("values").as(Values.class);

        Sum sum = new Sum();
        Object ret1 = fn.values(sum, "one", 1);
        Object ret2 = fn.values(sum, "two", 2);
        Object ret3 = fn.values(sum, "three", 3);


        if(sum.sum == 6 && ret1 == ret2 && ret3 == ret2 && sum == ret2) {


            res = true;

        }
        System.out.println("sumPairsFunctionalInterface: " + res);
     //   assertEquals(6, sum.sum);
      //  assertSame(ret1, ret2);
      //  assertSame(ret3, ret2);
      //  assertSame(sum, ret2);
    }


    public void sumPairsFunctionalRawInterface() {
        boolean res = false;
        String scriptText = "function values(sum, k, v) {\n" + //
                        "  obj = new Object();\n" + //
                        "  obj.key = k;\n" + //
                        "  obj.value = v;\n" + //
                        "  return sum.sum(obj);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        ValuesRaw fn = lookup("values").as(ValuesRaw.class);

        Sum sum = new Sum();
        Object ret1 = fn.values(sum, "one", 1);
        Object ret2 = fn.values(sum, "two", 2);
        Object ret3 = fn.values(sum, "three", 3);

        if(sum.sum == 6 && ret1 == ret2 && ret3 == ret2 && sum == ret2) {

            res = true;

        }

        System.out.println("sumPairsFunctionalRawInterface: " + res);
      //  assertEquals(6, sum.sum);
      //  assertSame(ret1, ret2);
       // assertSame(ret3, ret2);
      //  assertSame(sum, ret2);
    }


    public void sumPairsIndirect() {
        boolean res = false;
        String scriptText = "function values(sum, k, v) {\n" + //
                        "  obj = new Object();\n" + //
                        "  obj.key = k;\n" + //
                        "  obj.value = v;\n" + //
                        "  return sum.sum(obj);\n" + //
                        "}\n" + //
                        "function create() {\n" + //
                        "  obj = new Object();\n" + //
                        "  obj.doSum1 = values;\n" + //
                        "  obj.doSum2 = values;\n" + //
                        "  return obj;\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        DoSums fn = lookup("create").execute().as(DoSums.class);

        Sum sum = new Sum();
        Object ret1 = fn.doSum1(sum, "one", 1);
        Sum ret2 = fn.doSum2(sum, "two", 2);
        Object ret3 = fn.doSum1(sum, "three", 3);

        if(sum.sum == 6 && ret1 == ret2 && ret3 == ret2 && sum == ret2) {

            res = true;

        }

        System.out.println("sumPairsIndirect: " + res);
       // assertEquals(6, sum.sum);
       // assertSame(ret1, ret2);
      //  assertSame(ret3, ret2);
       // assertSame(sum, ret2);
    }


    public void sumPairsInArray() {
        boolean res = false;
        String scriptText = "function values(sum, arr) {\n" + //
                        "  sum.sumArray(arr);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");

        Sum javaSum = new Sum();

        PairImpl[] arr = {
                        new PairImpl("one", 1),
                        new PairImpl("two", 2),
                        new PairImpl("three", 3),
        };
        fn.execute(javaSum, arr);

        if(javaSum.sum == 6) {

            res = true;

        }

        System.out.println("sumPairsInArray: " + res);
       // assertEquals(6, javaSum.sum);
    }


    public void sumPairsInArrayOfArray() {
        boolean res = false;
        String scriptText = "function values(sum, arr) {\n" + //
                        "  sum.sumArrayArray(arr);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");

        Sum javaSum = new Sum();

        PairImpl[][] arr = {
                        new PairImpl[]{
                                        new PairImpl("one", 1),
                        },
                        new PairImpl[]{
                                        new PairImpl("two", 2),
                                        new PairImpl("three", 3),
                        }
        };
        fn.execute(javaSum, arr);
       // assertEquals(6, javaSum.sum);
        if(javaSum.sum == 6) {

            res = true;

        }

        System.out.println("sumPairsInArrayOfArray: " + res);
    }


    public void sumMapInArrayOfArray() {
        boolean res = false;
        String scriptText = "function values(sum, arr) {\n" + //
                        "  sum.sumArrayMap(arr);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");

        Sum javaSum = new Sum();

        PairImpl[][] arr = {
                        new PairImpl[]{
                                        new PairImpl("one", 1),
                        },
                        new PairImpl[]{
                                        new PairImpl("two", 2),
                                        new PairImpl("three", 3),
                        }
        };
        fn.execute(javaSum, arr);
       // assertEquals(6, javaSum.sum);

        if(javaSum.sum == 6) {

            res = true;

        }

        System.out.println("sumMapInArrayOfArray: " + res);
    }


    public void sumPairInMapOfArray() {
        boolean res = false;
        String scriptText = "function values(sum, arr) {\n" + //
                        "  sum.sumMapArray(arr);\n" + //
                        "}\n"; //
        context.eval("js", scriptText);
        Value fn = lookup("values");

        Sum javaSum = new Sum();

        TwoPairsImpl groups = new TwoPairsImpl(
                        new PairImpl[]{
                                        new PairImpl("one", 1),
                        },
                        new PairImpl[]{
                                        new PairImpl("two", 2),
                                        new PairImpl("three", 3),
                        });
        fn.execute(javaSum, groups);
      // assertEquals(6, javaSum.sum);

        if(javaSum.sum == 6) {

            res = true;

        }

        System.out.println("sumPairInMapOfArray: " + res);
    }


    public void accessJavaMap() {

        String scriptText = "function write(map, key, value) {\n" +
                        "  map.put(key, value);\n" +
                        "}\n" +
                        "function read(map, key) {\n" +
                        "  return map.get(key);\n" +
                        "}\n";
        context.eval("js", scriptText);
        Value read = lookup("read");
        Value write = lookup("write");

        Map<Object, Object> map = new HashMap<>();
        map.put("a", 42);

        Object b = read.execute(map, "a").as(Object.class);
        assertNumber(42L, b);

        write.execute(map, "a", 33);

        Object c = read.execute(map, "a").as(Object.class);
        assertNumber(33L, c);
    }


    public void testMemberAssignment() {
        boolean res = false;
        Integer hostObject = 6;
        context.eval("js", "function createNewObject() {\n" +
                        "  return new Object();\n" +
                        "}\n" +
                        "\n" +
                        "function assignObjectMemberFoo(obj, member) {\n" +
                        "  obj.foo = member;\n" +
                        "  return obj;\n" +
                        "}\n");
        Value bindings = context.getBindings("js");
        Value obj = bindings.getMember("createNewObject").execute();
        bindings.getMember("assignObjectMemberFoo").execute(obj, hostObject);

        if(obj.hasMember("foo") && hostObject.intValue() == obj.getMember("foo").asInt()) {

            res = true;

        }
        System.out.println("testMemberAssignment: " + res);
      //  assertTrue(obj.hasMember("foo"));
      //  assertEquals(hostObject.intValue(), obj.getMember("foo").asInt());
    }


    public void testCallback() {
        boolean res = false;
        TestObject hostObject = new TestObject();
        context.eval("js", "function createNewObject() {\n" +
                        "  return new Object();\n" +
                        "}\n" +
                        "\n" +
                        "function callMemberCallback(obj, memberName) {\n" +
                        "  return obj[memberName].callback(\"test\");\n" +
                        "}\n");
        Value bindings = context.getBindings("js");
        Value obj = bindings.getMember("createNewObject").execute();
        obj.putMember("hostObject", hostObject);
        Value v = bindings.getMember("callMemberCallback").execute(obj, "hostObject");

        if("test".equals(v.asString())) {

            res = true;

        }

        System.out.println("testCallback: " + res);
      //  assertEquals("test", v.asString());
    }

    /**
     * Converts a {@link ByteArrayOutputStream} content into UTF-8 String with UNIX line ends.
     */
    static String toUnixString(ByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @FunctionalInterface
    public interface Values {
        Sum values(Sum sum, String key, int value);
    }

    @FunctionalInterface
    public interface ValuesRaw {
        Object values(Object sum, String key, int value);
    }

    public interface DoSums {
        Object doSum1(Sum sum, String key, int value);

        Sum doSum2(Sum sum, String key, Integer value);
    }

    @FunctionalInterface
    public interface PassInArray {
        void call(Object[] arr);
    }

    @FunctionalInterface
    public interface PassInVarArg {
        void call(Object... arr);
    }

    @FunctionalInterface
    public interface PassInArgAndVarArg {
        void call(Object first, Object... arr);
    }

    public interface Pair {
        String key();

        int value();
    }

    public static final class PairImpl {
        public final String key;
        public final int value;

        PairImpl(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    public static final class TwoPairsImpl {
        public final PairImpl[] one;
        public final PairImpl[] two;

        TwoPairsImpl(PairImpl[] one, PairImpl[] two) {
            this.one = one;
            this.two = two;
        }
    }

    public static class Sum {
        int sum;

        @HostAccess.Export
        public Sum sum(Pair p) {
            sum += p.value();
            return this;
        }

        @HostAccess.Export
        public void sumArray(List<Pair> pairs) {
            Object[] arr = pairs.toArray();
           // assertNotNull("Array created", arr);
            for (Pair p : pairs) {
                sum(p);
            }
        }

        @HostAccess.Export
        public void sumArrayArray(List<List<Pair>> pairs) {
            Object[] arr = pairs.toArray();
          //  assertNotNull("Array created", arr);
           // assertEquals("Two lists", 2, arr.length);
            for (List<Pair> list : pairs) {
                sumArray(list);
            }
        }

        @HostAccess.Export
        public void sumArrayMap(List<List<Map<String, Integer>>> pairs) {
            Object[] arr = pairs.toArray();
           // assertNotNull("Array created", arr);
          //  assertEquals("Two lists", 2, arr.length);
            for (List<Map<String, Integer>> list : pairs) {
                for (Map<String, Integer> map : list) {
                    Integer value = map.get("value");
                    sum += value;
                }
            }
        }

        @HostAccess.Export
        public void sumMapArray(Map<String, List<Pair>> pairs) {
          //  assertEquals("Two elements", 2, pairs.size());
            Object one = pairs.get("one");
          //  assertNotNull(one);
            Object two = pairs.get("two");
          //  assertNotNull(two);

            sumArray(pairs.get("two"));
            sumArray(pairs.get("one"));
        }
    }

    public static class TestObject {

        public String callback(String msg) {
            return msg;
        }
    }
}
