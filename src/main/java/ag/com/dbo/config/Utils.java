package ag.com.dbo.config;

import java.util.Map;

public  class Utils {
    public static  Map<String,Object> checkProps(Map<String,Object> property, String name){
        Object val =property.get(name);
        if (val!=null){
            property.put(name, val);
        }
        return property;
    }

}
