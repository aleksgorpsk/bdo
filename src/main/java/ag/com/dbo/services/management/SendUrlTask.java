package ag.com.dbo.services.management;


import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import ag.com.scheduling.models.ScheduleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.math.BigInteger;

@Slf4j
public class SendUrlTask implements Runnable{
    private final ExternalServiceImpl externalService;
    private final BigInteger etlId;

    public SendUrlTask(ExternalServiceImpl externalService, BigInteger etlId){
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
