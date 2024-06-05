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

import static testSourceCode.SLJavaInteropTest.toUnixString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.graalvm.polyglot.*;

/** Guest lang is JS **/
public class SLSharedCodeSeparatedEnvTest {

    public static void main(String[] args) {

        SLSharedCodeSeparatedEnvTest slSharedCodeSeparatedEnvTest = new SLSharedCodeSeparatedEnvTest();
        try {
            slSharedCodeSeparatedEnvTest.initializeEngines();
            slSharedCodeSeparatedEnvTest.shareCodeUseDifferentOutputStreams();
            slSharedCodeSeparatedEnvTest.cleanOutStreams();
            slSharedCodeSeparatedEnvTest.instrumentsSeeOutputOfBoth();
            slSharedCodeSeparatedEnvTest.closeEngines();
        }catch (Exception exception) {exception.printStackTrace();}
    }

    //public static void runWithWeakEncapsulationOnly() {
       // TruffleTestAssumptions.assumeWeakEncapsulation();
   // }

    private ByteArrayOutputStream osRuntime;
    private ByteArrayOutputStream os1;
    private ByteArrayOutputStream os2;
    private Engine engine;
    private Context e1;
    private Context e2;

    public void cleanOutStreams() {

        os1.reset();
        os2.reset();
    }

    public void initializeEngines() {
        osRuntime = new ByteArrayOutputStream();
        engine = Engine.newBuilder().out(osRuntime).err(osRuntime).build();

        os1 = new ByteArrayOutputStream();
        os2 = new ByteArrayOutputStream();

        int instances = 2;
        // @formatter:off
        e1 = Context.newBuilder("js").engine(engine).out(os1).allowPolyglotAccess(PolyglotAccess.ALL).build();
        e1.getPolyglotBindings().putMember("extra", 1);
        e2 = Context.newBuilder("js").engine(engine).out(os2).allowPolyglotAccess(PolyglotAccess.ALL).build();
        e2.getPolyglotBindings().putMember("extra", 2);
        e1.initialize("js");
        e2.initialize("js");
        //assertEquals("One SLLanguage instance created", instances + 1, SLLanguage.counter);

    }


    public void closeEngines() {
        engine.close();
    }


    public void shareCodeUseDifferentOutputStreams() throws Exception {

        String sayHello =
            "function main() {\n" +
            "  print(\"Ahoj\" + Polyglot.import('extra'));\n" +
            "}";
        // @formatter:on

        e1.eval(Source.newBuilder("js", sayHello,"Test1").build());
        e1.getBindings("js").getMember("main").execute();
        if("Ahoj1\n".equals(toUnixString(os1))) {

            System.out.println(toUnixString(os1));

        }
        //assertEquals("Ahoj1\n", toUnixString(os1));
        //assertEquals("", toUnixString(os2));

        e2.eval(Source.newBuilder("js", sayHello,"Test2").build());
        e2.getBindings("js").getMember("main").execute();
        if("Ahoj1\n".equals(toUnixString(os1)) && "Ahoj2\n".equals(toUnixString(os2))) {

            System.out.println(toUnixString(os1) + ", " + toUnixString(os2));

        }
        //assertEquals("Ahoj1\n", toUnixString(os1));
       // assertEquals("Ahoj2\n", toUnixString(os2));
    }


    public void instrumentsSeeOutputOfBoth() throws Exception {

        ByteArrayOutputStream combinedOutput = new ByteArrayOutputStream();

        String sayHello = "function main() {\n" +
                        "  print(\"Ahoj\" + Polyglot.import(\"extra\"));\n" +
                        "}";
        // @formatter:on

        e1.eval(Source.newBuilder("js", sayHello, "Test1").build());
        e1.getBindings("js").getMember("main").execute();
        combinedOutput.write(os1.toByteArray());
       // assertEquals("Ahoj1\n", toUnixString(os1));
      //  assertEquals("", toUnixString(os2));
        if("Ahoj1\n".equals(toUnixString(os1)) && toUnixString(os2).isEmpty()) {

            System.out.println(true);

        }

        e2.eval(Source.newBuilder("js", sayHello, "Test2").build());
        e2.getBindings("js").getMember("main").execute();
        combinedOutput.write(os2.toByteArray());

        if("Ahoj1\n".equals(toUnixString(os1)) && "Ahoj2\n".equals(toUnixString(os2))) {

            System.out.println(true);

        }
       // assertEquals("Ahoj1\n", toUnixString(os1));
       // assertEquals("Ahoj2\n", toUnixString(os2));

        combinedOutput.write("endOfOutputCapture\n".getBytes(StandardCharsets.UTF_8));

        System.out.println(toUnixString(combinedOutput));
        System.out.println(toUnixString(osRuntime));
        engine.close();
    }

}
