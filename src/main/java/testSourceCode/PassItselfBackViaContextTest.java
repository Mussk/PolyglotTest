package testSourceCode;

import org.graalvm.polyglot.Context;

import org.graalvm.polyglot.Value;


/** Guest language is Python **/

public class PassItselfBackViaContextTest {

    public static void main(String[] args) {

        PassItselfBackViaContextTest passItselfBackViaContextTest = new PassItselfBackViaContextTest();

        passItselfBackViaContextTest.prepareSystem();
        passItselfBackViaContextTest.callbackWithParamTen();
        passItselfBackViaContextTest.callbackWithParamTruffleObject();
        passItselfBackViaContextTest.callbackWithValueTen();
        passItselfBackViaContextTest.callbackWithValueTruffleObject();
        passItselfBackViaContextTest.disposeSystem();


    }

    private Context context;
    private MyObj myObj;
    private Value myObjWrapped;
    private CallWithValue myObjCall;

    
    public void callbackWithParamTen() {
        myObjWrapped.execute(10);

    }

    
    public void callbackWithParamTruffleObject() {
        myObjWrapped.execute(myObjWrapped.execute(myObj));

    }

    
    public void callbackWithValueTen() {
        myObjCall.call(10);

    }

    
    public void callbackWithValueTruffleObject() {
        myObjCall.call(myObjWrapped.execute(myObj));

    }


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

        myObjCall = myObjWrapped.as(CallWithValue.class);
    }


    public void disposeSystem() {
        context.close();
    }



    public static class MyObj {
        private Object value;


        public Object execute(Object[] arguments) {
            value = arguments[0];
            return "";
        }

        @SuppressWarnings("static-method")
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
