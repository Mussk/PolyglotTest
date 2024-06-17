/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
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

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.math.BigInteger;

/** Guest language is Python **/

public class SLInteropPrimitiveTest {

    public static void main(String[] args) {

        SLInteropPrimitiveTest slInteropPrimitiveTest = new SLInteropPrimitiveTest();

        slInteropPrimitiveTest.setUp();
        slInteropPrimitiveTest.testBoolean();
        slInteropPrimitiveTest.testChar();
        slInteropPrimitiveTest.testNumbers();
        slInteropPrimitiveTest.tearDown();

    }


    private Context context;

    public void setUp() {
        context = Context.create("python");
    }


    public void tearDown() {
        context = null;
    }


    public void testBoolean() {
        final Source src = Source.newBuilder("python", "def testBoolean(a, b):\n   return a == b\ndef main(a, b):\n   return testBoolean(a, b)", "testBoolean.py").buildLiteral();
        final Value fnc = context.eval(src);
        //Assert.assertTrue(fnc.canExecute());
        final Value res = fnc.getMember("main").execute(true, false);
        System.out.println("False: " + res.asBoolean());

    }


    public void testChar() {
        final Source src = Source.newBuilder("python", "def testChar(a, b):\n    return a == b\ndef main(a, b):\n    return testChar(a, b)", "testChar.py").buildLiteral();
        final Value fnc = context.eval(src);
        //Assert.assertTrue(fnc.canExecute());
        final Value res = fnc.getMember("main").execute('a', 'b');
        System.out.println("False: " + res.asBoolean());
    }


    public void testNumbers() {
        context = Context.create("js");
        context = Context.newBuilder("js").build();
        final Source src = Source.newBuilder("js", "function addNum(a,b) {return a + b;} function main(a, b) {return addNum(a, b);}", "addNum.js").buildLiteral();
        final Value fnc = context.eval(src);
        Value res = fnc.getContext().getBindings("js").getMember("main").execute(20, 22);
        System.out.println("42: " + res.asInt());
        res = fnc.getContext().getBindings("js").getMember("main").execute(Long.MAX_VALUE, Long.MAX_VALUE);
        //should be 14 in the end
        System.out.println(new BigInteger("18446744073709551616").equals(res.asBigInteger()) ? "Big integers are equal" : "Big integers are not equal");
        res = fnc.getContext().getBindings("js").getMember("main").execute(BigInteger.valueOf(Long.MAX_VALUE)/*.add(BigInteger.TWO)*/, Long.MAX_VALUE);
        System.out.println(new BigInteger("18446744073709551616").equals(res.asBigInteger()) ? "Big integers are equal" : "Big integers are not equal");
        res = fnc.getContext().getBindings("js").getMember("main").execute(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE)/*.add(BigInteger.TWO)*/);
        System.out.println(new BigInteger("18446744073709551616").equals(res.asBigInteger()) ? "Big integers are equal" : "Big integers are not equal");
        //should be 18 at the end
        res = fnc.getContext().getBindings("js").getMember("main").execute(BigInteger.valueOf(Long.MAX_VALUE)/*.add(BigInteger.TWO)*/, BigInteger.valueOf(Long.MAX_VALUE)/*.add(BigInteger.TWO)*/);
        System.out.println(new BigInteger("18446744073709551616").equals(res.asBigInteger()) ? "Big integers are equal" : "Big integers are not equal");

       // Assert.assertTrue(fnc.canExecute());
       // Assert.assertEquals(42, fnc.execute(20, 22).asInt());
       // Assert.assertEquals(new BigInteger("18446744073709551614"), fnc.execute(Long.MAX_VALUE, Long.MAX_VALUE).asBigInteger());
       // Assert.assertEquals(new BigInteger("18446744073709551616"), fnc.execute(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO), Long.MAX_VALUE).asBigInteger());
       // Assert.assertEquals(new BigInteger("18446744073709551616"), fnc.execute(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO)).asBigInteger());
       // Assert.assertEquals(new BigInteger("18446744073709551618"),
        //    fnc.execute(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO), BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO)).asBigInteger());
    }
}
