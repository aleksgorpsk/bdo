package ag.com.dbo.services.management;


import ag.com.scheduling.models.ScheduleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.math.BigInteger;

@Slf4j
public class SendUrlTask implements Runnable{
    private final ExternalService externalService;
    private final BigInteger etlId;

    public SendUrlTask(ExternalService externalService, BigInteger etlId){
        this.externalService = externalService;
        this.etlId = etlId;
    }
    @Override
    public void run() {
        log.info("Run!!:{}", this.etlId);

        externalService.getManager().put()
                .uri("/schedule", etlId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ScheduleData(this.etlId))
                .retrieve()
                .toBodilessEntity();

    }
}
