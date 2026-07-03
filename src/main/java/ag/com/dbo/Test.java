package ag.com.dbo;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.Map;


public class Test {

    public static void main(String[] args) {
        String s="aaaa,bbb,ccc";
        List<String> nodeTagsList = List.of(s.split(","));
        System.out.println(nodeTagsList);

    }
}
