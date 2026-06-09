package ag.com.dbo;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.List;



public class Test {

    public static void main(String[] args) throws Exception {

        List input = List.of("apple","orange");
//        Map<String, Object> vars = stringToJsonVar(sVars);
        Binding binding = new Binding();
        binding.setVariable("input", input);
        GroovyShell shell = new GroovyShell(binding);
        Object oResult = shell.evaluate("""
import groovy.json.JsonOutput

println input[0]
def res=[input[0]]
res.add(input[0])

JsonOutput.toJson(res)

""");
// {"branches1":"{"directoryName":"/Users/aleksgor/opt/files", "resultName":"testFile"}"

        String jsonString =oResult.toString();

        ObjectMapper mapper = new ObjectMapper();
//        Object x =  mapper.readTree(jsonString);
        List<String> list = mapper.readValue(jsonString, new TypeReference<ArrayList<String>>() {});
        System.out.println("------------");
        System.out.println(list);

    }

}
