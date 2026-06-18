package ag.com.dbo;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;


public class Test {

    public static void main(String[] args) {
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            // Evaluate simple Python statements
            context.eval("python", "print('Hello from Python via GraalPy!')");

            // Pass data and get results back
            context.eval("python", "def square(x): return x * x");
            Value squareFunc = context.getBindings("python").getMember("square");
            int result = squareFunc.execute(5).asInt();
            System.out.println("Result from Python: " + result);
        }
    }
}
