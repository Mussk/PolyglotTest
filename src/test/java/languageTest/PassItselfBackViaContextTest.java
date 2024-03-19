package languageTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.graalvm.polyglot.Context;

import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

public class PassItselfBackViaContextTest {

    private Context context;
    private MyObj myObj;
    private Value myObjWrapped;
    private CallWithValue myObjCall;

    @Test
    public void callbackWithParamTen() {
        myObjWrapped.execute(10);
        assertEquals("Assigned to ten", 10, myObj.value);
    }

    @Test
    public void callbackWithParamTruffleObject() {
        myObjWrapped.execute(myObjWrapped.execute(myObj));
        assertEquals("Assigned to itself", myObj, myObj.value);
    }

    @Test
    public void callbackWithValueTen() {
        myObjCall.call(10);
        assertEquals("Assigned to ten", 10, myObj.value);
    }

    @Test
    public void callbackWithValueTruffleObject() {
        myObjCall.call(myObjWrapped.execute(myObj));
        assertEquals("Assigned to itself", myObj, myObj.value);
    }

    @Before
    public void prepareSystem() {
        myObj = new MyObj();
        context = Context.newBuilder().allowAllAccess(true).build();
        context.getPolyglotBindings().putMember("myObj", myObj);
        context.eval("python", "import polyglot \n" +
                                                "myObj = polyglot.import_value('myObj') \n" +
                                                "def main(arg): \n" +
                                                "   myObj.setValue(arg)\n" +
                                                "   return myObj");
        myObjWrapped = Value.asValue(context.getBindings("python").getMember("main"));
        assertFalse(myObjWrapped.isNull());
        myObjCall = myObjWrapped.as(CallWithValue.class);
    }

    @After
    public void disposeSystem() {
        context.close();
    }


    @ExportLibrary(InteropLibrary.class)
    public static class MyObj implements TruffleObject {
        private Object value;

        @ExportMessage

        public Object execute(Object[] arguments) {
            value = arguments[0];
            return "";
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        public boolean isExecutable() {
            return true;
        }

        public void setValue(Object valueLoc) {

            value = valueLoc;
        }
        public Object getValue() {

                return  value;
        }
    }

    @FunctionalInterface
    interface CallWithValue {
        void call(Object value);
    }
}
