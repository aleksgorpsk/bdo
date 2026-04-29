package ag.com.dbo.services.loadingService.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@Data

public class  Task implements Callable<PropData>  {
    private String inn;
    private String out;
    public Task(String data){
        this.inn = data;
    }
    @Override
    public PropData call() throws Exception {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        out = inn + " Result";
        return new PropData(0, out );
    }
}
