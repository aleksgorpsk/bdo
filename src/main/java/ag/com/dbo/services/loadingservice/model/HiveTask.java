package ag.com.dbo.services.loadingservice.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@Data

public class HiveTask<String> implements Callable<String> {
    private String inn;
    private String out;
    public HiveTask(String data){
        this.inn = data;
    }
    @Override
    public String call() throws Exception {



        out = (String) (inn + " Result");
        return out;
    }
}
