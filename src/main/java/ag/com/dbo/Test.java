package ag.com.dbo;

import java.util.concurrent.*;
public class Test {

    public static void main(String[] args) {

        String template = "Welcome {{user}}!";
        String result = template.replace("{{user}}", "Charlie");

        String l = "echo  \"!---!{msg}\"  {msg}";
        System.out.println(l.replace("{msg}","ddddd"));
    }
}
