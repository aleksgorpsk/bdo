package ag.com.dbo;


import com.fasterxml.jackson.databind.ObjectMapper;



public class Test {

    public static void main(String[] args) throws Exception {
        String test ="{\"cnt\":[\"349965\"],\"mean\":[\"65665.00440044004\"]}";

        ObjectMapper mapper = new ObjectMapper();
        Object x =mapper.readTree(test);
        System.out.println(x);

    }

}
