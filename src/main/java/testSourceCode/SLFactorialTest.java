/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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
import org.graalvm.polyglot.Value;

/** Guest language is Python **/

public class SLFactorialTest {

    public static void main(String[] args) {

        SLFactorialTest slFactorialTest = new SLFactorialTest();

        try {

            slFactorialTest.initEngine();
            slFactorialTest.factorialOf5();
            slFactorialTest.factorialOf3();
            slFactorialTest.factorialOf1();

        }catch (Exception exception){ exception.printStackTrace();}

    }

    private Context context;
    private Value factorial;


    public void initEngine() throws Exception {
        context = Context.create();
        // @formatter:off
        context.eval("python", "def fac(n):\n" +
                "   if n <= 1:\n" +
                "       return 1\n" +
                "   prev = fac(n - 1)\n" +
                "   return prev * n\n"
        );
        // @formatter:on
        factorial = context.getBindings("python").getMember("fac");
    }


    public void dispose() {
        context.close();
    }


    public void factorialOf5() throws Exception {
        Number ret = factorial.execute(5).as(Number.class);
        //assertEquals(120, ret.intValue());
        System.out.println("120: " + ret.intValue());
    }


    public void factorialOf3() throws Exception {
        Number ret = factorial.execute(3).as(Number.class);
        //assertEquals(6, ret.intValue());
        System.out.println("6: " + ret.intValue());
    }


    public void factorialOf1() throws Exception {
        Number ret = factorial.execute(1).as(Number.class);
        //assertEquals(1, ret.intValue());
        System.out.println("1: " + ret.intValue());
    }
}
