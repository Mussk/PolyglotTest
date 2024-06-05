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

/** Guest language is Python **/

public class SLInteropControlFlowTest {

    public static void main(String[] args) {

            SLInteropControlFlowTest slInteropControlFlowTest = new SLInteropControlFlowTest();

            slInteropControlFlowTest.setUp();
            slInteropControlFlowTest.testWhile();
            slInteropControlFlowTest.testIf();
            slInteropControlFlowTest.tearDown();

    }

    private Context context;


    public void setUp() {
        context = Context.create("python");
    }


    public void tearDown() {
        context = null;
    }


    public void testWhile() {
        final Source src = Source.newBuilder("python",  "def test_while(a):\n" +
                "    while a:\n" +
                "        break\n" +
                "    return\n" +
                "\n" +
                "def main(a):\n" +
                "    return test_while(a)\n", "testWhile.py").buildLiteral();
        final Value fnc = context.eval(src).getMember("main");
        //Assert.assertTrue(fnc.canExecute());
        if(fnc.canExecute())
            fnc.execute(false);

    }


    public void testIf() {
        final Source src = Source.newBuilder("python",  "def test_if(a):\n" +
                "    if a:\n" +
                "        return 1\n" +
                "    else:\n" +
                "        return 0\n" +
                "\n" +
                "def main(a):\n" +
                "    return test_if(a)\n", "testIf.py").buildLiteral();
        final Value fnc = context.eval(src).getMember("main");
        if(fnc.canExecute())
            fnc.execute(false);
    }
}
