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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

/** Guest Language is JS (except testParseErrorEmpty) **/
public class SLParseErrorTest {

    public static void main(String[] args) {

        SLParseErrorTest slParseErrorTest = new SLParseErrorTest();
        slParseErrorTest.setUp();
        slParseErrorTest.testParseError();
        slParseErrorTest.testParseErrorEOF1();
        slParseErrorTest.testParseErrorEOF2();
        slParseErrorTest.tearDown();

    }

    private Context context;


    public void setUp() {
        context = Context.create("js");
    }


    public void tearDown() {
        context = null;
    }


    public void testParseError() {
        try {
            final Source src = Source.newBuilder("js", "function testSyntaxError(a) {break;} function main() {return testSyntaxError;}", "testSyntaxError.js").buildLiteral();
            context.eval(src);
            System.out.println("Should not reach here.");
           // Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            System.out.println("testParseError");
            System.out.println("Should be a syntax error: " + e.isSyntaxError());
            System.out.println("Should have source section: " + e.getSourceLocation());
          //  Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
          //  Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }


    //empty script in js or python is valid

  /*  public void testParseErrorEmpty() {
        try {
            final Source src = Source.newBuilder("python", "", "testSyntaxErrorEmpty.py").buildLiteral();
            context.eval(src);
            System.out.println("Should not reach here.");
          //  Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            System.out.println("testParseErrorEmpty");
            System.out.println("Should be a syntax error: " + e.isSyntaxError());
            System.out.println("Should have source section: " + e.getSourceLocation());
         //   Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
          //  Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }*/


    public void testParseErrorEOF1() {
        try {
            final Source src = Source.newBuilder("js", "function main", "testSyntaxErrorEOF1.js").buildLiteral();
            context.eval(src);
          //  Assert.assertTrue("Should not reach here.", false);
            System.out.println("Should not reach here.");
        } catch (PolyglotException e) {
            System.out.println("testParseErrorEOF1");
            System.out.println("Should be a syntax error: " + e.isSyntaxError());
          //  Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            System.out.println("Should have source section: " + e.getSourceLocation());
          //  Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }


    public void testParseErrorEOF2() {
        try {
            final Source src = Source.newBuilder("js", "function\n", "testSyntaxErrorEOF2.js").buildLiteral();
            context.eval(src);
            System.out.println("Should not reach here.");
          // Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            System.out.println("testParseErrorEOF2");
            System.out.println("Should be a syntax error: " + e.isSyntaxError());
          //  Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            System.out.println("Should have source section: " + e.getSourceLocation());
          //  Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }
}
