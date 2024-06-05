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

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyInstantiable;
import org.graalvm.polyglot.proxy.ProxyObject;

/** Guest language is Python **/

public class SLInteropObjectTest {

    public static void main(String[] args) {

        SLInteropObjectTest slInteropObjectTest = new SLInteropObjectTest();

        slInteropObjectTest.setUp();
        slInteropObjectTest.testObject();
        slInteropObjectTest.testNewForeign();

    }

    private Context context;


    public void setUp() {
        context = Context.create("python");
    }


    public void tearDown() {
        context.close();
        context = null;
    }


    public void testObject() {
        final Source src = Source.newBuilder("python",  "def main():\n" +
                "    o = type('', (), {})()\n" +  // Creating an empty object
                "    o.a = 10\n" +
                "    o.b = \"B\"\n" +
                "    return o\n", "testObject.py").buildLiteral();
        final Value obj = context.eval(src).getMember("main").execute();
       // Assert.assertTrue(obj.hasMembers());
        if(obj.hasMembers()) {
            Value a = obj.getMember("a");
            System.out.println("a (should be 10): " + a.asInt());
            // Assert.assertNotNull(a);
            // Assert.assertTrue(a.isNumber());
            //  Assert.assertEquals(10, a.asInt());

            Value b = obj.getMember("b");
            System.out.println("b (shoud be B): " + b.asString());
            //  Assert.assertNotNull(b);
            // Assert.assertTrue(b.isString());
            //  Assert.assertEquals("B", b.asString());

            obj.putMember("a", b);
            a = obj.getMember("a");
            System.out.println("a (should be B): " + a.asString());
            //  Assert.assertTrue(a.isString());
            //  Assert.assertEquals("B", a.asString());

            obj.removeMember("a");
            System.out.println("should be false: " + obj.hasMember("a"));
            // Assert.assertFalse(obj.hasMember("a"));

            System.out.println("[b]: " + obj.getMemberKeys().toString());
            //  Assert.assertEquals("[b]", obj.getMemberKeys().toString());
        }
    }


    public void testNewForeign() {
        final Source src = Source.newBuilder("python", "def get_value(type):\n" +
                "    o = type()\n" +
                "    o.a = 10\n" +
                "    return o.value\n", "testObject.py").buildLiteral();
        context.eval(src);
        Value getValue = context.getBindings("python").getMember("get_value");
        Value ret = getValue.execute(new TestType());
        System.out.println("20: " + ret.asLong());
       // Assert.assertEquals(20, ret.asLong());
    }

    private static class TestType implements ProxyInstantiable {

        @Override
        public Object newInstance(Value... arguments) {
            return new TestObject();
        }

    }

    private static class TestObject implements ProxyObject {

        private long value;

        @Override
        public Object getMember(String key) {
            if ("value".equals(key)) {
                return 2 * value;
            }
            return 0;
        }

        @Override
        public Object getMemberKeys() {
            return new String[]{"a", "value"};
        }

        @Override
        public boolean hasMember(String key) {
            switch (key) {
                case "a":
                case "value":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void putMember(String key, Value v) {
            value += v.asLong();
        }

    }
}
