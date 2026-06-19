package ag.com.dbo;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.Map;


public class PyTest {

    public static void main(String[] args) {
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            // Evaluate simple Python statements
    //        context.eval("python", "print('Hello from Python via GraalPy!')");
            Map c= Map.of("directoryName","/Users/aleksgor/opt/files","stepResultName","testFile1");
            Map d= Map.of("sensor1",c);
            String t= "sensor1";

            // Pass data and get results back
            String s1="def checkw(x, t):\n" +
                    " print(\"333333\") \n"+
                    "return x.get(t).get('stepResultName') \n";
            String s2="import json\n" +
                    "\n" +
                    "def parse(stepName,vars, results):\n" +
                    "    return json.dumps('3')\n" +
                    "}\n" +
                    "\n" +
                    "return  parse(stepName,vars, results)\n";
            String pythonCode = "import json \n" +
                            "def process_data(a, b):\n" +
                            "    result = a + b\n" +
                            "    greeting = \"Hello from Python!\"\n" +
                            "    my_map = {'a':'b'} \n" +
                            "    return json.dumps('3') \n";
//                            "    return json.dumps(my_map, indent=4)\n";
            context.eval("python", pythonCode);
            Value squareFunc = context.getBindings("python").getMember("process_data");
            String result = squareFunc.execute(3, 4).asString();

            System.out.println("Result from Python: " + result);
        }
    }
}
