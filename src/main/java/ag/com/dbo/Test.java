package ag.com.dbo;


import java.util.List;


public class Test {

    public static void main(String[] args) {
        String s="aaaa,bbb,ccc";
        List<String> nodeTagsList = List.of(s.split(","));
        System.out.println(nodeTagsList);

    }
}
